package tech.pegasys.teku.networking.discovery

import com.google.common.primitives.UnsignedLong
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.apache.tuweni.bytes.Bytes
import org.apache.tuweni.bytes.Bytes32
import tech.pegasys.teku.datastructures.networking.libp2p.rpc.EnrForkId
import tech.pegasys.teku.datastructures.state.Fork
import tech.pegasys.teku.datastructures.state.ForkInfo
import tech.pegasys.teku.datastructures.util.BeaconStateUtil
import tech.pegasys.teku.datastructures.util.SimpleOffsetSerializer
import tech.pegasys.teku.logging.StatusLogger
import tech.pegasys.teku.networking.eth2.peers.Eth2Peer
import tech.pegasys.teku.networking.p2p.connection.ConnectionManager
import tech.pegasys.teku.networking.p2p.connection.ReputationManager
import tech.pegasys.teku.networking.p2p.discovery.DiscoveryPeer
import tech.pegasys.teku.networking.p2p.discovery.DiscoveryService
import tech.pegasys.teku.networking.p2p.discovery.discv5.DiscV5Service
import tech.pegasys.teku.networking.p2p.discovery.noop.NoOpDiscoveryService
import tech.pegasys.teku.networking.p2p.network.DelegatingP2PNetwork
import tech.pegasys.teku.networking.p2p.network.NetworkConfig
import tech.pegasys.teku.networking.p2p.network.P2PNetwork
import tech.pegasys.teku.networking.p2p.peer.NodeId
import tech.pegasys.teku.networking.p2p.peer.Peer
import tech.pegasys.teku.networking.p2p.peer.PeerConnectedSubscriber
import tech.pegasys.teku.networking.p2p.libp2p.VirtualLibP2PNetwork
import tech.pegasys.teku.ssz.SSZTypes.Bitvector
import tech.pegasys.teku.ssz.SSZTypes.Bytes4
import tech.pegasys.teku.util.async.AsyncRunner
import tech.pegasys.teku.util.async.SafeFuture
import tech.pegasys.teku.util.config.Constants
import java.util.Optional
import java.util.stream.Collectors
import java.util.stream.Stream

