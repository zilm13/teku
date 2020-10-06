package tech.pegasys.teku.networking.p2p

import com.google.common.base.Preconditions
import com.google.common.eventbus.EventBus
import io.libp2p.core.crypto.KEY_TYPE
import io.libp2p.core.crypto.generateKeyPair
import org.apache.logging.log4j.LogManager
import org.assertj.core.api.Assertions.assertThat
import org.hyperledger.besu.metrics.noop.NoOpMetricsSystem
import tech.pegasys.teku.datastructures.attestation.ProcessedAttestationListener
import tech.pegasys.teku.datastructures.attestation.ValidateableAttestation
import tech.pegasys.teku.datastructures.operations.Attestation
import tech.pegasys.teku.datastructures.operations.AttesterSlashing
import tech.pegasys.teku.datastructures.operations.ProposerSlashing
import tech.pegasys.teku.datastructures.operations.SignedVoluntaryExit
import tech.pegasys.teku.networking.eth2.AttestationSubnetService
import tech.pegasys.teku.networking.eth2.Eth2Network
import tech.pegasys.teku.networking.eth2.Eth2NetworkBuilder
import tech.pegasys.teku.networking.eth2.gossip.encoding.GossipEncoding
import tech.pegasys.teku.networking.eth2.gossip.topics.GossipedOperationConsumer
import tech.pegasys.teku.networking.eth2.gossip.topics.ProcessedAttestationSubscriptionProvider
import tech.pegasys.teku.networking.eth2.gossip.topics.VerifiedBlockAttestationsSubscriptionProvider
import tech.pegasys.teku.networking.eth2.peers.Eth2Peer
import tech.pegasys.teku.networking.eth2.peers.Eth2PeerManager
import tech.pegasys.teku.networking.eth2.rpc.core.encodings.RpcEncoding
import tech.pegasys.teku.networking.p2p.connection.ReputationManager
import tech.pegasys.teku.networking.p2p.connection.TargetPeerRange
import tech.pegasys.teku.networking.p2p.network.GossipConfig
import tech.pegasys.teku.networking.p2p.network.NetworkConfig
import tech.pegasys.teku.networking.p2p.network.PeerHandler
import tech.pegasys.teku.networking.p2p.network.WireLogsConfig
import tech.pegasys.teku.networking.p2p.rpc.RpcMethod
import tech.pegasys.teku.networking.discovery.VirtualDiscoveryNetwork
import tech.pegasys.teku.networking.p2p.libp2p.VirtualLibP2PNetwork
import tech.pegasys.teku.networking.p2p.peer.Peer
import tech.pegasys.teku.simulation.sync.DelayedExecutorAsyncRunner
import tech.pegasys.teku.simulation.sync.StubStorageQueryChannel
import tech.pegasys.teku.simulation.sync.StubTimeProvider
import tech.pegasys.teku.simulation.util.BeaconChainUtil
import tech.pegasys.teku.simulation.util.Waiter
import tech.pegasys.teku.statetransition.blockimport.VerifiedBlockOperationsListener
import tech.pegasys.teku.storage.api.StorageQueryChannel
import tech.pegasys.teku.storage.client.MemoryOnlyRecentChainData
import tech.pegasys.teku.storage.client.RecentChainData
import tech.pegasys.teku.util.async.AsyncRunner
import tech.pegasys.teku.util.config.Constants
import tech.pegasys.teku.util.events.Subscribers
import java.net.BindException
import java.security.SecureRandom
import java.time.Duration
import java.util.ArrayList
import java.util.Optional
import java.util.OptionalInt
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.function.Consumer
import java.util.function.Function
import java.util.stream.Collectors
import java.util.stream.Stream

class VirtualEth2NetworkFactory {
    private val networks: MutableList<Eth2Network> = ArrayList()
    fun builder(rnd: SecureRandom): Eth2P2PNetworkBuilder {
        return Eth2P2PNetworkBuilder(rnd)
    }

    fun stopAll() {
        networks.forEach(Consumer { obj: Eth2Network -> obj.stop() })
    }

