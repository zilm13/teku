package tech.pegasys.teku.phase1.simulator

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.SendChannel

abstract class Eth2Actor(private val eventBus: SendChannel<Eth2Event>) {
  protected suspend fun publish(event: Eth2Event) {
    if (!eventBus.isClosedForSend) {
      eventBus.send(event)
    }
  }

  internal abstract suspend fun dispatchImpl(event: Eth2Event, scope: CoroutineScope)
}
