package tech.pegasys.teku.simulation

import com.google.common.util.concurrent.ThreadFactoryBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import org.apache.tuweni.bytes.Bytes
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.ethereum.beacon.discovery.database.Database
import org.ethereum.beacon.discovery.scheduler.ExpirationSchedulerFactory
import org.ethereum.beacon.discovery.scheduler.Schedulers
import org.ethereum.beacon.discovery.schema.NodeRecord
import org.ethereum.beacon.discovery.schema.NodeRecordBuilder
import org.ethereum.beacon.discovery.schema.NodeRecordFactory
import org.ethereum.beacon.discovery.storage.LocalNodeRecordStore
import org.ethereum.beacon.discovery.storage.NodeBucketStorage
import org.ethereum.beacon.discovery.storage.NodeBucketStorageImpl
import org.ethereum.beacon.discovery.storage.NodeSerializerFactory
import org.ethereum.beacon.discovery.storage.NodeTableStorageFactory
import org.ethereum.beacon.discovery.storage.NodeTableStorageFactoryImpl
import tech.pegasys.teku.bls.BLSKeyPair
import tech.pegasys.teku.networking.discovery.DiscoveryEvent
import tech.pegasys.teku.networking.discovery.VirtualDiscoveryManager
import tech.pegasys.teku.networking.eth2.gossip.encoding.GossipEncoding
import tech.pegasys.teku.networking.eth2.rpc.core.encodings.RpcEncoding
import tech.pegasys.teku.networking.p2p.VirtualEth2NetworkFactory
import tech.pegasys.teku.phase1.integration.datastructures.BeaconBlockHeader
import tech.pegasys.teku.phase1.onotole.phase1.Root
import tech.pegasys.teku.phase1.onotole.phase1.SLOTS_PER_EPOCH
import tech.pegasys.teku.phase1.onotole.pylib.keys
import tech.pegasys.teku.phase1.onotole.ssz.SSZDict
import tech.pegasys.teku.phase1.simulation.Phase1Simulation
import tech.pegasys.teku.phase1.util.log
import tech.pegasys.teku.phase1.util.logSetDebugMode
import tech.pegasys.teku.service.serviceutils.AsyncRunnerFactory
import tech.pegasys.teku.simulation.sync.DelayedExecutorAsyncRunner
import tech.pegasys.teku.simulation.sync.SyncingNodeManager
import tech.pegasys.teku.simulation.sync.VirtualStubMetricsSystem
import tech.pegasys.teku.simulation.util.BLSKeyGenerator
import tech.pegasys.teku.simulation.util.Waiter
import tech.pegasys.teku.util.async.AsyncRunner
import java.security.SecureRandom
import java.security.Security
import java.util.ArrayList
import java.util.concurrent.Executors
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

