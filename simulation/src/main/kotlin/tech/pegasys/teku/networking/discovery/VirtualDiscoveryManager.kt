package tech.pegasys.teku.networking.discovery

import com.google.common.annotations.VisibleForTesting
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.tuweni.bytes.Bytes
import org.ethereum.beacon.discovery.DiscoveryManager
import org.ethereum.beacon.discovery.network.NetworkParcel
import org.ethereum.beacon.discovery.pipeline.Envelope
import org.ethereum.beacon.discovery.pipeline.Field
import org.ethereum.beacon.discovery.pipeline.Pipeline
import org.ethereum.beacon.discovery.pipeline.PipelineImpl
import org.ethereum.beacon.discovery.pipeline.handler.AuthHeaderMessagePacketHandler
import org.ethereum.beacon.discovery.pipeline.handler.BadPacketHandler
import org.ethereum.beacon.discovery.pipeline.handler.IncomingDataPacker
import org.ethereum.beacon.discovery.pipeline.handler.MessageHandler
import org.ethereum.beacon.discovery.pipeline.handler.MessagePacketHandler
import org.ethereum.beacon.discovery.pipeline.handler.NewTaskHandler
import org.ethereum.beacon.discovery.pipeline.handler.NextTaskHandler
import org.ethereum.beacon.discovery.pipeline.handler.NodeIdToSession
import org.ethereum.beacon.discovery.pipeline.handler.NodeSessionRequestHandler
import org.ethereum.beacon.discovery.pipeline.handler.NotExpectedIncomingPacketHandler
import org.ethereum.beacon.discovery.pipeline.handler.OutgoingParcelHandler
import org.ethereum.beacon.discovery.pipeline.handler.UnknownPacketTagToSender
import org.ethereum.beacon.discovery.pipeline.handler.UnknownPacketTypeByStatus
import org.ethereum.beacon.discovery.pipeline.handler.WhoAreYouAttempt
import org.ethereum.beacon.discovery.pipeline.handler.WhoAreYouPacketHandler
import org.ethereum.beacon.discovery.pipeline.handler.WhoAreYouSessionResolver
import org.ethereum.beacon.discovery.scheduler.ExpirationSchedulerFactory
import org.ethereum.beacon.discovery.scheduler.Scheduler
import org.ethereum.beacon.discovery.schema.NodeRecord
import org.ethereum.beacon.discovery.schema.NodeRecordFactory
import org.ethereum.beacon.discovery.storage.AuthTagRepository
import org.ethereum.beacon.discovery.storage.LocalNodeRecordStore
import org.ethereum.beacon.discovery.storage.NodeBucketStorage
import org.ethereum.beacon.discovery.storage.NodeTable
import org.ethereum.beacon.discovery.task.TaskOptions
import org.ethereum.beacon.discovery.task.TaskType
import org.reactivestreams.Publisher
import reactor.core.publisher.Flux
import reactor.core.publisher.FluxSink
import reactor.core.publisher.ReplayProcessor
import tech.pegasys.teku.simulation.VirtualPeer
import java.net.InetSocketAddress
import java.util.concurrent.CompletableFuture
import kotlin.coroutines.CoroutineContext