class VirtualDiscoveryNetwork<P : Peer> internal constructor(
        private val p2pNetwork: P2PNetwork<P>,
        private val discoveryService: DiscoveryService,
        private val connectionManager: ConnectionManager) : DelegatingP2PNetwork<P>(p2pNetwork) {
    @Volatile
    private var enrForkId: Optional<EnrForkId> = Optional.empty()

    init {
        setPreGenesisForkInfo()
        enr.ifPresent { enr: String? -> StatusLogger.STATUS_LOG.listeningForDiscv5PreGenesis(enr) }

        // Set connection manager peer predicate so that we don't attempt to connect peers with
        // different fork digests
        connectionManager.addPeerPredicate({ peer: DiscoveryPeer -> dontConnectPeersWithDifferentForkDigests(peer) })
    }

    override fun start(): SafeFuture<Void>? {
        return SafeFuture.allOfFailFast<Void>(p2pNetwork.start(), discoveryService.start())
                .thenCompose { connectionManager.start() as SafeFuture<Void> }
                .thenRun { enr.ifPresent { enr: String? -> StatusLogger.STATUS_LOG.listeningForDiscv5(enr) } }
    }

    override fun stop() {
        connectionManager
                .stop()
                .exceptionally { error: Throwable? ->
                    LOG.error("Failed to stop connection manager", error)
                    null
                }
                .always {
                    p2pNetwork.stop()
                    discoveryService.stop().reportExceptions()
                }
    }

    fun addStaticPeer(peerAddress: String?) {
        connectionManager.addStaticPeer(p2pNetwork.createPeerAddress(peerAddress))
    }

    override fun getEnr(): Optional<String?> {
        return discoveryService.enr
    }

    override fun getDiscoveryAddress(): Optional<String?>? {
        return discoveryService.discoveryAddress
    }

    fun setLongTermAttestationSubnetSubscriptions(subnetIds: Iterable<Int?>?) {
        discoveryService.updateCustomENRField(
                ATTESTATION_SUBNET_ENR_FIELD,
                Bitvector(subnetIds, Constants.ATTESTATION_SUBNET_COUNT).serialize())
    }

    fun setPreGenesisForkInfo() {
        val enrForkId = EnrForkId(
                BeaconStateUtil.compute_fork_digest(Constants.GENESIS_FORK_VERSION, Bytes32.ZERO),
                Constants.GENESIS_FORK_VERSION,
                Constants.FAR_FUTURE_EPOCH)
        discoveryService.updateCustomENRField(
                ETH2_ENR_FIELD, SimpleOffsetSerializer.serialize(enrForkId))
        this.enrForkId = Optional.of(enrForkId)
    }

    fun setForkInfo(currentForkInfo: ForkInfo, nextForkInfo: Optional<Fork>) {
        // If no future fork is planned, set next_fork_version = current_fork_version to signal this
        val nextVersion: Bytes4 = nextForkInfo
                .map(Fork::getCurrent_version)
                .orElse(currentForkInfo.fork.current_version)
        // If no future fork is planned, set next_fork_epoch = FAR_FUTURE_EPOCH to signal this
        val nextForkEpoch: UnsignedLong = nextForkInfo.map(Fork::getEpoch).orElse(Constants.FAR_FUTURE_EPOCH)
        val forkDigest: Bytes4 = currentForkInfo.forkDigest
        val enrForkId = EnrForkId(forkDigest, nextVersion, nextForkEpoch)
        val encodedEnrForkId: Bytes = SimpleOffsetSerializer.serialize(enrForkId)
        discoveryService.updateCustomENRField(ETH2_ENR_FIELD, encodedEnrForkId)
        this.enrForkId = Optional.of(enrForkId)
    }

    private fun dontConnectPeersWithDifferentForkDigests(peer: DiscoveryPeer): Boolean {
        return enrForkId
                .map { obj: EnrForkId -> obj.getForkDigest() }
                .flatMap { forkDigest: Bytes4 ->
                    peer.enrForkId
                            .map { peerEnrForkId: Bytes? ->
                                (SimpleOffsetSerializer.deserialize(peerEnrForkId, EnrForkId::class.java)
                                        .forkDigest
                                        == forkDigest)
                            }
                }
                .orElse(false)
    }

    override fun subscribeConnect(subscriber: PeerConnectedSubscriber<P>?): Long {
        return p2pNetwork.subscribeConnect(subscriber)
    }

    override fun unsubscribeConnect(subscriptionId: Long) {
        p2pNetwork.unsubscribeConnect(subscriptionId)
    }

    override fun getPeer(id: NodeId?): Optional<P>? {
        return p2pNetwork.getPeer(id)
    }

    override fun streamPeers(): Stream<P>? {
        return p2pNetwork.streamPeers()
    }

    companion object {
        private const val ATTESTATION_SUBNET_ENR_FIELD: String = "attnets"
        private const val ETH2_ENR_FIELD: String = "eth2"
        private val LOG: Logger = LogManager.getLogger()

        fun <P : Peer> create(
                asyncRunner: AsyncRunner?,
                p2pNetwork: VirtualLibP2PNetwork,
                reputationManager: ReputationManager?,
                p2pConfig: NetworkConfig): VirtualDiscoveryNetwork<Peer> {
            val discoveryService: DiscoveryService = createDiscoveryService(p2pConfig)
            val connectionManager = ConnectionManager(
                    discoveryService,
                    reputationManager,
                    asyncRunner,
                    p2pNetwork,
                    p2pConfig.staticPeers.stream()
                            .map { peerAddress: String -> p2pNetwork.createPeerAddress(peerAddress) }
                            .collect(Collectors.toList()),
                    p2pConfig.targetPeerRange)
            return VirtualDiscoveryNetwork(p2pNetwork, discoveryService, connectionManager)
        }

        private fun createDiscoveryService(p2pConfig: NetworkConfig): DiscoveryService {
            return if (p2pConfig.isDiscoveryEnabled) {
                DiscV5Service.create(p2pConfig)
            } else {
                NoOpDiscoveryService()
            }
        }
    }
}