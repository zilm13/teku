package tech.pegasys.teku.networking.discovery

import com.google.common.base.Preconditions
import com.google.common.util.concurrent.ThreadFactoryBuilder
import org.apache.tuweni.bytes.Bytes
import org.apache.tuweni.units.bigints.UInt64
import org.ethereum.beacon.discovery.DiscoveryManager
import org.ethereum.beacon.discovery.database.Database
import org.ethereum.beacon.discovery.scheduler.ExpirationSchedulerFactory
import org.ethereum.beacon.discovery.scheduler.Schedulers
import org.ethereum.beacon.discovery.schema.NodeRecord
import org.ethereum.beacon.discovery.schema.NodeRecordFactory
import org.ethereum.beacon.discovery.storage.LocalNodeRecordStore
import org.ethereum.beacon.discovery.storage.NodeBucketStorage
import org.ethereum.beacon.discovery.storage.NodeBucketStorageImpl
import org.ethereum.beacon.discovery.storage.NodeSerializerFactory
import org.ethereum.beacon.discovery.storage.NodeTableStorageFactory
import org.ethereum.beacon.discovery.storage.NodeTableStorageFactoryImpl
import org.ethereum.beacon.discovery.task.DiscoveryTaskManager
import tech.pegasys.teku.simulation.VirtualPeer
import java.net.InetSocketAddress
import java.util.Arrays
import java.util.Optional
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import java.util.stream.Collectors
import java.util.stream.Stream

class VirtualDiscoverySystemBuilder {
    private var bootnodes: List<NodeRecord> = emptyList()
    private var listenAddress = Optional.empty<InetSocketAddress>()
    private var localNodeRecord: NodeRecord? = null
    private var privateKey: Bytes? = null
    private val nodeRecordFactory = NodeRecordFactory.DEFAULT
    private var database: Database? = null
    private var schedulers: Schedulers? = null
    private var peer: VirtualPeer? = null

    fun localNodeRecord(localNodeRecord: NodeRecord): VirtualDiscoverySystemBuilder {
        this.localNodeRecord = localNodeRecord
        return this
    }

    fun listen(listenAddress: String, listenPort: Int): VirtualDiscoverySystemBuilder {
        this.listenAddress = Optional.of(InetSocketAddress(listenAddress, listenPort))
        return this
    }

    fun privateKey(privateKey: Bytes): VirtualDiscoverySystemBuilder {
        this.privateKey = privateKey
        return this
    }

    fun bootnodes(vararg enrs: String): VirtualDiscoverySystemBuilder {
        bootnodes = Stream.of(*enrs)
                .map { enr: String -> if (enr.startsWith("enr:")) enr.substring("enr:".length) else enr }
                .map { enrBase64: String -> nodeRecordFactory.fromBase64(enrBase64) }
                .collect(Collectors.toList())
        return this
    }

    fun bootnodes(vararg records: NodeRecord): VirtualDiscoverySystemBuilder {
        bootnodes = Arrays.asList(*records)
        return this
    }

    fun database(database: Database): VirtualDiscoverySystemBuilder {
        this.database = database
        return this
    }

    fun schedulers(schedulers: Schedulers): VirtualDiscoverySystemBuilder {
        this.schedulers = schedulers
        return this
    }

    fun peer(peer: VirtualPeer): VirtualDiscoverySystemBuilder {
        this.peer = peer
        return this
    }

    fun build(): VirtualDiscoverySystem {
        Preconditions.checkNotNull(localNodeRecord, "Missing local node record")
        Preconditions.checkNotNull(privateKey, "Missing private key")
        if (database == null) {
            database = Database.inMemoryDB()
        }
        val nodeTableStorageFactory: NodeTableStorageFactory = NodeTableStorageFactoryImpl()
        val serializerFactory = NodeSerializerFactory(nodeRecordFactory)
        val nodeTableStorage = nodeTableStorageFactory.createTable(
                database, serializerFactory, { oldSeq: UInt64? -> localNodeRecord }) { bootnodes }
        val nodeTable = nodeTableStorage.get()
        if (schedulers == null) {
            schedulers = Schedulers.createDefault()
        }
        val nodeBucketStorage: NodeBucketStorage = NodeBucketStorageImpl(database, serializerFactory, localNodeRecord)
        val clientNumber = COUNTER.incrementAndGet()
        val localNodeRecordStore = LocalNodeRecordStore(localNodeRecord, privateKey)
        val expirationSchedulerFactory = ExpirationSchedulerFactory(
                Executors.newSingleThreadScheduledExecutor(
                        ThreadFactoryBuilder().setNameFormat("discovery-expiration-%d").build()))
        val discoveryManager: DiscoveryManager = VirtualDiscoveryManager(
                listenAddress.get(),
                nodeTable,
                nodeBucketStorage,
                localNodeRecordStore,
                privateKey!!,
                nodeRecordFactory,
                schedulers!!.newSingleThreadDaemon("discovery-client-$clientNumber"),
                expirationSchedulerFactory,
                peer!!
        )
        val discoveryTaskManager = DiscoveryTaskManager(
                discoveryManager,
                nodeTable,
                nodeBucketStorage,
                localNodeRecord,
                schedulers!!.newSingleThreadDaemon("discovery-tasks-$clientNumber"),
                true,
                true,
                expirationSchedulerFactory)
        return VirtualDiscoverySystem(
                discoveryManager, discoveryTaskManager, expirationSchedulerFactory, nodeTable, bootnodes)
    }

    companion object {
        private val COUNTER = AtomicInteger()
    }
}