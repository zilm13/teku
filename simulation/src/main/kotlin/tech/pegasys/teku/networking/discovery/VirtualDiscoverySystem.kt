package tech.pegasys.teku.networking.discovery

import org.apache.logging.log4j.LogManager
import org.apache.tuweni.bytes.Bytes
import org.apache.tuweni.bytes.Bytes32
import org.ethereum.beacon.discovery.DiscoveryManager
import org.ethereum.beacon.discovery.scheduler.ExpirationSchedulerFactory
import org.ethereum.beacon.discovery.schema.NodeRecord
import org.ethereum.beacon.discovery.schema.NodeRecordInfo
import org.ethereum.beacon.discovery.storage.NodeTable
import org.ethereum.beacon.discovery.task.DiscoveryTaskManager
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer
import java.util.stream.Stream


class VirtualDiscoverySystem internal constructor(
        private val discoveryManager: DiscoveryManager,
        private val taskManager: DiscoveryTaskManager,
        private val expirationSchedulerFactory: ExpirationSchedulerFactory,
        private val nodeTable: NodeTable,
        private val bootnodes: List<NodeRecord>) {
    fun start(): CompletableFuture<Void> {
        return discoveryManager.start().thenRun { taskManager.start() }.thenRun { pingBootnodes() }
    }

    private fun pingBootnodes() {
        bootnodes.forEach(
                Consumer { bootnode: NodeRecord ->
                    discoveryManager
                            .ping(bootnode)
                            .exceptionally { e: Throwable? ->
                                LOG.debug("Failed to ping bootnode: $bootnode")
                                null
                            }
                })
    }

    fun stop() {
        taskManager.stop()
        discoveryManager.stop()
        expirationSchedulerFactory.stop()
    }

    val localNodeRecord: NodeRecord
        get() = discoveryManager.localNodeRecord

    fun updateCustomFieldValue(fieldName: String?, value: Bytes?) {
        discoveryManager.updateCustomFieldValue(fieldName, value)
    }

    /**
     * Initiates FINDNODE with node `nodeRecord`
     *
     * @param nodeRecord Ethereum Node record
     * @param distance Distance to search for
     * @return Future which is fired when reply is received or fails in timeout/not successful
     * handshake/bad message exchange.
     */
    fun findNodes(nodeRecord: NodeRecord?, distance: Int): CompletableFuture<Void> {
        return discoveryManager.findNodes(nodeRecord, distance)
    }

    /**
     * Initiates PING with node `nodeRecord`
     *
     * @param nodeRecord Ethereum Node record
     * @return Future which is fired when reply is received or fails in timeout/not successful
     * handshake/bad message exchange.
     */
    fun ping(nodeRecord: NodeRecord?): CompletableFuture<Void> {
        return discoveryManager.ping(nodeRecord)
    }

    fun streamKnownNodes(): Stream<NodeRecordInfo> {
        // 0 indicates no limit to the number of nodes to return.
        return nodeTable.streamClosestNodes(Bytes32.ZERO, 0)
    }

    fun searchForNewPeers(): CompletableFuture<Void> {
        return taskManager.searchForNewPeers()
    }

    companion object {
        private val LOG = LogManager.getLogger()
    }
}
