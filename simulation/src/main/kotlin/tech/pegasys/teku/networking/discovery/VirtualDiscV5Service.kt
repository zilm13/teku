package tech.pegasys.teku.networking.discovery

import org.apache.tuweni.bytes.Bytes
import org.ethereum.beacon.discovery.DiscoverySystem
import org.ethereum.beacon.discovery.DiscoverySystemBuilder
import org.ethereum.beacon.discovery.schema.EnrField
import org.ethereum.beacon.discovery.schema.NodeRecord
import org.ethereum.beacon.discovery.schema.NodeRecordBuilder
import org.ethereum.beacon.discovery.schema.NodeRecordInfo
import org.ethereum.beacon.discovery.schema.NodeStatus
import tech.pegasys.teku.networking.p2p.discovery.DiscoveryPeer
import tech.pegasys.teku.networking.p2p.discovery.DiscoveryService
import tech.pegasys.teku.networking.p2p.libp2p.MultiaddrUtil
import tech.pegasys.teku.networking.p2p.network.NetworkConfig
import tech.pegasys.teku.service.serviceutils.Service
import tech.pegasys.teku.util.async.SafeFuture
import java.util.Optional
import java.util.stream.Stream

class VirtualDiscV5Service private constructor(private val discoverySystem: DiscoverySystem) : Service(), DiscoveryService {
    override fun doStart(): SafeFuture<*> {
        return SafeFuture.of(discoverySystem.start())
    }

    override fun doStop(): SafeFuture<*> {
        discoverySystem.stop()
        return SafeFuture.completedFuture<Any?>(null)
    }

    override fun streamKnownPeers(): Stream<DiscoveryPeer> {
        return activeNodes().map { nodeRecord: NodeRecord -> DiscoveryPeer(
                nodeRecord.get(EnrField.PKEY_SECP256K1) as Bytes,
                nodeRecord.tcpAddress.get(),
                Optional.ofNullable(nodeRecord.get("eth2") as Bytes))}
    }

    override fun searchForPeers(): SafeFuture<Void> {
        return SafeFuture.of(discoverySystem.searchForNewPeers())
    }

    override fun getEnr(): Optional<String> {
        return Optional.of(discoverySystem.localNodeRecord.asEnr())
    }

    override fun getDiscoveryAddress(): Optional<String> {
        val nodeRecord = discoverySystem.localNodeRecord
        if (nodeRecord.udpAddress.isEmpty) {
            return Optional.empty()
        }
        val discoveryPeer = DiscoveryPeer(
                nodeRecord[EnrField.PKEY_SECP256K1] as Bytes,
                nodeRecord.udpAddress.get(),
                Optional.empty())
        return Optional.of(MultiaddrUtil.fromDiscoveryPeerAsUdp(discoveryPeer).toString())
    }

    override fun updateCustomENRField(fieldName: String, value: Bytes) {
        discoverySystem.updateCustomFieldValue(fieldName, value)
    }

    private fun activeNodes(): Stream<NodeRecord> {
        return discoverySystem
                .streamKnownNodes()
                .filter { record: NodeRecordInfo -> record.status == NodeStatus.ACTIVE }
                .map { obj: NodeRecordInfo -> obj.node }
    }

    companion object {
        fun create(p2pConfig: NetworkConfig): DiscoveryService {
            val privateKey = Bytes.wrap(p2pConfig.privateKey.raw())
            val listenAddress = p2pConfig.networkInterface
            val listenPort = p2pConfig.listenPort
            val advertisedAddress = p2pConfig.advertisedIp
            val advertisedPort = p2pConfig.advertisedPort
            val bootnodes = p2pConfig.bootnodes
            val discoveryManager = DiscoverySystemBuilder()
                    .listen(listenAddress, listenPort)
                    .privateKey(privateKey)
                    .bootnodes(*bootnodes.toTypedArray())
                    .localNodeRecord(
                            NodeRecordBuilder()
                                    .privateKey(privateKey)
                                    .address(advertisedAddress, advertisedPort)
                                    .build())
                    .build()
            return VirtualDiscV5Service(discoveryManager)
        }
    }
}
