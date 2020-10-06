package tech.pegasys.teku.simulation

import com.google.common.eventbus.AsyncEventBus
import com.google.common.eventbus.EventBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch
import org.hyperledger.besu.plugin.services.MetricsSystem
import tech.pegasys.teku.TekuDefaultExceptionHandler
import tech.pegasys.teku.datastructures.attestation.ProcessedAttestationListener
import tech.pegasys.teku.datastructures.attestation.ValidateableAttestation
import tech.pegasys.teku.datastructures.blocks.SignedBeaconBlock
import tech.pegasys.teku.datastructures.operations.Attestation
import tech.pegasys.teku.datastructures.operations.AttesterSlashing
import tech.pegasys.teku.datastructures.operations.ProposerSlashing
import tech.pegasys.teku.datastructures.operations.SignedVoluntaryExit
import tech.pegasys.teku.events.EventChannels
import tech.pegasys.teku.networking.discovery.DiscoveryEvent
import tech.pegasys.teku.networking.discovery.VirtualDiscV5Service
import tech.pegasys.teku.networking.discovery.VirtualDiscoveryManager
import tech.pegasys.teku.networking.discovery.VirtualDiscoveryNetwork
import tech.pegasys.teku.networking.eth2.AttestationSubnetService
import tech.pegasys.teku.networking.eth2.Eth2Network
import tech.pegasys.teku.networking.eth2.gossip.encoding.GossipEncoding
import tech.pegasys.teku.networking.eth2.gossip.topics.GossipedOperationConsumer
import tech.pegasys.teku.networking.eth2.gossip.topics.ProcessedAttestationSubscriptionProvider
import tech.pegasys.teku.networking.eth2.gossip.topics.VerifiedBlockAttestationsSubscriptionProvider
import tech.pegasys.teku.networking.eth2.peers.Eth2PeerManager
import tech.pegasys.teku.networking.eth2.rpc.core.encodings.RpcEncoding
import tech.pegasys.teku.networking.p2p.VirtualActiveEth2Network
import tech.pegasys.teku.networking.p2p.connection.ConnectionManager
import tech.pegasys.teku.networking.p2p.connection.ReputationManager
import tech.pegasys.teku.networking.p2p.libp2p.VirtualLibP2PNetwork
import tech.pegasys.teku.networking.p2p.network.NetworkConfig
import tech.pegasys.teku.networking.p2p.peer.Peer
import tech.pegasys.teku.service.serviceutils.AsyncRunnerFactory
import tech.pegasys.teku.service.serviceutils.ServiceConfig
import tech.pegasys.teku.services.beaconchain.SlotProcessor
import tech.pegasys.teku.simulation.sync.StubStorageQueryChannel
import tech.pegasys.teku.simulation.sync.StubTimeProvider
import tech.pegasys.teku.simulation.sync.SyncingNodeManager
import tech.pegasys.teku.statetransition.blockimport.BlockImporter
import tech.pegasys.teku.statetransition.blockimport.VerifiedBlockOperationsListener
import tech.pegasys.teku.statetransition.util.FutureItems
import tech.pegasys.teku.statetransition.util.PendingPool
import tech.pegasys.teku.storage.api.StorageQueryChannel
import tech.pegasys.teku.storage.client.RecentChainData
import tech.pegasys.teku.sync.BlockManager
import tech.pegasys.teku.sync.DefaultSyncService
import tech.pegasys.teku.sync.FetchRecentBlocksService
import tech.pegasys.teku.sync.SyncManager
import tech.pegasys.teku.util.async.AsyncRunner
import tech.pegasys.teku.util.async.SafeFuture
import tech.pegasys.teku.util.config.Constants
import tech.pegasys.teku.util.config.TekuConfigurationBuilder
import tech.pegasys.teku.util.events.Subscribers
import tech.pegasys.teku.util.time.SystemTimeProvider
import java.time.Duration
import java.util.concurrent.ThreadPoolExecutor
import java.util.stream.Collectors
import kotlin.coroutines.CoroutineContext

