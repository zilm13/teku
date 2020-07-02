package tech.pegasys.teku.phase1

import org.apache.tuweni.bytes.Bytes
import org.apache.tuweni.bytes.Bytes32
import tech.pegasys.teku.bls.BLSSecretKey
import tech.pegasys.teku.datastructures.util.MockStartBeaconStateGenerator
import tech.pegasys.teku.datastructures.util.MockStartDepositGenerator
import tech.pegasys.teku.datastructures.util.MockStartValidatorKeyPairFactory
import tech.pegasys.teku.phase1.integration.datastructures.BeaconBlock
import tech.pegasys.teku.phase1.integration.datastructures.BeaconBlockBody
import tech.pegasys.teku.phase1.integration.datastructures.BeaconState
import tech.pegasys.teku.phase1.integration.datastructures.SignedBeaconBlock
import tech.pegasys.teku.phase1.integration.toUnsignedLong
import tech.pegasys.teku.phase1.onotole.phase1.Root
import tech.pegasys.teku.phase1.onotole.phase1.SLOTS_PER_EPOCH
import tech.pegasys.teku.phase1.onotole.phase1.get_beacon_proposer_index
import tech.pegasys.teku.phase1.onotole.phase1.get_block_signature
import tech.pegasys.teku.phase1.onotole.phase1.get_epoch_signature
import tech.pegasys.teku.phase1.onotole.phase1.process_slots
import tech.pegasys.teku.phase1.onotole.phase1.state_transition
import tech.pegasys.teku.phase1.onotole.phase1.upgrade_to_phase1
import tech.pegasys.teku.phase1.onotole.pylib.pyint
import tech.pegasys.teku.phase1.onotole.ssz.uint64
import java.math.BigInteger
import tech.pegasys.teku.datastructures.blocks.BeaconBlock as Phase0Block

private val SLOTS = 64uL * SLOTS_PER_EPOCH
private val blsKeyPairs = MockStartValidatorKeyPairFactory().generateKeyPairs(0, 16)

fun main() {
  var state = getGenesisState()
  val genesis = Phase0Block(state.hashTreeRoot())
  var parentRoot = genesis.hash_tree_root()
  for (slot in 1uL..SLOTS) {
    val signedBlock = produceBlock(state.copy(), slot, parentRoot)
    parentRoot = signedBlock.message.hashTreeRoot()
    state = state_transition(state, signedBlock)
    state = state.applyChanges()

    println("Slot $slot: block = $signedBlock, state = $state")
    if (slot % SLOTS_PER_EPOCH == 0uL) {
      println(state.balances.mapIndexed { index, balance -> "$index: $balance" }.joinToString { it })
    }
  }
}

fun produceBlock(state: BeaconState, slot: uint64, parentRoot: Root): SignedBeaconBlock {
  val advancedState = state.copy()
  if (advancedState.slot < slot) {
    process_slots(advancedState, slot)
  }
  val proposerIndex = get_beacon_proposer_index(advancedState)
  val proposerSecretKey = blsKeyPairs[proposerIndex.toInt()].secretKey.toPyint()
  val blockHeader = BeaconBlock(
    slot,
    proposerIndex,
    parentRoot,
    Bytes32.ZERO,
    BeaconBlockBody()
  )
  val randaoReveal = get_epoch_signature(advancedState, blockHeader, proposerSecretKey)
  val block = BeaconBlock(
    slot,
    proposerIndex,
    parentRoot,
    Bytes32.ZERO,
    BeaconBlockBody(
      randaoReveal, state.eth1_data, Bytes32.rightPad(Bytes.ofUnsignedLong(proposerIndex.toLong()))
    )
  )
  val endState = state_transition(state.copy(), SignedBeaconBlock(block), false)
  val blockWithStateRoot = block.copy(state_root = endState.hashTreeRoot())
  val signature = get_block_signature(state, blockWithStateRoot, proposerSecretKey)

  return SignedBeaconBlock(blockWithStateRoot, signature)
}

fun getGenesisState(): BeaconState {
  val deposits = MockStartDepositGenerator().createDeposits(blsKeyPairs)
  val state = upgrade_to_phase1(
    MockStartBeaconStateGenerator().createInitialBeaconState(
      0uL.toUnsignedLong(),
      deposits
    )
  )
  return state.applyChanges()
}

private fun BLSSecretKey.toPyint() = pyint(BigInteger(1, this.secretKey.toBytes().toArray()))
