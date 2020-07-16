package tech.pegasys.teku.phase1.simulation.util

import org.apache.tuweni.bytes.Bytes
import tech.pegasys.teku.phase1.integration.datastructures.BeaconState
import tech.pegasys.teku.phase1.integration.datastructures.ShardBlock
import tech.pegasys.teku.phase1.integration.datastructures.SignedShardBlock
import tech.pegasys.teku.phase1.onotole.phase1.Root
import tech.pegasys.teku.phase1.onotole.phase1.Shard
import tech.pegasys.teku.phase1.onotole.phase1.Slot
import tech.pegasys.teku.phase1.onotole.phase1.get_shard_block_signature
import tech.pegasys.teku.phase1.onotole.phase1.get_shard_proposer_index
import java.util.*

private const val SHARD_BLOCK_SIZE = 1024
private val rnd = Random(1)

fun produceShardBlock(
  slot: Slot,
  shard: Shard,
  shardParentRoot: Root,
  beaconHeadRoot: Root,
  beaconHeadState: BeaconState,
  body: Bytes,
  secretKeys: SecretKeyRegistry
): SignedShardBlock {
  val proposerIndex = get_shard_proposer_index(beaconHeadState, slot, shard)
  val shardBlock = ShardBlock(
    shardParentRoot,
    beaconHeadRoot,
    slot,
    shard,
    proposerIndex,
    body.toArrayUnsafe().toList()
  )

  return SignedShardBlock(
    shardBlock,
    get_shard_block_signature(beaconHeadState, shardBlock, secretKeys[proposerIndex])
  )
}

fun getRandomShardBlockBody(): Bytes = Bytes.random(SHARD_BLOCK_SIZE, rnd)
