package tech.pegasys.teku.simulation.sync

import tech.pegasys.teku.datastructures.state.Checkpoint
import tech.pegasys.teku.storage.api.FinalizedCheckpointChannel

class StubFinalizedCheckpointChannel : FinalizedCheckpointChannel {
    override fun onNewFinalizedCheckpoint(checkpoint: Checkpoint) {}
}