class SyncSimulation(
        private val scope: CoroutineScope,
        private val config: Config
) {
    private val asyncRunner: AsyncRunner = DelayedExecutorAsyncRunner.create()
    private val validatorKeys: List<BLSKeyPair> = BLSKeyGenerator.generateKeyPairs(128, config.rnd)
    private val networkFactory: VirtualEth2NetworkFactory = VirtualEth2NetworkFactory()
    private val discoveryBus: Channel<DiscoveryEvent> = Channel(Channel.UNLIMITED)
    private val scheduler = Schedulers.createDefault().newParallelDaemon("all-nodes", 4)

    init {
        logSetDebugMode(config.debug)
    }

    suspend fun start() {
        log("Starting simulation...")
        val router = Router()
        val peers = ArrayList<VirtualPeer>()
        val nodeTableStorageFactory: NodeTableStorageFactory = NodeTableStorageFactoryImpl()
        val threadPool = Executors.newScheduledThreadPool(4,
                ThreadFactoryBuilder().setNameFormat("main-thread-pool").build())
        val expirationSchedulerFactory = ExpirationSchedulerFactory(threadPool)
        val metricsSystem = VirtualStubMetricsSystem()
        val asyncRunnerFactory = AsyncRunnerFactory()
        val asyncRunner = asyncRunnerFactory.create("async-runners", 4, metricsSystem)

        for (i in 0 until config.peers) {  // TODO: change to until without (-1) when until is fixed
            // TODO: make sync virtual, without network
            val networkConfig = genNetworkConfig(i)
            val syncManager =  SyncingNodeManager.create(
                    asyncRunner,
                    networkFactory,
                    networkConfig,
                    validatorKeys,
                    { it.rpcEncoding(RpcEncoding.SSZ).gossipEncoding(GossipEncoding.SSZ) },
                    config.rnd)
            val database = Database.inMemoryDB()
            val nodeRecordFactory = NodeRecordFactory.DEFAULT
            val serializerFactory = NodeSerializerFactory(nodeRecordFactory)
            val privateKey = Bytes.wrap(networkConfig.privateKey.raw())
            val localNodeRecord = NodeRecordBuilder()
                    .privateKey(privateKey)
                    .address(networkConfig.advertisedIp, networkConfig.advertisedPort)
                    .build()
            val bootnodes: List<NodeRecord> = networkConfig.bootnodes.map(nodeRecordFactory::fromBase64)
            val nodeTableStorage = nodeTableStorageFactory.createTable(
                    database, serializerFactory, { localNodeRecord }, { bootnodes })
            val nodeBucketStorage: NodeBucketStorage = NodeBucketStorageImpl(database, serializerFactory, localNodeRecord)
            val localNodeRecordStore = LocalNodeRecordStore(localNodeRecord, privateKey)
            val workerPool = ThreadPoolExecutor(
                    1,
                    2,
                    60,
                    TimeUnit.SECONDS,
                    SynchronousQueue(),
                    ThreadFactoryBuilder().setNameFormat("threadpool-async-%d").setDaemon(true).build())
            val peer = VirtualPeer(syncManager, discoveryBus, networkConfig, metricsSystem, asyncRunnerFactory, asyncRunner, workerPool)
            val discoveryManager = VirtualDiscoveryManager(
                    networkConfig.address, nodeTableStorage.get(), nodeBucketStorage, localNodeRecordStore, privateKey,
                    nodeRecordFactory, scheduler, expirationSchedulerFactory, peer
            )
            peer.setDiscoveryManager(discoveryManager)
            peers.add(peer)
            router.register(peer)
        }

////         TODO: pass our validators there if needed
//        val stateSim = simulateChain(config.startEpochs, config.rnd, scope)
//        peers[1].getStore().saveBlocks(cutBlocks(stateSim.getBlocks(), stateSim.getBlockSlots(), config.rnd.nextInt(config.peers), config.stateChunks))
        // TODO: add more than 2 nodes
        // Add some blocks to node1, which node 2 will need to fetch

        // TODO: split state to multiply parts, save parts on different nodes except 0s
        peers[1].generateBlocks(config.blockCount)

        // Connect networks
        Waiter.waitFor(peers[1].connect(peers[0]))
        Waiter.waitFor(peers[0].onSyncDone(), 120)
    }

    private fun cutBlocks(blocks: SSZDict<Root, BeaconBlockHeader>, blockSlots: SSZDict<ULong, Root>, chunk: Int, chunks: Int): List<BeaconBlockHeader> {
        val keys = blockSlots.keys().sorted()
        val lastSlot = keys.last()
        val chunkBlockStart = lastSlot/chunks.toULong() * chunk.toULong()
        val chunkBlockEnd = lastSlot/chunks.toULong() * (chunk + 1).toULong() + 1uL
        return blockSlots.filter { chunkBlockStart <= it.key && it.key < chunkBlockEnd }.values.map { blocks[it]!! }.toList()
    }

    suspend fun simulateChain(epochs: ULong, rnd: SecureRandom, scope: CoroutineScope): Phase1Simulation {
        Security.addProvider(BouncyCastleProvider())
        val simulation = Phase1Simulation(scope) {
            it.slotsToRun = epochs * SLOTS_PER_EPOCH
            it.validatorRegistrySize = 2048
            it.debug = false
            it.bls = Phase1Simulation.BLSConfig.Pseudo
        }
        simulation.start()
        return simulation
    }

    data class Config(
            var rnd: SecureRandom = SecureRandom(),
            var peers: Int = 10,
            var startEpochs: ULong = 100uL,
            var debug: Boolean = false,
            var stateChunks: Int = 10,
            var blockCount: ULong = 1000uL,
    ) {

    }
}

@Suppress("FunctionName")
fun SyncSimulation(
        scope: CoroutineScope,
        userConfig: (SyncSimulation.Config) -> Unit
): SyncSimulation {
    val config = SyncSimulation.Config()
    userConfig(config)
    return SyncSimulation(scope, config)
}