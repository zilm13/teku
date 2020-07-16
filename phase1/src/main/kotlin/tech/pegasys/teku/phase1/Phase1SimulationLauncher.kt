package tech.pegasys.teku.phase1

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import tech.pegasys.teku.phase1.simulator.Phase1Simulation

fun main() = runBlocking<Unit> {
  val scope = CoroutineScope(coroutineContext + Dispatchers.Default)
  val simulation = Phase1Simulation(1000uL, scope).also { it.start() }
}
