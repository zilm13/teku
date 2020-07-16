package tech.pegasys.teku.phase1.simulator

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.SendChannel
import tech.pegasys.teku.phase1.onotole.phase1.Slot

class SlotTicker(
  eventBus: SendChannel<Eth2Event>,
  private val slotsToRun: Slot
) : Eth2Actor(eventBus) {

  override suspend fun dispatchImpl(event: Eth2Event, scope: CoroutineScope) {
    when (event) {
      is SlotTerminal -> onSlotTerminal(event.slot)
    }
  }

  private suspend fun onSlotTerminal(slot: Slot) {
    println("[${Thread.currentThread().name}] Publish NewSlot(${slot + 1uL})")

    if (!runsOutOfSlots(slot, slotsToRun)) {
      publish(NewSlot(slot + 1uL))
    }
  }
}
