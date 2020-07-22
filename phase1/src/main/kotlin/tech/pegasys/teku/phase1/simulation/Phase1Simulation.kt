package tech.pegasys.teku.phase1.simulation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import tech.pegasys.teku.datastructures.util.MockStartValidatorKeyPairFactory
import tech.pegasys.teku.phase1.eth1client.Eth1EngineClient
import tech.pegasys.teku.phase1.eth1client.stub.Eth1EngineClientStub
import tech.pegasys.teku.phase1.eth1client.withLogger
import tech.pegasys.teku.phase1.onotole.phase1.SLOTS_PER_EPOCH
import tech.pegasys.teku.phase1.simulation.actors.BeaconAttester
import tech.pegasys.teku.phase1.simulation.actors.BeaconProposer
import tech.pegasys.teku.phase1.simulation.actors.DelayedAttestationsPark
import tech.pegasys.teku.phase1.simulation.actors.Eth2ChainProcessor
import tech.pegasys.teku.phase1.simulation.actors.ShardProposer
import tech.pegasys.teku.phase1.simulation.actors.SlotTicker
import tech.pegasys.teku.phase1.simulation.util.SecretKeyRegistry
import tech.pegasys.teku.phase1.simulation.util.SimulationRandomness
import tech.pegasys.teku.phase1.simulation.util.getGenesisState
import tech.pegasys.teku.phase1.simulation.util.getGenesisStore
import tech.pegasys.teku.phase1.simulation.util.getShardGenesisStores
import tech.pegasys.teku.phase1.simulation.util.runsOutOfSlots
import tech.pegasys.teku.phase1.util.log
import tech.pegasys.teku.phase1.util.logDebug
import tech.pegasys.teku.phase1.util.logSetDebugMode

class Phase1Simulation(
  private val scope: CoroutineScope,
  private val config: Config
) {
  private val eventBus: Channel<Eth2Event> = Channel(Channel.UNLIMITED)
  private val terminator = object : Eth2Actor(eventBus) {
    override suspend fun dispatchImpl(event: Eth2Event, scope: CoroutineScope) {
      // stop simulation when the last slot has been processed
      if (event is SlotTerminal && runsOutOfSlots(
          event.slot,
          config.slotsToRun.toULong()
        )
      ) {
        stop()
      }
    }
  }

  private val actors: List<Eth2Actor>

  init {
    logSetDebugMode(config.debug)

    log("Initializing ${config.validatorRegistrySize} BLS Key Pairs...")
    val blsKeyPairs =
      MockStartValidatorKeyPairFactory().generateKeyPairs(0, config.validatorRegistrySize)

    log("Initializing genesis state and store...")
    val genesisState = getGenesisState(blsKeyPairs)
    val store = getGenesisStore(genesisState)
    val shardStores = getShardGenesisStores(genesisState)
    val secretKeys = SecretKeyRegistry(blsKeyPairs)
    val proposerEth1Engine = config.proposerEth1Engine.withLogger("ProposerEth1Engine")
    val processorEth1Engine = config.processorEth1Engine.withLogger("ProcessorEth1Engine")

    actors = listOf(
      SlotTicker(eventBus, config.slotsToRun),
      Eth2ChainProcessor(eventBus, store, shardStores, processorEth1Engine),
      BeaconProposer(eventBus, secretKeys),
      ShardProposer(eventBus, secretKeys, proposerEth1Engine),
      BeaconAttester(eventBus, secretKeys),
      DelayedAttestationsPark(eventBus),
      terminator
    )
  }

  suspend fun start() {
    log("Starting simulation...")
    eventLoop(actors, scope)
    eventBus.send(GenesisSlotEvent)
  }

  fun stop() {
    eventBus.close()
    log("Simulation stopped")
  }

  /**
   * A concurrent event loop
   */
  private fun eventLoop(actors: List<Eth2Actor>, scope: CoroutineScope) = scope.launch {
    for (event in eventBus) {
      logDebug("Dispatch $event")
      coroutineScope {
        actors.forEach {
          launch { it.dispatchImpl(event, scope) }
        }
      }
    }
  }

  data class Config(
    var slotsToRun: ULong = 2uL * SLOTS_PER_EPOCH,
    var validatorRegistrySize: Int = 16,
    var proposerEth1Engine: Eth1EngineClient = Eth1EngineClientStub(SimulationRandomness),
    var processorEth1Engine: Eth1EngineClient = Eth1EngineClientStub(SimulationRandomness),
    var debug: Boolean = false
  )
}

@Suppress("FunctionName")
fun Phase1Simulation(
  scope: CoroutineScope,
  userConfig: (Phase1Simulation.Config) -> Unit
): Phase1Simulation {
  val config = Phase1Simulation.Config()
  userConfig(config)
  return Phase1Simulation(scope, config)
}
