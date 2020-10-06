package tech.pegasys.teku.simulation.sync

import com.google.common.primitives.UnsignedLong
import org.apache.tuweni.bytes.Bytes32
import tech.pegasys.teku.datastructures.blocks.SignedBeaconBlock
import tech.pegasys.teku.datastructures.state.BeaconState
import tech.pegasys.teku.storage.api.StorageQueryChannel
import tech.pegasys.teku.util.async.SafeFuture
import java.util.Optional

class StubStorageQueryChannel : StorageQueryChannel {
    override fun getFinalizedBlockAtSlot(slot: UnsignedLong): SafeFuture<Optional<SignedBeaconBlock>> {
        return SafeFuture.completedFuture(Optional.empty())
    }

    override fun getLatestFinalizedBlockAtSlot(slot: UnsignedLong): SafeFuture<Optional<SignedBeaconBlock>> {
        return SafeFuture.completedFuture(Optional.empty())
    }

    override fun getBlockByBlockRoot(blockRoot: Bytes32): SafeFuture<Optional<SignedBeaconBlock>> {
        return SafeFuture.completedFuture(Optional.empty())
    }

    override fun getLatestFinalizedStateAtSlot(slot: UnsignedLong): SafeFuture<Optional<BeaconState>> {
        return SafeFuture.completedFuture(Optional.empty())
    }

    override fun getFinalizedStateByBlockRoot(blockRoot: Bytes32): SafeFuture<Optional<BeaconState>> {
        return SafeFuture.completedFuture(Optional.empty())
    }
}