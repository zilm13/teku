package tech.pegasys.teku.simulation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import tech.pegasys.teku.simulation.util.InsecureRandom

val RANDOM = InsecureRandom(ByteArray(1) { 1 }).apply {
    setInsecureSeed(1)
}
const val PEER_COUNT = 10000
const val BLOCK_COUNT = 1_000_000uL

fun main() = runBlocking<Unit> {
    val scope = CoroutineScope(coroutineContext + Dispatchers.Default)
    val simulation = SyncSimulation(scope) {
        it.rnd = RANDOM
        it.peers = PEER_COUNT
        it.blockCount = BLOCK_COUNT
    }
    simulation.start()
}
