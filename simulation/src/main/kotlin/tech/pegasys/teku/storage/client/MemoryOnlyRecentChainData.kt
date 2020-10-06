package tech.pegasys.teku.storage.client

import com.google.common.base.Preconditions
import com.google.common.eventbus.EventBus
import org.hyperledger.besu.metrics.noop.NoOpMetricsSystem
import org.hyperledger.besu.plugin.services.MetricsSystem
import tech.pegasys.teku.simulation.sync.StubFinalizedCheckpointChannel
import tech.pegasys.teku.simulation.sync.StubReorgEventChannel
import tech.pegasys.teku.simulation.sync.StubStorageUpdateChannel
import tech.pegasys.teku.storage.api.FinalizedCheckpointChannel
import tech.pegasys.teku.storage.api.ReorgEventChannel
import tech.pegasys.teku.storage.api.StorageUpdateChannel
import tech.pegasys.teku.storage.store.UpdatableStore

class MemoryOnlyRecentChainData private constructor(
        metricsSystem: MetricsSystem,
        eventBus: EventBus,
        storageUpdateChannel: StorageUpdateChannel,
        finalizedCheckpointChannel: FinalizedCheckpointChannel,
        reorgEventChannel: ReorgEventChannel) : RecentChainData(
        metricsSystem,
        storageUpdateChannel,
        finalizedCheckpointChannel,
        reorgEventChannel,
        eventBus) {
    class Builder {
        var eventBus = EventBus()
        var storageUpdateChannel: StorageUpdateChannel = StubStorageUpdateChannel()
        var finalizedCheckpointChannel: FinalizedCheckpointChannel = StubFinalizedCheckpointChannel()
        var reorgEventChannel: ReorgEventChannel = StubReorgEventChannel()
        fun build(): RecentChainData {
            return MemoryOnlyRecentChainData(
                    NoOpMetricsSystem(),
                    eventBus,
                    storageUpdateChannel,
                    finalizedCheckpointChannel,
                    reorgEventChannel)
        }

        fun eventBus(eventBus: EventBus): Builder {
            Preconditions.checkNotNull(eventBus)
            this.eventBus = eventBus
            return this
        }

        fun storageUpdateChannel(storageUpdateChannel: StorageUpdateChannel): Builder {
            Preconditions.checkNotNull(storageUpdateChannel)
            this.storageUpdateChannel = storageUpdateChannel
            return this
        }

        fun finalizedCheckpointChannel(
                finalizedCheckpointChannel: FinalizedCheckpointChannel): Builder {
            Preconditions.checkNotNull(finalizedCheckpointChannel)
            this.finalizedCheckpointChannel = finalizedCheckpointChannel
            return this
        }

        fun reorgEventChannel(reorgEventChannel: ReorgEventChannel): Builder {
            Preconditions.checkNotNull(reorgEventChannel)
            this.reorgEventChannel = reorgEventChannel
            return this
        }
    }

    companion object {
        fun builder(): Builder {
            return Builder()
        }

        fun create(eventBus: EventBus): RecentChainData {
            return builder().eventBus(eventBus).build()
        }

        fun create(
                eventBus: EventBus, reorgEventChannel: ReorgEventChannel): RecentChainData {
            return builder().eventBus(eventBus).reorgEventChannel(reorgEventChannel).build()
        }

        fun createWithStore(
                eventBus: EventBus,
                reorgEventChannel: ReorgEventChannel,
                store: UpdatableStore): RecentChainData {
            val recentChainData = builder().eventBus(eventBus).reorgEventChannel(reorgEventChannel).build()
            recentChainData.setStore(store)
            return recentChainData
        }
    }

    init {
        eventBus.register(this)
    }
}