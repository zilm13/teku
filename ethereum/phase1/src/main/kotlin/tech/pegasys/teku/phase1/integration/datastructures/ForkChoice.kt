package tech.pegasys.teku.phase1.integration.datastructures

import tech.pegasys.teku.phase1.onotole.phase1.BeaconBlockHeader
import tech.pegasys.teku.phase1.onotole.phase1.BeaconState
import tech.pegasys.teku.phase1.onotole.phase1.Checkpoint
import tech.pegasys.teku.phase1.onotole.phase1.Epoch
import tech.pegasys.teku.phase1.onotole.phase1.LatestMessage
import tech.pegasys.teku.phase1.onotole.phase1.Root
import tech.pegasys.teku.phase1.onotole.phase1.Store
import tech.pegasys.teku.phase1.onotole.phase1.ValidatorIndex
import tech.pegasys.teku.phase1.onotole.ssz.CDict
import tech.pegasys.teku.phase1.onotole.ssz.uint64

internal class StoreImpl(
  override var time: uint64,
  override val genesis_time: uint64,
  override var justified_checkpoint: Checkpoint,
  override var finalized_checkpoint: Checkpoint,
  override var best_justified_checkpoint: Checkpoint,
  override var blocks: CDict<Root, BeaconBlockHeader>,
  override var block_states: CDict<Root, BeaconState>,
  override var checkpoint_states: CDict<Checkpoint, BeaconState>,
  override var latest_messages: CDict<ValidatorIndex, LatestMessage>
) : Store

internal class LatestMessageImpl(override val epoch: Epoch, override val root: Root) :
  LatestMessage {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as LatestMessageImpl

    if (epoch != other.epoch) return false
    if (root != other.root) return false

    return true
  }

  override fun hashCode(): Int {
    var result = epoch.hashCode()
    result = 31 * result + root.hashCode()
    return result
  }

  override fun toString(): String {
    return "LatestMessageImpl(epoch=$epoch, root=$root)"
  }
}