class VirtualPeer(
        private val syncManager: SyncingNodeManager,
        private val discoveryBus: SendChannel<DiscoveryEvent>,
        val networkConfig: NetworkConfig,
        private val metricsSystem: MetricsSystem,
        private val asyncRunnerFactory: AsyncRunnerFactory, // FIXME: too much resources
        private val asyncRunner: AsyncRunner,
        private val threadPool: ThreadPoolExecutor) {
    private val p2pNetwork: Eth2Network
    private val eventBus: EventBus? = null
    private val blockImporter: BlockImporter? = null
    private val recentChainData: RecentChainData? = null
    private val slotProcessor: SlotProcessor? = null
    private var discoveryManager: VirtualDiscoveryManager? = null

    init {
        val pendingBlocks = PendingPool.createForBlocks()
        val futureBlocks = FutureItems { obj: SignedBeaconBlock -> obj.slot }
        val subscriberExceptionHandler = TekuDefaultExceptionHandler()
        val eventBus: EventBus = AsyncEventBus(threadPool, subscriberExceptionHandler)
        val eventChannels = EventChannels(subscriberExceptionHandler, metricsSystem)
        val serviceConfig = ServiceConfig(
                asyncRunnerFactory,
                SystemTimeProvider(),
                eventBus,
                eventChannels,
                metricsSystem,
                TekuConfigurationBuilder().build()
        )
        val reputationManager = ReputationManager(StubTimeProvider.withTimeInMillis(1000), Constants.REPUTATION_MANAGER_CAPACITY)
        val discoveryService = VirtualDiscV5Service.create(networkConfig)
        val libP2PNetwork = VirtualLibP2PNetwork(
                asyncRunner, networkConfig, reputationManager, metricsSystem, emptyList(), emptyList())
        val connectionManager = ConnectionManager(
                discoveryService,
                reputationManager,
                asyncRunner,
                libP2PNetwork,
                networkConfig.staticPeers.stream()
                        .map { peerAddress: String -> libP2PNetwork.createPeerAddress(peerAddress) }
                        .collect(Collectors.toList()),
                networkConfig.targetPeerRange)
        val discoveryNetwork = VirtualDiscoveryNetwork(libP2PNetwork, discoveryService, connectionManager)
        val historicalChainData: StorageQueryChannel = StubStorageQueryChannel()
        val eth2PeerManager = Eth2PeerManager.create(
                asyncRunner,
                recentChainData,
                historicalChainData,
                metricsSystem,
                AttestationSubnetService(),
                RpcEncoding.SSZ,
                Duration.ofMillis(1000),
                5,
                Duration.ofMillis(5000))

        val attestationSubnetService = AttestationSubnetService()
        val gossipedAttestationConsumer = GossipedOperationConsumer.noop<ValidateableAttestation>()
        val gossipedAttesterSlashingConsumer = GossipedOperationConsumer.noop<AttesterSlashing>()
        val gossipedProposerSlashingConsumer = GossipedOperationConsumer.noop<ProposerSlashing>()
        val gossipedVoluntaryExitConsumer = GossipedOperationConsumer.noop<SignedVoluntaryExit>()
        val attestationSubscribers: Subscribers<ProcessedAttestationListener> = Subscribers.create(false)
        val processedAttestationSubscriptionProvider = ProcessedAttestationSubscriptionProvider { subscriber: ProcessedAttestationListener -> attestationSubscribers.subscribe(subscriber) }
        val blockSubscribers: Subscribers<VerifiedBlockOperationsListener<Attestation>> = Subscribers.create(false)
        val verifiedBlockAttestationsSubscriptionProvider = VerifiedBlockAttestationsSubscriptionProvider { subscriber: VerifiedBlockOperationsListener<Attestation> -> blockSubscribers.subscribe(subscriber) }

        p2pNetwork = VirtualActiveEth2Network(discoveryNetwork, eth2PeerManager, eventBus, recentChainData!!, GossipEncoding.SSZ,
                attestationSubnetService, gossipedAttestationConsumer, gossipedAttesterSlashingConsumer, gossipedProposerSlashingConsumer,
                gossipedVoluntaryExitConsumer, processedAttestationSubscriptionProvider, verifiedBlockAttestationsSubscriptionProvider)
        val recentBlockFetcher = FetchRecentBlocksService.create(asyncRunner, p2pNetwork, pendingBlocks)
        val blockManager = BlockManager.create(
                eventBus,
                pendingBlocks,
                futureBlocks,
                recentBlockFetcher,
                recentChainData,
                blockImporter)
        val syncManager = SyncManager.create(asyncRunner, p2pNetwork, recentChainData, blockImporter)
        val syncService = DefaultSyncService(blockManager, syncManager, recentChainData)
        val slotProcessor = SlotProcessor(
                recentChainData, syncService, p2pNetwork, blockManager, eventBus)
    }

    fun setDiscoveryManager(discoveryManager: VirtualDiscoveryManager) {
        this.discoveryManager = discoveryManager
    }

    fun connect(peer: VirtualPeer): SafeFuture<Peer> {
        return syncManager.connect(peer.syncManager)
    }

    fun onSyncDone(): SafeFuture<Void> {
        return syncManager.onSyncDone()
    }

    suspend fun publishOnDiscovery(event: DiscoveryEvent) {
        if (!discoveryBus.isClosedForSend) {
            discoveryBus.send(event)
        }
    }

    fun listenOnDiscovery(event: DiscoveryEvent, scope: CoroutineScope) {
        scope.launch {
            if (event.to == networkConfig.address) {
                discoveryManager!!.putIncomingDiscovery(event)
            }
        }
    }

    @ExperimentalUnsignedTypes
    fun generateBlocks(blockCount: ULong) {
        (1..blockCount.toLong()).forEach { syncManager.chainUtil().createAndImportBlockAtSlot(it) }
    }
}