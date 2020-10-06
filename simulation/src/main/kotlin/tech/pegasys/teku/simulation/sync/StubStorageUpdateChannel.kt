package tech.pegasys.teku.simulation.sync

import tech.pegasys.teku.storage.api.StorageUpdateChannel
import tech.pegasys.teku.storage.events.StorageUpdate
import tech.pegasys.teku.storage.store.UpdatableStore
import tech.pegasys.teku.util.async.SafeFuture
import java.util.Optional

class StubStorageUpdateChannel : StorageUpdateChannel {
    override fun onStoreRequest(): SafeFuture<Optional<UpdatableStore>> {
        return SafeFuture.failedFuture(IllegalStateException("Storage is unavailable."))
    }

    override fun onStorageUpdate(event: StorageUpdate): SafeFuture<Void> {
        return SafeFuture.COMPLETE
    }

    override fun onGenesis(store: UpdatableStore) {}
}