package tech.pegasys.teku.networking.p2p.libp2p

import identify.pb.IdentifyOuterClass
import io.libp2p.core.Host
import io.libp2p.core.PeerId
import io.libp2p.core.crypto.PrivKey
import io.libp2p.core.dsl.host
import io.libp2p.core.multiformats.Multiaddr
import io.libp2p.core.multistream.ProtocolBinding
import io.libp2p.core.pubsub.createPubsubApi
import io.libp2p.etc.types.toProtobuf
import io.libp2p.mux.mplex.MplexStreamMuxer
import io.libp2p.protocol.Identify
import io.libp2p.protocol.Ping
import io.libp2p.pubsub.gossip.Gossip
import io.libp2p.pubsub.gossip.GossipParams
import io.libp2p.pubsub.gossip.GossipRouter
import io.libp2p.security.noise.NoiseXXSecureChannel
import io.libp2p.security.secio.SecIoSecureChannel
import io.libp2p.transport.tcp.TcpTransport
import io.netty.channel.ChannelHandler
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler
import org.apache.logging.log4j.LogManager
import org.apache.tuweni.crypto.Hash
import org.hyperledger.besu.plugin.services.MetricsSystem
import pubsub.pb.Rpc
import tech.pegasys.teku.logging.StatusLogger
import tech.pegasys.teku.networking.p2p.connection.ReputationManager
import tech.pegasys.teku.networking.p2p.discovery.DiscoveryPeer
import tech.pegasys.teku.networking.p2p.gossip.GossipNetwork
import tech.pegasys.teku.networking.p2p.gossip.TopicChannel
import tech.pegasys.teku.networking.p2p.gossip.TopicHandler
import tech.pegasys.teku.networking.p2p.libp2p.gossip.LibP2PGossipNetwork
import tech.pegasys.teku.networking.p2p.libp2p.rpc.RpcHandler
import tech.pegasys.teku.networking.p2p.network.NetworkConfig
import tech.pegasys.teku.networking.p2p.network.P2PNetwork
import tech.pegasys.teku.networking.p2p.network.PeerAddress
import tech.pegasys.teku.networking.p2p.network.PeerHandler
import tech.pegasys.teku.networking.p2p.peer.NodeId
import tech.pegasys.teku.networking.p2p.peer.Peer
import tech.pegasys.teku.networking.p2p.peer.PeerConnectedSubscriber
import tech.pegasys.teku.networking.p2p.rpc.RpcMethod
import tech.pegasys.teku.util.async.AsyncRunner
import tech.pegasys.teku.util.async.SafeFuture
import tech.pegasys.teku.util.cli.VersionProvider
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.UnknownHostException
import java.util.Base64
import java.util.Optional
import java.util.Random
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference
import java.util.function.Consumer
import java.util.stream.Stream


