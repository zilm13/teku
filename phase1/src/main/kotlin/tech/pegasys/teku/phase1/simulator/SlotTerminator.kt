package tech.pegasys.teku.phase1.simulator

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.SendChannel

class SlotTerminator(
  eventBus: SendChannel<Eth2Event>
) : Eth2Actor(eventBus) {

  override suspend fun dispatchImpl(event: Eth2Event, scope: CoroutineScope) {
    when (event) {
      is NewSlot -> {
        println("[${Thread.currentThread().name}] Publish SlotTerminal(${event.slot})")
        publish(SlotTerminal(event.slot))
      }
    }
  }
}
