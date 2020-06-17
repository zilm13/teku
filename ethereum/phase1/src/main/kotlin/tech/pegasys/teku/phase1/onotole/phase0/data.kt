package tech.pegasys.teku.phase1.onotole.phase0

import tech.pegasys.teku.phase1.integration.types.BLSPublicKeyType
import tech.pegasys.teku.phase1.integration.types.Bytes32Type
import tech.pegasys.teku.phase1.integration.types.Bytes4Type
import tech.pegasys.teku.phase1.integration.types.ReadOnlyTypePair
import tech.pegasys.teku.phase1.integration.types.SSZBitListType
import tech.pegasys.teku.phase1.integration.types.SSZBitVectorType
import tech.pegasys.teku.phase1.integration.types.SSZListType
import tech.pegasys.teku.phase1.integration.types.SSZVectorType
import tech.pegasys.teku.phase1.integration.types.UInt64Type
import tech.pegasys.teku.phase1.onotole.ssz.Bytes32
import tech.pegasys.teku.phase1.onotole.ssz.Bytes4
import tech.pegasys.teku.phase1.onotole.ssz.Bytes48
import tech.pegasys.teku.phase1.onotole.ssz.SSZBitList
import tech.pegasys.teku.phase1.onotole.ssz.SSZBitVector
import tech.pegasys.teku.phase1.onotole.ssz.SSZList
import tech.pegasys.teku.phase1.onotole.ssz.SSZVector
import tech.pegasys.teku.phase1.onotole.ssz.boolean
import tech.pegasys.teku.phase1.onotole.ssz.uint64

typealias Slot = uint64
typealias Epoch = uint64
typealias CommitteeIndex = uint64
typealias ValidatorIndex = uint64
typealias Gwei = uint64
typealias Root = Bytes32
typealias Version = Bytes4
typealias BLSPubkey = Bytes48

inline class Fork(private val v: tech.pegasys.teku.datastructures.state.Fork) {
  val previous_version: Version
    get() = Bytes4Type.wrap(v.previous_version)
  val current_version: Version
    get() = Bytes4Type.wrap(v.current_version)
  val epoch: Epoch
    get() = UInt64Type.wrap(v.epoch)
}

inline class Checkpoint(private val v: tech.pegasys.teku.datastructures.state.Checkpoint) {
  val epoch: Epoch
    get() = UInt64Type.wrap(v.epoch)
  val root: Root
    get() = Bytes32Type.wrap(v.root)
}

inline class Validator(private val v: tech.pegasys.teku.datastructures.state.Validator) {
  val pubkey: BLSPubkey
    get() = BLSPublicKeyType.wrap(v.pubkey)
  val withdrawal_credentials: Bytes32
    get() = v.withdrawal_credentials
  val effective_balance: Gwei
    get() = UInt64Type.wrap(v.effective_balance)
  val slashed: boolean
    get() = v.isSlashed
  val activation_eligibility_epoch: Epoch
    get() = UInt64Type.wrap(v.activation_eligibility_epoch)
  val activation_epoch: Epoch
    get() = UInt64Type.wrap(v.activation_epoch)
  val exit_epoch: Epoch
    get() = UInt64Type.wrap(v.exit_epoch)
  val withdrawable_epoch: Epoch
    get() = UInt64Type.wrap(v.withdrawable_epoch)
}

internal val ValidatorType =
  object : ReadOnlyTypePair<Validator, tech.pegasys.teku.datastructures.state.Validator> {
    override val teku = tech.pegasys.teku.datastructures.state.Validator::class
    override val onotole = Validator::class
    override fun wrap(v: tech.pegasys.teku.datastructures.state.Validator) = Validator(v)
  }

inline class AttestationData(private val v: tech.pegasys.teku.datastructures.operations.AttestationData) {
  val slot: Slot
    get() = UInt64Type.wrap(v.slot)
  val index: CommitteeIndex
    get() = UInt64Type.wrap(v.index)
  val beacon_block_root: Root
    get() = v.beacon_block_root
  val source: Checkpoint
    get() = Checkpoint(v.source)
  val target: Checkpoint
    get() = Checkpoint(v.target)
}

inline class PendingAttestation(private val v: tech.pegasys.teku.datastructures.state.PendingAttestation) {
  val aggregation_bits: SSZBitList
    get() = SSZBitListType.wrap(v.aggregation_bits)
  val data: AttestationData
    get() = AttestationData(v.data)
  val inclusion_delay: Slot
    get() = UInt64Type.wrap(v.inclusion_delay)
  val proposer_index: ValidatorIndex
    get() = UInt64Type.wrap(v.proposer_index)
}