class VirtualLibP2PNetwork(
        asyncRunner: AsyncRunner,
        private val config: NetworkConfig,
        reputationManager: ReputationManager,
        metricsSystem: MetricsSystem,
        rpcMethods: List<RpcMethod>,
        peerHandlers: List<PeerHandler>): P2PNetwork<Peer> {

    private val LOG = LogManager.getLogger()

    private var privKey: PrivKey
    private var nodeId: NodeId

    private var host: Host
    private var peerManager: VirtualPeerManager
    private var advertisedAddr: Multiaddr
    private var gossip: Gossip
    private var gossipNetwork: GossipNetwork

    private val state = AtomicReference(P2PNetwork.State.IDLE)
    private val rpcHandlers: MutableMap<RpcMethod, RpcHandler> = ConcurrentHashMap()
    private var listenPort = 0

    init {
        privKey = config.privateKey
        nodeId = LibP2PNodeId(PeerId.fromPubKey(privKey.publicKey()))
        advertisedAddr = getAdvertisedAddr(config, nodeId)
        listenPort = config.listenPort

        // Setup gossip
        gossip = createGossip()
        val publisher = gossip.createPublisher(privKey, Random().nextLong())
        gossipNetwork = LibP2PGossipNetwork(gossip, publisher)

        // Setup rpc methods
        rpcMethods.forEach(Consumer { method: RpcMethod -> rpcHandlers[method] = RpcHandler(asyncRunner, method) })

        // Setup peers
        peerManager = VirtualPeerManager(metricsSystem, reputationManager, peerHandlers, rpcHandlers)

        // temporary flag
        // set true for Lighthouse compatibility
        // set false for Prysm compatibility
        // TODO remove when all clients adjust the same Noise spec
        NoiseXXSecureChannel.rustInteroperability = false
        val listenAddr = MultiaddrUtil.fromInetSocketAddress(
                InetSocketAddress(config.networkInterface, config.listenPort))
        host = host {
            identity {
                run { privKey }
            }
            transports {
                +::TcpTransport
            }
            secureChannels {
                add { NoiseXXSecureChannel(privKey) }
                add { SecIoSecureChannel(privKey) } // to be removed later
            }
            muxers {
                add { MplexStreamMuxer() }
            }
            network {
                listen(listenAddr.toString()) // TODO: don't listen
            }
            protocols {
                addAll(getDefaultProtocols())
                addAll(rpcHandlers.values)
            }
            connectionHandlers {
                add(peerManager)
            }
        }
        if (config.wireLogsConfig.isLogWireCipher) {
            host {
                debug {
                    beforeSecureHandler.setLogger(LogLevel.DEBUG, "wire.ciphered")
                }
            }
        }
        if (config.wireLogsConfig.isLogWirePlain) {
            host {
                debug {
                    afterSecureHandler.setLogger(LogLevel.DEBUG, "wire.plain")
                }
            }
        }
        if (config.wireLogsConfig.isLogWireMuxFrames) {
            host {
                debug {
                    muxFramesHandler.setLogger(LogLevel.DEBUG, "wire.mux")
                }
            }
        }
    }

    private fun createGossip(): Gossip {
        val gossipParams = GossipParams.builder()
                .D(config.gossipConfig.d)
                .DLow(config.gossipConfig.dLow)
                .DHigh(config.gossipConfig.dHigh)
                .DLazy(config.gossipConfig.dLazy)
                .fanoutTTL(config.gossipConfig.fanoutTTL)
                .gossipSize(config.gossipConfig.advertise)
                .gossipHistoryLength(config.gossipConfig.history)
                .heartbeatInterval(config.gossipConfig.heartbeatInterval)
                .build()
        val router = GossipRouter(gossipParams)
        router.messageIdGenerator = { msg: Rpc.Message ->
            Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(Hash.sha2_256(msg.data.toByteArray()))
        }
        val debugHandler: ChannelHandler? = if (config.wireLogsConfig.isLogWireGossip) LoggingHandler("wire.gossip", LogLevel.DEBUG) else null
        val pubsubApi = createPubsubApi(router)
        return Gossip(router, pubsubApi, debugHandler)
    }

    private fun getDefaultProtocols(): Collection<ProtocolBinding<Any>> {
        val ping = Ping()
        val identifyMsg = IdentifyOuterClass.Identify.newBuilder()
                .setProtocolVersion("ipfs/0.1.0")
                .setAgentVersion(VersionProvider.CLIENT_IDENTITY + "/" + VersionProvider.VERSION)
                .setPublicKey(privKey.publicKey().bytes().toProtobuf())
                .addListenAddrs(advertisedAddr.getBytes().toProtobuf())
                .setObservedAddr(
                        // TODO: Report external IP
                        advertisedAddr.getBytes().toProtobuf())
                .addAllProtocols(ping.protocolDescriptor.announceProtocols)
                .addAllProtocols(gossip.protocolDescriptor.announceProtocols)
                .build()
        return java.util.List.of(ping, Identify(identifyMsg), gossip)
    }

    override fun start(): SafeFuture<*> {
        if (!state.compareAndSet(P2PNetwork.State.IDLE, P2PNetwork.State.RUNNING)) {
            return SafeFuture.failedFuture<Any>(IllegalStateException("Network already started"))
        }
        LOG.info("Starting libp2p network...")
        return SafeFuture.of(host.start())
                .thenApply<Any> { i: Void ->
                    StatusLogger.STATUS_LOG.listeningForLibP2P(nodeAddress)
                    null
                }
    }

    private fun getAdvertisedAddr(config: NetworkConfig, nodeId: NodeId): Multiaddr {
        return try {
            val advertisedAddress = InetSocketAddress(config.advertisedIp, config.advertisedPort)
            val resolvedAddress: InetSocketAddress
            resolvedAddress = if (advertisedAddress.address.isAnyLocalAddress) {
                InetSocketAddress(InetAddress.getLocalHost(), advertisedAddress.port)
            } else {
                advertisedAddress
            }
            MultiaddrUtil.fromInetSocketAddress(resolvedAddress, nodeId)
        } catch (err: UnknownHostException) {
            throw RuntimeException(
                    "Unable to start LibP2PNetwork due to failed attempt at obtaining host address", err)
        }
    }

    override fun getNodeAddress(): String {
        return advertisedAddr.toString()
    }

    override fun connect(peer: PeerAddress): SafeFuture<Peer> {
        return peer.`as`(MultiaddrPeerAddress::class.java)
                .map { staticPeer: MultiaddrPeerAddress -> peerManager.connect(staticPeer, host.network) }
                .orElseGet {
                    SafeFuture.failedFuture(
                            IllegalArgumentException(
                                    "Unsupported peer address: " + peer.javaClass.name))
                }
    }

    override fun createPeerAddress(peerAddress: String): PeerAddress {
        return MultiaddrPeerAddress.fromAddress(peerAddress)
    }

    override fun createPeerAddress(discoveryPeer: DiscoveryPeer): PeerAddress {
        return MultiaddrPeerAddress.fromDiscoveryPeer(discoveryPeer)
    }

    override fun subscribeConnect(subscriber: PeerConnectedSubscriber<Peer>): Long {
        return peerManager.subscribeConnect(subscriber)
    }

    override fun unsubscribeConnect(subscriptionId: Long) {
        peerManager.unsubscribeConnect(subscriptionId)
    }

    override fun isConnected(peerAddress: PeerAddress): Boolean {
        return peerManager.getPeer(peerAddress.id).isPresent
    }

    override fun getPeer(id: NodeId): Optional<Peer> {
        return peerManager.getPeer(id)
    }

    override fun streamPeers(): Stream<Peer> {
        return peerManager.streamPeers()
    }

    override fun getPeerCount(): Int {
        return peerManager.peerCount
    }

    override fun getListenPort(): Int {
        return listenPort
    }

    override fun stop() {
        if (!state.compareAndSet(P2PNetwork.State.RUNNING, P2PNetwork.State.STOPPED)) {
            return
        }
        LOG.debug("JvmLibP2PNetwork.stop()")
        SafeFuture.reportExceptions(host.stop())
    }

    override fun getNodeId(): NodeId {
        return nodeId
    }

    override fun getEnr(): Optional<String> {
        return Optional.empty()
    }

    override fun getDiscoveryAddress(): Optional<String> {
        return Optional.empty()
    }

    override fun subscribe(topic: String, topicHandler: TopicHandler): TopicChannel {
        return gossipNetwork.subscribe(topic, topicHandler)
    }
}