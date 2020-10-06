package tech.pegasys.teku.networking.p2p

import com.google.common.eventbus.EventBus
import org.assertj.core.util.VisibleForTesting
import tech.pegasys.teku.core.StateTransition
import tech.pegasys.teku.core.operationsignatureverifiers.ProposerSlashingSignatureVerifier
import tech.pegasys.teku.core.operationsignatureverifiers.VoluntaryExitSignatureVerifier
import tech.pegasys.teku.core.operationvalidators.AttesterSlashingStateTransitionValidator
import tech.pegasys.teku.core.operationvalidators.ProposerSlashingStateTransitionValidator
import tech.pegasys.teku.core.operationvalidators.VoluntaryExitStateTransitionValidator
import tech.pegasys.teku.datastructures.attestation.ValidateableAttestation
import tech.pegasys.teku.datastructures.networking.libp2p.rpc.MetadataMessage
import tech.pegasys.teku.datastructures.operations.Attestation
import tech.pegasys.teku.datastructures.operations.AttesterSlashing
import tech.pegasys.teku.datastructures.operations.ProposerSlashing
import tech.pegasys.teku.datastructures.operations.SignedVoluntaryExit
import tech.pegasys.teku.networking.eth2.AttestationSubnetService
import tech.pegasys.teku.networking.eth2.Eth2Network
import tech.pegasys.teku.networking.eth2.gossip.AggregateGossipManager
import tech.pegasys.teku.networking.eth2.gossip.AttestationGossipManager
import tech.pegasys.teku.networking.eth2.gossip.AttestationSubnetSubscriptions
import tech.pegasys.teku.networking.eth2.gossip.AttesterSlashingGossipManager
import tech.pegasys.teku.networking.eth2.gossip.BlockGossipManager
import tech.pegasys.teku.networking.eth2.gossip.ProposerSlashingGossipManager
import tech.pegasys.teku.networking.eth2.gossip.VoluntaryExitGossipManager
import tech.pegasys.teku.networking.eth2.gossip.encoding.GossipEncoding
import tech.pegasys.teku.networking.eth2.gossip.topics.GossipedOperationConsumer
import tech.pegasys.teku.networking.eth2.gossip.topics.ProcessedAttestationSubscriptionProvider
import tech.pegasys.teku.networking.eth2.gossip.topics.VerifiedBlockAttestationsSubscriptionProvider
import tech.pegasys.teku.networking.eth2.gossip.topics.validation.AttestationValidator
import tech.pegasys.teku.networking.eth2.gossip.topics.validation.AttesterSlashingValidator
import tech.pegasys.teku.networking.eth2.gossip.topics.validation.BlockValidator
import tech.pegasys.teku.networking.eth2.gossip.topics.validation.ProposerSlashingValidator
import tech.pegasys.teku.networking.eth2.gossip.topics.validation.SignedAggregateAndProofValidator
import tech.pegasys.teku.networking.eth2.gossip.topics.validation.VoluntaryExitValidator
import tech.pegasys.teku.networking.eth2.peers.Eth2PeerManager
import tech.pegasys.teku.networking.eth2.rpc.beaconchain.BeaconChainMethods
import tech.pegasys.teku.networking.p2p.network.DelegatingP2PNetwork
import tech.pegasys.teku.networking.p2p.network.P2PNetwork
import tech.pegasys.teku.networking.p2p.peer.NodeId
import tech.pegasys.teku.networking.p2p.peer.PeerConnectedSubscriber
import tech.pegasys.teku.networking.discovery.VirtualDiscoveryNetwork
import tech.pegasys.teku.networking.eth2.peers.Eth2Peer
import tech.pegasys.teku.networking.p2p.peer.Peer
import tech.pegasys.teku.ssz.SSZTypes.SSZList
import tech.pegasys.teku.storage.client.RecentChainData
import tech.pegasys.teku.util.async.SafeFuture
import java.util.HashSet
import java.util.Optional
import java.util.concurrent.atomic.AtomicReference
import java.util.function.Consumer
import java.util.stream.Stream

