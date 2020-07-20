package tech.pegasys.teku.phase1.simulation

import tech.pegasys.teku.phase1.eth1client.Eth1BlockData
import tech.pegasys.teku.phase1.eth1client.Eth1EngineClient
import tech.pegasys.teku.phase1.eth1shard.ETH1_SHARD_NUMBER
import tech.pegasys.teku.phase1.integration.datastructures.ShardStore
import tech.pegasys.teku.phase1.integration.datastructures.SignedShardBlock
import tech.pegasys.teku.phase1.integration.datastructures.Store
import tech.pegasys.teku.phase1.onotole.phase1.Shard
import tech.pegasys.teku.phase1.onotole.phase1.on_shard_block
import tech.pegasys.teku.phase1.util.Color
import tech.pegasys.teku.phase1.util.log

interface ShardBlockProcessor {
  fun process(block: SignedShardBlock)
}

class RegularShardProcessor(
  private val store: Store,
  private val shardStore: ShardStore
) : ShardBlockProcessor {
  override fun process(block: SignedShardBlock) {
    on_shard_block(store, shardStore, block)
  }
}

class Eth1ShardProcessor(
  private val store: Store,
  private val shardStore: ShardStore,
  private val eth1Engine: Eth1EngineClient
) : ShardBlockProcessor {
  override fun process(block: SignedShardBlock) {
    val eth1BlockData = Eth1BlockData(block.message.body.toBytes())
    val ret = eth1Engine.eth2_insertBlock(eth1BlockData.blockRLP)
    if (ret.result != true) {
      throw IllegalStateException("Failed to import $eth1BlockData, reason: ${ret.reason}")
    }

    on_shard_block(store, shardStore, block)

    log("Eth1ShardProcessor: eth1 shard block inserted:\n$eth1BlockData\n", Color.YELLOW)
  }
}

@Suppress("FunctionName")
fun ShardBlockProcessor(
  shard: Shard,
  store: Store,
  shardStore: ShardStore,
  eth1Engine: Eth1EngineClient
): ShardBlockProcessor {
  return if (shard == ETH1_SHARD_NUMBER) {
    Eth1ShardProcessor(store, shardStore, eth1Engine)
  } else {
    RegularShardProcessor(store, shardStore)
  }
}
