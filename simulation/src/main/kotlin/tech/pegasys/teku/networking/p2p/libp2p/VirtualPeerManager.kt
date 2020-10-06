package tech.pegasys.teku.networking.p2p.libp2p

import com.google.common.annotations.VisibleForTesting
import com.google.common.base.Throwables
import io.libp2p.core.Connection
import io.libp2p.core.ConnectionHandler
import io.libp2p.core.Network
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.util.Supplier
import org.hyperledger.besu.plugin.services.MetricsSystem
import tech.pegasys.teku.metrics.TekuMetricCategory
import tech.pegasys.teku.networking.p2p.connection.ReputationManager
import tech.pegasys.teku.networking.p2p.libp2p.rpc.RpcHandler
import tech.pegasys.teku.networking.p2p.network.PeerHandler
import tech.pegasys.teku.networking.p2p.peer.NodeId
import tech.pegasys.teku.networking.p2p.peer.Peer
import tech.pegasys.teku.networking.p2p.peer.PeerConnectedSubscriber
import tech.pegasys.teku.networking.p2p.rpc.RpcMethod
import tech.pegasys.teku.util.async.SafeFuture
import tech.pegasys.teku.util.events.Subscribers
import java.util.Optional
import java.util.concurrent.CompletionStage
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Consumer
import java.util.stream.Stream

class VirtualPeerManager(
        metricsSystem: MetricsSystem,
        private val reputationManager: ReputationManager,
        private val peerHandlers: List<PeerHandler>,
        private val rpcHandlers: Map<RpcMethod, RpcHandler>) : ConnectionHandler {
    private val connectedPeerMap = ConcurrentHashMap<NodeId, Peer>()
    private val pendingConnections = ConcurrentHashMap<NodeId, SafeFuture<Peer>>()
    private val connectSubscribers: Subscribers<PeerConnectedSubscriber<Peer>> = Subscribers.create(true)
    override fun handleConnection(connection: Connection) {
        val peer: Peer = LibP2PPeer(connection, rpcHandlers)
        onConnectedPeer(peer)
    }

    fun subscribeConnect(subscriber: PeerConnectedSubscriber<Peer>): Long {
        return connectSubscribers.subscribe(subscriber)
    }

    fun unsubscribeConnect(subscriptionId: Long) {
        connectSubscribers.unsubscribe(subscriptionId)
    }

    fun connect(peer: MultiaddrPeerAddress, network: Network): SafeFuture<Peer> {
        return pendingConnections.computeIfAbsent(peer.id) { doConnect(peer, network) }
    }

    private fun doConnect(peer: MultiaddrPeerAddress, network: Network): SafeFuture<Peer> {
        LOG.debug("Connecting to {}", peer)
        return SafeFuture.of { network.connect(peer.multiaddr) }
                .thenApply { connection: Connection ->
                    val nodeId = LibP2PNodeId(connection.secureSession().remoteId)
                    val connectedPeer = connectedPeerMap[nodeId]
                            ?: if (connection.closeFuture().isDone) {
                                // Connection has been immediately closed and the peer already removed
                                // Since the connection is closed anyway, we can create a new peer to wrap it.
                                return@thenApply LibP2PPeer(connection, rpcHandlers)
                            } else {
                                // Theoretically this should never happen because removing from the map is done
                                // by the close future completing, but make a loud noise just in case.
                                throw IllegalStateException(
                                        "No peer registered for established connection to $nodeId")
                            }
                    reputationManager.reportInitiatedConnectionSuccessful(peer)
                    connectedPeer
                }
                .exceptionallyCompose { error: Throwable -> handleConcurrentConnectionInitiation(error) }
                .catchAndRethrow { reputationManager.reportInitiatedConnectionFailed(peer) }
                .whenComplete { _: Peer, _: Throwable -> pendingConnections.remove(peer.id) }
    }

    private fun handleConcurrentConnectionInitiation(error: Throwable): CompletionStage<Peer> {
        val rootCause = Throwables.getRootCause(error)
        return if (rootCause is PeerAlreadyConnectedException) SafeFuture.completedFuture(rootCause.peer) else SafeFuture.failedFuture(error)
    }

    fun getPeer(id: NodeId): Optional<Peer> {
        return Optional.ofNullable(connectedPeerMap[id])
    }

    @VisibleForTesting
    fun onConnectedPeer(peer: Peer) {
        val wasAdded = connectedPeerMap.putIfAbsent(peer.id, peer) == null
        if (wasAdded) {
            LOG.debug("onConnectedPeer() {}", peer.id)
            peerHandlers.forEach(Consumer { h: PeerHandler -> h.onConnect(peer) })
            connectSubscribers.forEach { c: PeerConnectedSubscriber<Peer> -> c.onConnected(peer) }
            peer.subscribeDisconnect { onDisconnectedPeer(peer) }
        } else {
            LOG.trace("Disconnecting duplicate connection to {}", Supplier<Any> { peer.id })
            peer.disconnectImmediately()
            throw PeerAlreadyConnectedException(peer)
        }
    }

    @VisibleForTesting
    fun onDisconnectedPeer(peer: Peer) {
        if (connectedPeerMap.remove(peer.id) != null) {
            LOG.debug("Peer disconnected: {}", peer.id)
            peerHandlers.forEach(Consumer { h: PeerHandler -> h.onDisconnect(peer) })
        }
    }

    fun streamPeers(): Stream<Peer> {
        return connectedPeerMap.values.stream()
    }

    val peerCount: Int
        get() = connectedPeerMap.size

    companion object {
        private val LOG = LogManager.getLogger()
    }

    init {
        metricsSystem.createGauge(
                TekuMetricCategory.LIBP2P, "peers", "Tracks number of libp2p peers") { peerCount.toDouble() }
    }
}
