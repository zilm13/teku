package tech.pegasys.teku.phase1

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.bouncycastle.jce.provider.BouncyCastleProvider
import tech.pegasys.teku.phase1.eth1client.Web3jEth1EngineClient
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
//    it.proposerEth1Engine = Web3jEth1EngineClient("http://127.0.0.1:8545/", scope.coroutineContext)
//    it.processorEth1Engine = Web3jEth1EngineClient("http://127.0.0.1:8546/", scope.coroutineContext)
  }
  simulation.start()
}
