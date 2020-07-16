package tech.pegasys.teku.phase1.simulation.actors

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.coroutineScope
import tech.pegasys.teku.phase1.onotole.phase1.GENESIS_SLOT
import tech.pegasys.teku.phase1.onotole.phase1.INITIAL_ACTIVE_SHARDS
import tech.pegasys.teku.phase1.onotole.phase1.Root
import tech.pegasys.teku.phase1.onotole.phase1.Slot
import tech.pegasys.teku.phase1.simulation.BeaconHead
import tech.pegasys.teku.phase1.simulation.Eth2Actor
import tech.pegasys.teku.phase1.simulation.Eth2Event
import tech.pegasys.teku.phase1.simulation.HeadAfterNewBeaconBlock
import tech.pegasys.teku.phase1.simulation.NewShardBlocks
import tech.pegasys.teku.phase1.simulation.NewShardHeads
import tech.pegasys.teku.phase1.simulation.NewSlot
import tech.pegasys.teku.phase1.simulation.util.SecretKeyRegistry
import tech.pegasys.teku.phase1.simulation.util.getRandomShardBlockBody
import tech.pegasys.teku.phase1.simulation.util.produceShardBlock

class ShardProposer(
  eventBus: SendChannel<Eth2Event>,
  private val secretKeys: SecretKeyRegistry
) : Eth2Actor(eventBus) {

  private var recentSlot = GENESIS_SLOT
  private var recentShardHeads = List(INITIAL_ACTIVE_SHARDS.toInt()) { Root() }

  override suspend fun dispatchImpl(event: Eth2Event, scope: CoroutineScope) {
    when (event) {
      is NewSlot -> onNewSlot(event.slot)
      is NewShardHeads -> onNewShardHeads(event.shardHeadRoots)
      is HeadAfterNewBeaconBlock -> onHeadAfterNewBeaconBlock(event.head)
    }
  }

  private fun onNewSlot(slot: Slot) {
    this.recentSlot = slot
  }

  private fun onNewShardHeads(headRoots: List<Root>) {
    this.recentShardHeads = headRoots
  }

  private suspend fun onHeadAfterNewBeaconBlock(head: BeaconHead) = coroutineScope {
    val newShardBlocks = (0uL until INITIAL_ACTIVE_SHARDS).map {
      async {
        val randomBody = getRandomShardBlockBody()
        produceShardBlock(
          recentSlot,
          it,
          recentShardHeads[it.toInt()],
          head.root,
          head.state,
          randomBody,
          secretKeys
        )
      }
    }.awaitAll()

    publish(NewShardBlocks(newShardBlocks))
  }
}