class VirtualDiscoveryManager(
        private val listenAddress: InetSocketAddress,
        nodeTable: NodeTable,
        nodeBucketStorage: NodeBucketStorage,
        private val localNodeRecordStore: LocalNodeRecordStore,
        homeNodePrivateKey: Bytes,
        nodeRecordFactory: NodeRecordFactory,
        taskScheduler: Scheduler,
        expirationSchedulerFactory: ExpirationSchedulerFactory,
        private val peer: VirtualPeer) : DiscoveryManager {
    private val outgoingMessages: ReplayProcessor<NetworkParcel> = ReplayProcessor.cacheLast()
    private val incomingPipeline: Pipeline = PipelineImpl()
    private val outgoingPipeline: Pipeline = PipelineImpl()

    init {
        val homeNodeRecord = localNodeRecordStore.localNodeRecord
        val authTagRepo = AuthTagRepository()
        val nodeIdToSession = NodeIdToSession(
                localNodeRecordStore,
                homeNodePrivateKey,
                nodeBucketStorage,
                authTagRepo,
                nodeTable,
                outgoingPipeline,
                expirationSchedulerFactory)
        incomingPipeline
                .addHandler(IncomingDataPacker())
                .addHandler(WhoAreYouAttempt(homeNodeRecord.nodeId))
                .addHandler(WhoAreYouSessionResolver(authTagRepo))
                .addHandler(UnknownPacketTagToSender(homeNodeRecord.nodeId))
                .addHandler(nodeIdToSession)
                .addHandler(UnknownPacketTypeByStatus())
                .addHandler(NotExpectedIncomingPacketHandler())
                .addHandler(WhoAreYouPacketHandler(outgoingPipeline, taskScheduler))
                .addHandler(
                        AuthHeaderMessagePacketHandler(outgoingPipeline, taskScheduler, nodeRecordFactory))
                .addHandler(MessagePacketHandler())
                .addHandler(MessageHandler(nodeRecordFactory, localNodeRecordStore))
                .addHandler(BadPacketHandler())
        val outgoingSink: FluxSink<NetworkParcel> = outgoingMessages.sink()
        outgoingPipeline
                .addHandler(OutgoingParcelHandler(outgoingSink))
                .addHandler(NodeSessionRequestHandler())
                .addHandler(nodeIdToSession)
                .addHandler(NewTaskHandler())
                .addHandler(NextTaskHandler(outgoingPipeline, taskScheduler))
    }

    override fun start() = runBlocking {
        val future = CompletableFuture<Void>()
        launch { startImpl(future, this) }
        future
    }

    suspend fun startImpl(future: CompletableFuture<Void>, scope: CoroutineScope): CompletableFuture<Void> {
        incomingPipeline.build()
        outgoingPipeline.build()
        Flux.from(outgoingMessages)
                .subscribe { runBlocking(scope.coroutineContext) { peer.publishOnDiscovery(DiscoveryEvent(it.packet.bytes, listenAddress, it.destination)) } }
        future.complete(null)
        return future
    }

    private suspend fun pollOutgoingMessages() {

    }

    fun putIncomingDiscovery(event: DiscoveryEvent) {
        val envelope = Envelope()
        envelope.put(Field.INCOMING, event.data)
        envelope.put(Field.REMOTE_SENDER, event.from)
        incomingPipeline.push(envelope)
    }

    override fun stop() {
        // TODO: implement when needed
    }

    override fun getLocalNodeRecord(): NodeRecord {
        return localNodeRecordStore.localNodeRecord
    }

    override fun updateCustomFieldValue(fieldName: String, value: Bytes) {
        localNodeRecordStore.onCustomFieldValueChanged(fieldName, value)
    }

    private fun executeTaskImpl(
            nodeRecord: NodeRecord, taskType: TaskType, taskOptions: TaskOptions): CompletableFuture<Void> {
        val envelope = Envelope()
        envelope.put(Field.NODE, nodeRecord)
        val future = CompletableFuture<Void>()
        envelope.put(Field.TASK, taskType)
        envelope.put(Field.FUTURE, future)
        envelope.put(Field.TASK_OPTIONS, taskOptions)
        outgoingPipeline.push(envelope)
        return future
    }

    override fun findNodes(nodeRecord: NodeRecord, distance: Int): CompletableFuture<Void> {
        return executeTaskImpl(nodeRecord, TaskType.FINDNODE, TaskOptions(true, distance))
    }

    override fun ping(nodeRecord: NodeRecord): CompletableFuture<Void> {
        return executeTaskImpl(nodeRecord, TaskType.PING, TaskOptions(true))
    }

    @VisibleForTesting
    fun getOutgoingMessages(): Publisher<NetworkParcel> {
        return outgoingMessages
    }
}