package tech.pegasys.teku.phase1.simulation.util

import tech.pegasys.teku.bls.BLSKeyPair
import tech.pegasys.teku.datastructures.util.BeaconStateUtil
import tech.pegasys.teku.datastructures.util.MockStartBeaconStateGenerator
import tech.pegasys.teku.datastructures.util.MockStartDepositGenerator
import tech.pegasys.teku.phase1.integration.datastructures.BeaconState
import tech.pegasys.teku.phase1.integration.toUnsignedLong
import tech.pegasys.teku.phase1.onotole.phase1.INITIAL_ACTIVE_SHARDS
import tech.pegasys.teku.phase1.onotole.phase1.get_forkchoice_shard_store
import tech.pegasys.teku.phase1.onotole.phase1.get_forkchoice_store
import tech.pegasys.teku.phase1.onotole.phase1.upgrade_to_phase1

fun getGenesisState(blsKeyPairs: List<BLSKeyPair>): BeaconState {
  BeaconStateUtil.BLS_VERIFY_DEPOSIT = false
  val deposits = MockStartDepositGenerator().createDeposits(blsKeyPairs)
  val state = upgrade_to_phase1(
    MockStartBeaconStateGenerator().createInitialBeaconState(
      0uL.toUnsignedLong(),
      deposits
    )
  )
  return state.applyChanges()
}

fun getGenesisStore(state: BeaconState) = get_forkchoice_store(state)

fun getShardGenesisStores(state: BeaconState) =
  (0uL until INITIAL_ACTIVE_SHARDS).map { it to get_forkchoice_shard_store(state, it) }.toMap()