internal val PendingAttestationType = object :
  ReadOnlyTypePair<PendingAttestation, tech.pegasys.teku.datastructures.state.PendingAttestation> {
  override val teku = tech.pegasys.teku.datastructures.state.PendingAttestation::class
  override val onotole = PendingAttestation::class
  override fun wrap(v: tech.pegasys.teku.datastructures.state.PendingAttestation) =
    PendingAttestation(v)
}

inline class Eth1Data(private val v: tech.pegasys.teku.datastructures.blocks.Eth1Data) {
  val deposit_root: Root
    get() = Bytes32Type.wrap(v.deposit_root)
  val deposit_count: uint64
    get() = UInt64Type.wrap(v.deposit_count)
  val block_hash: Bytes32
    get() = Bytes32Type.wrap(v.block_hash)
}

internal val Eth1DataType = object :
  ReadOnlyTypePair<Eth1Data, tech.pegasys.teku.datastructures.blocks.Eth1Data> {
  override val teku = tech.pegasys.teku.datastructures.blocks.Eth1Data::class
  override val onotole = Eth1Data::class
  override fun wrap(v: tech.pegasys.teku.datastructures.blocks.Eth1Data) = Eth1Data(v)
}

inline class BeaconBlockHeader(private val v: tech.pegasys.teku.datastructures.blocks.BeaconBlockHeader) {
  val slot: Slot
    get() = UInt64Type.wrap(v.slot)
  val proposer_index: ValidatorIndex
    get() = UInt64Type.wrap(v.proposer_index)
  val parent_root: Root
    get() = Bytes32Type.wrap(v.parent_root)
  val state_root: Root
    get() = Bytes32Type.wrap(v.state_root)
  val body_root: Root
    get() = Bytes32Type.wrap(v.body_root)
}

inline class BeaconState(private val v: tech.pegasys.teku.datastructures.state.BeaconState) {
  val genesis_time: uint64
    get() = UInt64Type.wrap(v.genesis_time)
  val genesis_validators_root: Root
    get() = v.genesis_validators_root
  val slot: Slot
    get() = UInt64Type.wrap(v.slot)
  val fork: Fork
    get() = Fork(v.fork)
  val latest_block_header: BeaconBlockHeader
    get() = BeaconBlockHeader(v.latest_block_header)
  val block_roots: SSZVector<Root>
    get() = SSZVectorType(Bytes32Type).wrap(v.block_roots)
  val state_roots: SSZVector<Root>
    get() = SSZVectorType(Bytes32Type).wrap(v.block_roots)
  val historical_roots: SSZList<Root>
    get() = SSZListType(Bytes32Type).wrap(v.historical_roots)
  val eth1_data: Eth1Data
    get() = Eth1DataType.wrap(v.eth1_data)
  val eth1_data_votes: SSZList<Eth1Data>
    get() = SSZListType(Eth1DataType).wrap(v.eth1_data_votes)
  val eth1_deposit_index: uint64
    get() = UInt64Type.wrap(v.eth1_deposit_index)
  val validators: SSZList<Validator>
    get() = SSZListType(ValidatorType).wrap(v.validators)
  val balances: SSZList<Gwei>
    get() = SSZListType(UInt64Type).wrap(v.balances)
  val randao_mixes: SSZVector<Bytes32>
    get() = SSZVectorType(Bytes32Type).wrap(v.randao_mixes)
  val slashings: SSZVector<Gwei>
    get() = SSZVectorType(UInt64Type).wrap(v.slashings)
  val previous_epoch_attestations: SSZList<PendingAttestation>
    get() = SSZListType(PendingAttestationType).wrap(v.previous_epoch_attestations)
  val current_epoch_attestations: SSZList<PendingAttestation>
    get() = SSZListType(PendingAttestationType).wrap(v.current_epoch_attestations)
  val justification_bits: SSZBitVector
    get() = SSZBitVectorType.wrap(v.justification_bits)
  val previous_justified_checkpoint: Checkpoint
    get() = Checkpoint(v.previous_justified_checkpoint)
  val current_justified_checkpoint: Checkpoint
    get() = Checkpoint(v.current_justified_checkpoint)
  val finalized_checkpoint: Checkpoint
    get() = Checkpoint(v.finalized_checkpoint)
}
