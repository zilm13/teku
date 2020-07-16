package tech.pegasys.teku.phase1.simulation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import tech.pegasys.teku.datastructures.util.MockStartValidatorKeyPairFactory
import tech.pegasys.teku.phase1.simulation.actors.BeaconAttester
import tech.pegasys.teku.phase1.simulation.actors.BeaconProposer
import tech.pegasys.teku.phase1.simulation.actors.DelayedAttestationsPark
import tech.pegasys.teku.phase1.simulation.actors.Eth2ChainProcessor
import tech.pegasys.teku.phase1.simulation.actors.ShardProposer
import tech.pegasys.teku.phase1.simulation.actors.SlotTicker
import tech.pegasys.teku.phase1.simulation.util.SecretKeyRegistry
import tech.pegasys.teku.phase1.simulation.util.getGenesisState
import tech.pegasys.teku.phase1.simulation.util.getGenesisStore
import tech.pegasys.teku.phase1.simulation.util.getShardGenesisStores
import tech.pegasys.teku.phase1.simulation.util.runsOutOfSlots

class Phase1Simulation(
  slotsToRun: ULong,
  validatorRegistrySize: Int,
  private val scope: CoroutineScope
) {
  private val eventBus: Channel<Eth2Event> = Channel(Channel.UNLIMITED)
  private val terminator = object : Eth2Actor(eventBus) {
    override suspend fun dispatchImpl(event: Eth2Event, scope: CoroutineScope) {
      // stop simulation when the last slot has been processed
      if (event is SlotTerminal && runsOutOfSlots(
          event.slot,
          slotsToRun.toULong()
        )
      ) {
        stop()
      }
    }
  }

  private val actors: List<Eth2Actor>

  init {
    val blsKeyPairs = MockStartValidatorKeyPairFactory().generateKeyPairs(0, validatorRegistrySize)
    val genesisState = getGenesisState(blsKeyPairs)
    val store = getGenesisStore(genesisState)
    val shardStores = getShardGenesisStores(genesisState)
    val secretKeys = SecretKeyRegistry(blsKeyPairs)

    actors = listOf(
      SlotTicker(eventBus, slotsToRun),
      Eth2ChainProcessor(eventBus, store, shardStores),
      BeaconProposer(eventBus, secretKeys),
      ShardProposer(eventBus, secretKeys),
      BeaconAttester(eventBus, secretKeys),
      DelayedAttestationsPark(eventBus),
      terminator
    )
  }

  suspend fun start() {
    eventLoop(actors, scope)
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
