package tech.pegasys.teku.phase1.simulator

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import tech.pegasys.teku.phase1.onotole.phase1.Slot

class Phase1Simulation(
  private val slotsToRun: Slot,
  private val scope: CoroutineScope
) {
  //  private val beaconProposer: BeaconProposer
  private val slotTicker: SlotTicker
  private val eventBus: Channel<Eth2Event> = Channel(Channel.UNLIMITED)
  private val terminator = object : Eth2Actor(eventBus) {
    override suspend fun dispatchImpl(event: Eth2Event, scope: CoroutineScope) {
      // stop simulation when the last slot has been processed
      if (event is SlotTerminal && runsOutOfSlots(event.slot, slotsToRun)) {
        stop()
      }
    }
  }

  init {
    this.slotTicker = SlotTicker(eventBus, slotsToRun)
//    this.beaconProposer = BeaconProposer(eventBus,)
  }

  suspend fun start() {
    eventLoop(listOf(terminator, slotTicker, SlotTerminator(eventBus)), scope)
    eventBus.send(GenesisSlotEvent)
  }

  fun stop() {
    eventBus.close()
  }

  /**
   * A concurrent event loop
   */
  private suspend fun eventLoop(actors: List<Eth2Actor>, scope: CoroutineScope) = scope.launch {
    for (event in eventBus) {
      supervisorScope {
        actors.forEach {
          launch { it.dispatchImpl(event, scope) }
        }
      }
    }
  }
}
