package tech.pegasys.teku.simulation.sync

import com.google.common.eventbus.EventBus
import com.google.common.primitives.UnsignedLong
import org.hyperledger.besu.metrics.noop.NoOpMetricsSystem
import tech.pegasys.teku.bls.BLSKeyPair
import tech.pegasys.teku.datastructures.blocks.SignedBeaconBlock
import tech.pegasys.teku.events.EventChannels
import tech.pegasys.teku.networking.eth2.Eth2Network
import tech.pegasys.teku.networking.p2p.network.NetworkConfig
import tech.pegasys.teku.networking.p2p.network.PeerAddress
import tech.pegasys.teku.networking.p2p.peer.Peer
import tech.pegasys.teku.networking.p2p.VirtualEth2NetworkFactory
import tech.pegasys.teku.simulation.util.BeaconChainUtil
import tech.pegasys.teku.statetransition.blockimport.BlockImporter
import tech.pegasys.teku.statetransition.util.FutureItems
import tech.pegasys.teku.statetransition.util.PendingPool
import tech.pegasys.teku.storage.api.FinalizedCheckpointChannel
import tech.pegasys.teku.storage.client.MemoryOnlyRecentChainData
import tech.pegasys.teku.storage.client.RecentChainData
import tech.pegasys.teku.sync.BlockManager
import tech.pegasys.teku.sync.DefaultSyncService
import tech.pegasys.teku.sync.FetchRecentBlocksService
import tech.pegasys.teku.sync.SyncManager
import tech.pegasys.teku.sync.SyncService
import tech.pegasys.teku.util.async.AsyncRunner
import tech.pegasys.teku.util.async.SafeFuture
import tech.pegasys.teku.util.time.channels.SlotEventsChannel
import java.security.SecureRandom
import java.util.function.Consumer

class SyncingNodeManager private constructor(
        private val eventBus: EventBus,
        private val eventChannels: EventChannels,
        private val storageClient: RecentChainData,
        private val chainUtil: BeaconChainUtil,
        private val eth2Network: Eth2Network,
        private val syncService: SyncService) {

    fun connect(peer: SyncingNodeManager): SafeFuture<Peer> {
        val peerAddress: PeerAddress = eth2Network.createPeerAddress(peer.network().getNodeAddress())
        return eth2Network.connect(peerAddress)
    }

    fun eventChannels(): EventChannels {
        return eventChannels
    }

    fun eventBus(): EventBus {
        return eventBus
    }

    fun chainUtil(): BeaconChainUtil {
        return chainUtil
    }

    fun network(): Eth2Network {
        return eth2Network
    }

    fun storageClient(): RecentChainData {
        return storageClient
    }

    fun syncService(): SyncService {
        return syncService
    }

    fun onSyncDone(): SafeFuture<Void>{
        return syncService.onSyncDone(MILLIS_RETRY);
    }

    fun saveBlock(block: SignedBeaconBlock) {
        chainUtil.importBlock(block)
    }

    fun setSlot(slot: UnsignedLong) {
        eventChannels().getPublisher(SlotEventsChannel::class.java).onSlot(slot)
        chainUtil().setSlot(slot)
    }

    companion object {
        private const val MILLIS_RETRY = 100;

        public fun create(
                asyncRunner: AsyncRunner,
                networkFactory: VirtualEth2NetworkFactory,
                networkConfig: NetworkConfig,
                validatorKeys: List<BLSKeyPair>,
                configureNetwork: Consumer<VirtualEth2NetworkFactory.Eth2P2PNetworkBuilder>,
                rnd: SecureRandom): SyncingNodeManager {
            val eventBus = EventBus()
            val eventChannels = EventChannels.createSyncChannels(TestExceptionHandler.TEST_EXCEPTION_HANDLER, NoOpMetricsSystem())
            val recentChainData = MemoryOnlyRecentChainData.create(eventBus)
            val chainUtil = BeaconChainUtil.create(recentChainData, validatorKeys)
            chainUtil.initializeStorage()
            val networkBuilder = networkFactory.builder(rnd)
                    .eventBus(eventBus)
                    .networkConfig(networkConfig)
                    .recentChainData(recentChainData)
            configureNetwork.accept(networkBuilder)
            val eth2Network = networkBuilder.startNetwork()
            val blockImporter = BlockImporter(recentChainData, eventBus)
            val pendingBlocks = PendingPool.createForBlocks()
            val futureBlocks = FutureItems { obj: SignedBeaconBlock -> obj.slot }
            val recentBlockFetcher = FetchRecentBlocksService.create(asyncRunner, eth2Network, pendingBlocks)
            val blockManager = BlockManager.create(
                    eventBus,
                    pendingBlocks,
                    futureBlocks,
                    recentBlockFetcher,
                    recentChainData,
                    blockImporter)
            val syncManager = SyncManager.create(asyncRunner, eth2Network, recentChainData, blockImporter)
            val syncService: SyncService = DefaultSyncService(blockManager, syncManager, recentChainData)
            eventChannels
                    .subscribe(SlotEventsChannel::class.java, blockManager)
                    .subscribe(FinalizedCheckpointChannel::class.java, pendingBlocks)
            syncService.start().join()
            return SyncingNodeManager(
                    eventBus, eventChannels, recentChainData, chainUtil, eth2Network, syncService)
        }
    }
}