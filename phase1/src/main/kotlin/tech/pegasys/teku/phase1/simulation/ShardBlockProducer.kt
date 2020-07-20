package tech.pegasys.teku.phase1.simulation

import org.apache.tuweni.bytes.Bytes32
import tech.pegasys.teku.phase1.eth1client.Eth1BlockData
import tech.pegasys.teku.phase1.eth1client.Eth1EngineClient
import tech.pegasys.teku.phase1.eth1shard.ETH1_SHARD_NUMBER
import tech.pegasys.teku.phase1.integration.datastructures.ShardBlock
import tech.pegasys.teku.phase1.integration.datastructures.SignedShardBlock
import tech.pegasys.teku.phase1.onotole.phase1.Root
import tech.pegasys.teku.phase1.onotole.phase1.Shard
import tech.pegasys.teku.phase1.onotole.phase1.Slot
import tech.pegasys.teku.phase1.simulation.util.SecretKeyRegistry
import tech.pegasys.teku.phase1.simulation.util.getRandomShardBlockBody
import tech.pegasys.teku.phase1.simulation.util.produceShardBlock
import tech.pegasys.teku.phase1.util.Color
import tech.pegasys.teku.phase1.util.log

interface ShardBlockProducer {
  fun produce(
    slot: Slot,
    beaconHead: BeaconHead,
    parentRoot: Root,
    parent: ShardBlock
  ): SignedShardBlock
}

class RandomShardBlockProducer(
  private val shard: Shard,
  private val secretKeys: SecretKeyRegistry
) : ShardBlockProducer {
  override fun produce(
    slot: Slot,
    beaconHead: BeaconHead,
    parentRoot: Root,
    parent: ShardBlock
  ): SignedShardBlock {
    val body = getRandomShardBlockBody()
    return produceShardBlock(
      slot,
      shard,
      parentRoot,
      beaconHead.root,
      beaconHead.state,
      body,
      secretKeys
    )
  }
}

class Eth1ShardBlockProducer(
  private val shard: Shard,
  private val secretKeys: SecretKeyRegistry,
  private val eth1Engine: Eth1EngineClient
) : ShardBlockProducer {

  override fun produce(
    slot: Slot,
    beaconHead: BeaconHead,
    parentRoot: Root,
    parent: ShardBlock
  ): SignedShardBlock {
    val eth1ParentHash = getEth1BlockHash(parent, eth1Engine)
    val ret = eth1Engine.eth2_produceBlock(eth1ParentHash)

    // check if Eth1 block was created successfully
    if (ret.result == null) {
      throw IllegalStateException("Failed to create Eth1Block(slot=$slot), reason ${ret.reason}")
    }
    val eth1BlockData = ret.result

    // check if created Eth1 block was imported successfully
    val importRet = eth1Engine.eth2_insertBlock(eth1BlockData.blockRLP)
    if (importRet.result == false) {
      throw IllegalStateException("Failed to import $eth1BlockData, reason ${ret.reason}")
    }

    log("Eth1ShardBlockProducer: New block created:\n$eth1BlockData\n", Color.YELLOW)

    val body = eth1BlockData.encodeWithSOS()
    return produceShardBlock(
      slot,
      shard,
      parentRoot,
      beaconHead.root,
      beaconHead.state,
      body,
      secretKeys
    )
  }
}

/**
 * Falls back to eth1-engine if shard block body is empty (PHASE_1_GENESIS case)
 */
private fun getEth1BlockHash(block: ShardBlock, eth1Engine: Eth1EngineClient): Bytes32 {
  return if (block.body.size > 0) {
    val eth1ParentData = Eth1BlockData(block.body.toBytes())
    eth1ParentData.blockHash
  } else {
    eth1Engine.eth_getHead().result!!
  }
}

@Suppress("FunctionName")
fun ShardBlockProducer(
  shard: Shard,
  secretKeys: SecretKeyRegistry,
  eth1Engine: Eth1EngineClient
): ShardBlockProducer {
  return if (shard == ETH1_SHARD_NUMBER) {
    Eth1ShardBlockProducer(shard, secretKeys, eth1Engine)
  } else {
    RandomShardBlockProducer(shard, secretKeys)
  }
}
