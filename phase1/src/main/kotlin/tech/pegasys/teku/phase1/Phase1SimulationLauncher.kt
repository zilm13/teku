package tech.pegasys.teku.phase1

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.bouncycastle.jce.provider.BouncyCastleProvider
import tech.pegasys.teku.phase1.onotole.phase1.SLOTS_PER_EPOCH
import tech.pegasys.teku.phase1.simulation.Phase1Simulation
import java.security.Security

fun main() = runBlocking<Unit> {
  Security.addProvider(BouncyCastleProvider())
  val scope = CoroutineScope(coroutineContext + Dispatchers.Default)
  val simulation = Phase1Simulation(scope) {
    it.slotsToRun = 128uL * SLOTS_PER_EPOCH
    it.validatorRegistrySize = 2048
    it.debug = false
    it.bls = Phase1Simulation.BLSConfig.Pseudo
  }
  simulation.start()
}
