package tech.pegasys.teku.phase1

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import tech.pegasys.teku.phase1.onotole.phase1.SLOTS_PER_EPOCH
import tech.pegasys.teku.phase1.simulation.Phase1Simulation

fun main() = runBlocking<Unit> {
  val scope = CoroutineScope(coroutineContext + Dispatchers.Default)
  val simulation = Phase1Simulation(128uL * SLOTS_PER_EPOCH, 16, scope)
  simulation.start()
}
