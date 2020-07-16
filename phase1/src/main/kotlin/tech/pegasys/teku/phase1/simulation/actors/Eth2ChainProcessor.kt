package tech.pegasys.teku.phase1.simulation.actors

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import tech.pegasys.teku.phase1.integration.datastructures.Attestation
import tech.pegasys.teku.phase1.integration.datastructures.FullAttestation
import tech.pegasys.teku.phase1.integration.datastructures.ShardStore
import tech.pegasys.teku.phase1.integration.datastructures.SignedBeaconBlock
import tech.pegasys.teku.phase1.integration.datastructures.SignedShardBlock
import tech.pegasys.teku.phase1.integration.datastructures.Store
import tech.pegasys.teku.phase1.onotole.phase1.INITIAL_ACTIVE_SHARDS
import tech.pegasys.teku.phase1.onotole.phase1.SECONDS_PER_SLOT
import tech.pegasys.teku.phase1.onotole.phase1.Shard
import tech.pegasys.teku.phase1.onotole.phase1.Slot
import tech.pegasys.teku.phase1.onotole.phase1.get_head
import tech.pegasys.teku.phase1.onotole.phase1.get_pending_shard_blocks
import tech.pegasys.teku.phase1.onotole.phase1.get_shard_head
import tech.pegasys.teku.phase1.onotole.phase1.on_attestation
import tech.pegasys.teku.phase1.onotole.phase1.on_block
import tech.pegasys.teku.phase1.onotole.phase1.on_shard_block
import tech.pegasys.teku.phase1.onotole.phase1.on_tick
import tech.pegasys.teku.phase1.simulation.BeaconHead
import tech.pegasys.teku.phase1.simulation.Eth2Actor
import tech.pegasys.teku.phase1.simulation.Eth2Event
import tech.pegasys.teku.phase1.simulation.HeadAfterAttestationsApplied
import tech.pegasys.teku.phase1.simulation.HeadAfterNewBeaconBlock
import tech.pegasys.teku.phase1.simulation.NewBeaconBlock
import tech.pegasys.teku.phase1.simulation.NewShardBlocks
import tech.pegasys.teku.phase1.simulation.NewShardHeads
import tech.pegasys.teku.phase1.simulation.NewSlot
import tech.pegasys.teku.phase1.simulation.NotCrosslinkedBlocksPublished
import tech.pegasys.teku.phase1.simulation.PrevSlotAttestationsPublished

class Eth2ChainProcessor(
  eventBus: SendChannel<Eth2Event>,
  private val store: Store,
  private val shardStores: Map<Shard, ShardStore>
) : Eth2Actor(eventBus) {

  override suspend fun dispatchImpl(event: Eth2Event, scope: CoroutineScope) {
    when (event) {
      is NewSlot -> onNewSlot(event.slot)
      is PrevSlotAttestationsPublished -> onPrevSlotAttestationsPublished(event.attestations)
      is NewBeaconBlock -> onNewBeaconBlock(event.block)
      is NewShardBlocks -> onNewShardBlocks(event.blocks)
    }
  }

  private suspend fun onNewBeaconBlock(block: SignedBeaconBlock) {
    on_block(store, block)
    publishBeaconHead(::HeadAfterNewBeaconBlock)
  }

  private fun onNewSlot(slot: Slot) {
    on_tick(store, store.genesis_time + slot * SECONDS_PER_SLOT)
  }

  private suspend fun onPrevSlotAttestationsPublished(attestations: List<FullAttestation>) {
    attestations.map { Attestation(it) }.forEach { on_attestation(store, it) }
    publishBeaconHead(::HeadAfterAttestationsApplied)
  }

  private suspend fun publishBeaconHead(eventCtor: (BeaconHead) -> Eth2Event) {
    val headRoot = get_head(store)
    val headState = store.block_states[headRoot]!!
    publish(
      eventCtor(
        BeaconHead(
          headRoot,
          headState
        )
      )
    )
  }

  private suspend fun onNewShardBlocks(blocks: List<SignedShardBlock>) {
    processNewShardBlocks(blocks)
    collectAndPublishShardHeads()
    collectAndPublishNotCrosslinkedBlocks()
  }

  /**
   * Groups blocks by shard and processes each group in a separate coroutine
   */
  private suspend fun processNewShardBlocks(blocks: List<SignedShardBlock>) = coroutineScope {
    blocks.groupBy { it.message.shard }.entries.forEach {
      launch {
        it.value.sortedBy { it.message.slot }
          .forEach { on_shard_block(store, shardStores[it.message.shard]!!, it) }
      }
    }
  }

  /**
   * Computes shards' heads concurrently and publishes them
   */
  private suspend fun collectAndPublishShardHeads() = coroutineScope {
    val res = (0uL until INITIAL_ACTIVE_SHARDS).map {
      async { get_shard_head(store, shardStores[it]!!) }
    }.awaitAll()

    publish(NewShardHeads(res))
  }

  /**
   * Uses flow to collect blocks for different shards concurrently
   */
  private suspend fun collectAndPublishNotCrosslinkedBlocks() = coroutineScope {
    val res = (0uL until INITIAL_ACTIVE_SHARDS).map {
      async { get_pending_shard_blocks(store, shardStores[it]!!) }
    }.awaitAll().flatten()

    publish(NotCrosslinkedBlocksPublished(res))
  }
}