class VirtualActiveEth2Network(
        private val discoveryNetwork: VirtualDiscoveryNetwork<*>,
        @get:VisibleForTesting val peerManager: Eth2PeerManager,
        private val eventBus: EventBus,
        private val recentChainData: RecentChainData,
        private val gossipEncoding: GossipEncoding,
        private val attestationSubnetService: AttestationSubnetService,
        // Upstream consumers
        private val gossipedAttestationConsumer: GossipedOperationConsumer<ValidateableAttestation>,
        private val gossipedAttesterSlashingConsumer: GossipedOperationConsumer<AttesterSlashing>,
        private val gossipedProposerSlashingConsumer: GossipedOperationConsumer<ProposerSlashing>,
        private val gossipedVoluntaryExitConsumer: GossipedOperationConsumer<SignedVoluntaryExit>,
        private val processedAttestationSubscriptionProvider: ProcessedAttestationSubscriptionProvider,
        private val verifiedBlockAttestationsSubscriptionProvider: VerifiedBlockAttestationsSubscriptionProvider) : DelegatingP2PNetwork<Eth2Peer>(discoveryNetwork), Eth2Network {
    private val state = AtomicReference(P2PNetwork.State.IDLE)
    private val pendingSubnetSubscriptions: MutableSet<Int> = HashSet()

    // Gossip managers
    private var blockGossipManager: BlockGossipManager? = null
    private var attestationGossipManager: AttestationGossipManager? = null
    private var aggregateGossipManager: AggregateGossipManager? = null
    private var voluntaryExitGossipManager: VoluntaryExitGossipManager? = null
    private var proposerSlashingGossipManager: ProposerSlashingGossipManager? = null
    private var attesterSlashingGossipManager: AttesterSlashingGossipManager? = null
    private var discoveryNetworkAttestationSubnetsSubscription: Long = 0
    override fun start(): SafeFuture<*> {
        // Set the current fork info prior to discovery starting up.
        val currentForkInfo = recentChainData
                .headForkInfo
                .orElseThrow { IllegalStateException("Can not start Eth2Network before genesis is known") }
        discoveryNetwork.setForkInfo(currentForkInfo, recentChainData.nextFork)
        return super.start().thenAccept { r: Any? -> startup() }
    }

    @Synchronized
    private fun startup() {
        state.set(P2PNetwork.State.RUNNING)
        val blockValidator = BlockValidator(recentChainData, StateTransition())
        val attestationValidator = AttestationValidator(recentChainData)
        val aggregateValidator = SignedAggregateAndProofValidator(attestationValidator, recentChainData)
        val forkInfo = recentChainData.headForkInfo.orElseThrow()
        val exitValidator = VoluntaryExitValidator(
                recentChainData,
                VoluntaryExitStateTransitionValidator(),
                VoluntaryExitSignatureVerifier())
        val proposerSlashingValidator = ProposerSlashingValidator(
                recentChainData,
                ProposerSlashingStateTransitionValidator(),
                ProposerSlashingSignatureVerifier())
        val attesterSlashingValidator = AttesterSlashingValidator(
                recentChainData, AttesterSlashingStateTransitionValidator())
        val attestationSubnetSubscriptions = AttestationSubnetSubscriptions(
                discoveryNetwork,
                gossipEncoding,
                attestationValidator,
                recentChainData,
                gossipedAttestationConsumer)
        blockGossipManager = BlockGossipManager(
                discoveryNetwork, gossipEncoding, forkInfo, blockValidator, eventBus)
        attestationGossipManager = AttestationGossipManager(gossipEncoding, attestationSubnetSubscriptions)
        aggregateGossipManager = AggregateGossipManager(
                discoveryNetwork,
                gossipEncoding,
                forkInfo,
                aggregateValidator,
                gossipedAttestationConsumer)
        voluntaryExitGossipManager = VoluntaryExitGossipManager(
                discoveryNetwork,
                gossipEncoding,
                forkInfo,
                exitValidator,
                gossipedVoluntaryExitConsumer)
        proposerSlashingGossipManager = ProposerSlashingGossipManager(
                discoveryNetwork,
                gossipEncoding,
                forkInfo,
                proposerSlashingValidator,
                gossipedProposerSlashingConsumer)
        attesterSlashingGossipManager = AttesterSlashingGossipManager(
                discoveryNetwork,
                gossipEncoding,
                forkInfo,
                attesterSlashingValidator,
                gossipedAttesterSlashingConsumer)
        discoveryNetworkAttestationSubnetsSubscription = attestationSubnetService.subscribeToUpdates { subnetIds: Iterable<Int?>? -> discoveryNetwork.setLongTermAttestationSubnetSubscriptions(subnetIds) }
        pendingSubnetSubscriptions.forEach(Consumer { subnetId: Int -> subscribeToAttestationSubnetId(subnetId) })
        pendingSubnetSubscriptions.clear()
        processedAttestationSubscriptionProvider.subscribe { validateableAttestation: ValidateableAttestation? -> attestationGossipManager!!.onNewAttestation(validateableAttestation) }
        processedAttestationSubscriptionProvider.subscribe { validateableAttestation: ValidateableAttestation? -> aggregateGossipManager!!.onNewAggregate(validateableAttestation) }
        verifiedBlockAttestationsSubscriptionProvider.subscribe { attestations: SSZList<Attestation?> ->
            attestations.forEach(
                    Consumer { attestation: Attestation? ->
                        aggregateValidator.addSeenAggregate(
                                ValidateableAttestation.fromAttestation(attestation))
                    })
        }
    }

    @Synchronized
    override fun stop() {
        if (!state.compareAndSet(P2PNetwork.State.RUNNING, P2PNetwork.State.STOPPED)) {
            return
        }
        blockGossipManager!!.shutdown()
        attestationGossipManager!!.shutdown()
        aggregateGossipManager!!.shutdown()
        voluntaryExitGossipManager!!.shutdown()
        proposerSlashingGossipManager!!.shutdown()
        attesterSlashingGossipManager!!.shutdown()
        attestationSubnetService.unsubscribe(discoveryNetworkAttestationSubnetsSubscription)
        super.stop()
    }

    override fun getPeer(id: NodeId): Optional<Eth2Peer> {
        return peerManager.getPeer(id)
    }

    override fun streamPeers(): Stream<Eth2Peer> {
        return peerManager.streamPeers()
    }

    override fun getPeerCount(): Int {
        // TODO - look into keep separate collections for pending peers / validated peers so
        // we don't have to iterate over the peer list to get this count.
        return Math.toIntExact(streamPeers().count())
    }

    override fun subscribeConnect(subscriber: PeerConnectedSubscriber<Eth2Peer>): Long {
        return peerManager.subscribeConnect(subscriber)
    }

    override fun unsubscribeConnect(subscriptionId: Long) {
        peerManager.unsubscribeConnect(subscriptionId)
    }

    val beaconChainMethods: BeaconChainMethods
        get() = peerManager.beaconChainMethods

    @Synchronized
    override fun subscribeToAttestationSubnetId(subnetId: Int) {
        if (attestationGossipManager == null) {
            pendingSubnetSubscriptions.add(subnetId)
        } else {
            attestationGossipManager!!.subscribeToSubnetId(subnetId)
        }
    }

    @Synchronized
    override fun unsubscribeFromAttestationSubnetId(subnetId: Int) {
        if (attestationGossipManager == null) {
            pendingSubnetSubscriptions.remove(subnetId)
        } else {
            attestationGossipManager!!.unsubscribeFromSubnetId(subnetId)
        }
    }

    override fun setLongTermAttestationSubnetSubscriptions(subnetIndices: Iterable<Int>) {
        attestationSubnetService.updateSubscriptions(subnetIndices)
    }

    override fun getMetadata(): MetadataMessage {
        return peerManager.metadataMessage
    }

}