    inner class Eth2P2PNetworkBuilder(private val rnd: SecureRandom) {
        protected var peers: MutableList<Eth2Network> = ArrayList()
        protected var asyncRunner: AsyncRunner? = null
        protected var eventBus: EventBus? = null
        protected var networkConfig: NetworkConfig? = null
        protected var recentChainData: RecentChainData? = null
        protected var gossipedAttestationConsumer: GossipedOperationConsumer<ValidateableAttestation>? = null
        protected var gossipedAttesterSlashingConsumer: GossipedOperationConsumer<AttesterSlashing>? = null
        protected var gossipedProposerSlashingConsumer: GossipedOperationConsumer<ProposerSlashing>? = null
        protected var gossipedVoluntaryExitConsumer: GossipedOperationConsumer<SignedVoluntaryExit>? = null
        protected var processedAttestationSubscriptionProvider: ProcessedAttestationSubscriptionProvider? = null
        protected var verifiedBlockAttestationsSubscriptionProvider: VerifiedBlockAttestationsSubscriptionProvider? = null
        protected var rpcMethodsModifier = Function<RpcMethod, Stream<RpcMethod>> { t: RpcMethod -> Stream.of(t) }
        protected var peerHandlers: MutableList<PeerHandler> = ArrayList()
        protected var rpcEncoding = RpcEncoding.SSZ_SNAPPY
        protected var gossipEncoding = GossipEncoding.SSZ_SNAPPY
        protected var eth2RpcPingInterval: Duration? = null
        protected var eth2RpcOutstandingPingThreshold: Int? = null
        protected var eth2StatusUpdateInterval: Duration? = null

        @Throws(Exception::class)
        fun startNetwork(): Eth2Network {
            setDefaults()
            val network = buildAndStartNetwork()
            networks.add(network)
            return network
        }

        @Throws(Exception::class)
        protected fun buildAndStartNetwork(): Eth2Network {
            var attempt = 1
            while (true) {
                if (networkConfig == null) {
                    networkConfig = generateConfig(rnd)
                }
                val network = buildNetwork(networkConfig!!)
                try {
                    network.start()[30, TimeUnit.SECONDS]
                    networks.add(network)
                    Waiter.waitFor { assertThat(network.peerCount).isEqualTo(peers.size) }
                    return network
                } catch (e: ExecutionException) {
                    if (e.cause is BindException) {
                        if (attempt > 10) {
                            throw RuntimeException("Failed to find a free port after multiple attempts", e)
                        }
                        LOG.info(
                                "Port conflict detected, retrying with a new port. Original message: {}",
                                e.message)
                        attempt++
                        network.stop()
                    } else {
                        throw e
                    }
                }
            }
        }

        protected fun buildNetwork(config: NetworkConfig): Eth2Network {
            run {

                // Setup eth2 handlers
                val historicalChainData: StorageQueryChannel = StubStorageQueryChannel()
                val attestationSubnetService = AttestationSubnetService()
                val eth2PeerManager = Eth2PeerManager.create(
                        asyncRunner,
                        recentChainData,
                        historicalChainData,
                        METRICS_SYSTEM,
                        attestationSubnetService,
                        rpcEncoding,
                        eth2RpcPingInterval,
                        eth2RpcOutstandingPingThreshold!!,
                        eth2StatusUpdateInterval)
                val rpcMethods = eth2PeerManager.beaconChainMethods.all().stream()
                        .flatMap(rpcMethodsModifier)
                        .collect(Collectors.toList())
                this.peerHandler(eth2PeerManager)
                val reputationManager = ReputationManager(
                        StubTimeProvider.withTimeInSeconds(1000), Constants.REPUTATION_MANAGER_CAPACITY)
                val network: VirtualDiscoveryNetwork<Peer> = VirtualDiscoveryNetwork.create<Peer>(
                        asyncRunner,
                        VirtualLibP2PNetwork(
                                asyncRunner!!,
                                config,
                                reputationManager,
                                METRICS_SYSTEM,
                                ArrayList(rpcMethods),
                                peerHandlers),
                        reputationManager,
                        config)
                return VirtualActiveEth2Network(
                        network,
                        eth2PeerManager,
                        eventBus!!,
                        recentChainData!!,
                        gossipEncoding,
                        attestationSubnetService,
                        gossipedAttestationConsumer!!,
                        gossipedAttesterSlashingConsumer!!,
                        gossipedProposerSlashingConsumer!!,
                        gossipedVoluntaryExitConsumer!!,
                        processedAttestationSubscriptionProvider!!,
                        verifiedBlockAttestationsSubscriptionProvider!!)
            }
        }

        private fun generateConfig(random: SecureRandom): NetworkConfig {
            val peerAddresses = peers.stream().map { obj: Eth2Network -> obj.nodeAddress }.collect(Collectors.toList())
            val port = MIN_PORT + random.nextInt(MAX_PORT - MIN_PORT)
            return NetworkConfig(
                    generateKeyPair(KEY_TYPE.SECP256K1).component1(),
                    "127.0.0.1",
                    Optional.empty(),
                    port,
                    OptionalInt.empty(),
                    peerAddresses,
                    false,
                    emptyList(),
                    TargetPeerRange(20, 30),
                    GossipConfig.DEFAULT_CONFIG,
                    WireLogsConfig(false, false, true, false))
        }

        private fun setDefaults() {
            if (eventBus == null) {
                eventBus = EventBus()
            }
            if (asyncRunner == null) {
                asyncRunner = DelayedExecutorAsyncRunner.create()
            }
            if (eth2RpcPingInterval == null) {
                eth2RpcPingInterval = Eth2NetworkBuilder.DEFAULT_ETH2_RPC_PING_INTERVAL
            }
            if (eth2StatusUpdateInterval == null) {
                eth2StatusUpdateInterval = Eth2NetworkBuilder.DEFAULT_ETH2_STATUS_UPDATE_INTERVAL
            }
            if (eth2RpcOutstandingPingThreshold == null) {
                eth2RpcOutstandingPingThreshold = Eth2NetworkBuilder.DEFAULT_ETH2_RPC_OUTSTANDING_PING_THRESHOLD
            }
            if (recentChainData == null) {
                recentChainData = MemoryOnlyRecentChainData.create(eventBus!!)
                BeaconChainUtil.create(0, recentChainData!!).initializeStorage()
            }
            if (processedAttestationSubscriptionProvider == null) {
                val subscribers: Subscribers<ProcessedAttestationListener> = Subscribers.create(false)
                processedAttestationSubscriptionProvider = ProcessedAttestationSubscriptionProvider { subscriber: ProcessedAttestationListener -> subscribers.subscribe(subscriber) }
            }
            if (verifiedBlockAttestationsSubscriptionProvider == null) {
                val subscribers: Subscribers<VerifiedBlockOperationsListener<Attestation>> = Subscribers.create(false)
                verifiedBlockAttestationsSubscriptionProvider = VerifiedBlockAttestationsSubscriptionProvider { subscriber: VerifiedBlockOperationsListener<Attestation> -> subscribers.subscribe(subscriber) }
            }
            if (gossipedAttestationConsumer == null) {
                gossipedAttestationConsumer = GossipedOperationConsumer.noop()
            }
            if (gossipedAttesterSlashingConsumer == null) {
                gossipedAttesterSlashingConsumer = GossipedOperationConsumer.noop()
            }
            if (gossipedProposerSlashingConsumer == null) {
                gossipedProposerSlashingConsumer = GossipedOperationConsumer.noop()
            }
            if (gossipedVoluntaryExitConsumer == null) {
                gossipedVoluntaryExitConsumer = GossipedOperationConsumer.noop()
            }
        }

        fun rpcEncoding(rpcEncoding: RpcEncoding): Eth2P2PNetworkBuilder {
            Preconditions.checkNotNull(rpcEncoding)
            this.rpcEncoding = rpcEncoding
            return this
        }

        fun gossipEncoding(gossipEncoding: GossipEncoding): Eth2P2PNetworkBuilder {
            Preconditions.checkNotNull(gossipEncoding)
            this.gossipEncoding = gossipEncoding
            return this
        }

        fun peer(peer: Eth2Network): Eth2P2PNetworkBuilder {
            peers.add(peer)
            return this
        }

        fun eventBus(eventBus: EventBus): Eth2P2PNetworkBuilder {
            Preconditions.checkNotNull(eventBus)
            this.eventBus = eventBus
            return this
        }

        fun networkConfig(networkConfig: NetworkConfig?): Eth2P2PNetworkBuilder {
            Preconditions.checkNotNull(networkConfig)
            this.networkConfig = networkConfig!!
            return this
        }

        fun recentChainData(recentChainData: RecentChainData): Eth2P2PNetworkBuilder {
            Preconditions.checkNotNull(recentChainData)
            this.recentChainData = recentChainData
            return this
        }

        fun gossipedAttestationConsumer(
                gossipedAttestationConsumer: GossipedOperationConsumer<ValidateableAttestation>): Eth2P2PNetworkBuilder {
            Preconditions.checkNotNull(gossipedAttestationConsumer)
            this.gossipedAttestationConsumer = gossipedAttestationConsumer
            return this
        }

        fun gossipedAttesterSlashingConsumer(
                gossipedAttesterSlashingConsumer: GossipedOperationConsumer<AttesterSlashing>): Eth2P2PNetworkBuilder {
            Preconditions.checkNotNull(gossipedAttesterSlashingConsumer)
            this.gossipedAttesterSlashingConsumer = gossipedAttesterSlashingConsumer
            return this
        }

        fun gossipedProposerSlashingConsumer(
                gossipedProposerSlashingConsumer: GossipedOperationConsumer<ProposerSlashing>): Eth2P2PNetworkBuilder {
            Preconditions.checkNotNull(gossipedProposerSlashingConsumer)
            this.gossipedProposerSlashingConsumer = gossipedProposerSlashingConsumer
            return this
        }

        fun gossipedVoluntaryExitConsumer(
                gossipedVoluntaryExitConsumer: GossipedOperationConsumer<SignedVoluntaryExit>): Eth2P2PNetworkBuilder {
            Preconditions.checkNotNull(gossipedVoluntaryExitConsumer)
            this.gossipedVoluntaryExitConsumer = gossipedVoluntaryExitConsumer
            return this
        }

        fun processedAttestationSubscriptionProvider(
                processedAttestationSubscriptionProvider: ProcessedAttestationSubscriptionProvider): Eth2P2PNetworkBuilder {
            Preconditions.checkNotNull(processedAttestationSubscriptionProvider)
            this.processedAttestationSubscriptionProvider = processedAttestationSubscriptionProvider
            return this
        }

        fun verifiedBlockAttestationsSubscriptionProvider(
                verifiedBlockAttestationsSubscriptionProvider: VerifiedBlockAttestationsSubscriptionProvider): Eth2P2PNetworkBuilder {
            Preconditions.checkNotNull(verifiedBlockAttestationsSubscriptionProvider)
            this.verifiedBlockAttestationsSubscriptionProvider = verifiedBlockAttestationsSubscriptionProvider
            return this
        }

        fun rpcMethodsModifier(
                rpcMethodsModifier: Function<RpcMethod, Stream<RpcMethod>>): Eth2P2PNetworkBuilder {
            Preconditions.checkNotNull(rpcMethodsModifier)
            this.rpcMethodsModifier = rpcMethodsModifier
            return this
        }

        fun peerHandler(peerHandler: PeerHandler): Eth2P2PNetworkBuilder {
            Preconditions.checkNotNull(peerHandler)
            peerHandlers.add(peerHandler)
            return this
        }

        fun asyncRunner(asyncRunner: AsyncRunner): Eth2P2PNetworkBuilder {
            Preconditions.checkNotNull(asyncRunner)
            this.asyncRunner = asyncRunner
            return this
        }

        fun eth2RpcPingInterval(eth2RpcPingInterval: Duration): Eth2P2PNetworkBuilder {
            Preconditions.checkNotNull(eth2RpcPingInterval)
            this.eth2RpcPingInterval = eth2RpcPingInterval
            return this
        }

        fun eth2RpcOutstandingPingThreshold(
                eth2RpcOutstandingPingThreshold: Int): Eth2P2PNetworkBuilder {
            Preconditions.checkArgument(eth2RpcOutstandingPingThreshold > 0)
            this.eth2RpcOutstandingPingThreshold = eth2RpcOutstandingPingThreshold
            return this
        }

        fun eth2StatusUpdateInterval(eth2StatusUpdateInterval: Duration): Eth2P2PNetworkBuilder {
            Preconditions.checkNotNull(eth2StatusUpdateInterval)
            this.eth2StatusUpdateInterval = eth2StatusUpdateInterval
            return this
        }
    }

    companion object {
        protected val LOG = LogManager.getLogger()
        protected val METRICS_SYSTEM: NoOpMetricsSystem = NoOpMetricsSystem()
        private const val MIN_PORT = 6000
        private const val MAX_PORT = 9000
    }
}