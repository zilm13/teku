package tech.pegasys.teku.phase1.integration.datastructures

import tech.pegasys.teku.phase1.onotole.phase1.Epoch
import tech.pegasys.teku.phase1.onotole.phase1.Root
import tech.pegasys.teku.phase1.onotole.phase1.Shard
import tech.pegasys.teku.phase1.onotole.phase1.ValidatorIndex
import tech.pegasys.teku.phase1.onotole.ssz.SSZDict
import tech.pegasys.teku.phase1.onotole.ssz.uint64

data class Store(
  var time: uint64 = 0uL,
  val genesis_time: uint64 = 0uL,
  var justified_checkpoint: Checkpoint = Checkpoint(),
  var finalized_checkpoint: Checkpoint = Checkpoint(),
  var best_justified_checkpoint: Checkpoint = Checkpoint(),
  var blocks: SSZDict<Root, BeaconBlockHeader> = SSZDict(),
  var blocks_by_slot: SSZDict<ULong, Root> = SSZDict(),
  var block_states: SSZDict<Root, BeaconState> = SSZDict(),
  var checkpoint_states: SSZDict<Checkpoint, BeaconState> = SSZDict(),
  var latest_messages: SSZDict<ValidatorIndex, LatestMessage> = SSZDict()
)

data class ShardStore(
  var shard: Shard = Shard(),
  var signed_blocks: SSZDict<Root, SignedShardBlock> = SSZDict(),
  var block_states: SSZDict<Root, ShardState> = SSZDict()
)

data class LatestMessage(
  var epoch: Epoch = Epoch(),
  var root: Root = Root(),
  var shard: Shard = Shard(),
  var shard_root: Root = Root()
)
