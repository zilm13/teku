package tech.pegasys.teku.phase1.simulation.actors

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.SendChannel
import tech.pegasys.teku.phase1.onotole.phase1.Slot
import tech.pegasys.teku.phase1.simulation.Eth2Actor
import tech.pegasys.teku.phase1.simulation.Eth2Event
import tech.pegasys.teku.phase1.simulation.NewSlot
import tech.pegasys.teku.phase1.simulation.SlotTerminal
import tech.pegasys.teku.phase1.simulation.util.runsOutOfSlots

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
    if (!runsOutOfSlots(slot, slotsToRun)) {
      publish(NewSlot(slot + 1uL))
    }
  }
}
