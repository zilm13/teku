package tech.pegasys.teku.phase1.core

import tech.pegasys.teku.phase1.ssz.Bytes32
import tech.pegasys.teku.phase1.ssz.Bytes4
import tech.pegasys.teku.phase1.ssz.Bytes48
import tech.pegasys.teku.phase1.ssz.Bytes96
import tech.pegasys.teku.phase1.ssz.CBitlist
import tech.pegasys.teku.phase1.ssz.CBitvector
import tech.pegasys.teku.phase1.ssz.CByteList
import tech.pegasys.teku.phase1.ssz.CDict
import tech.pegasys.teku.phase1.ssz.CList
import tech.pegasys.teku.phase1.ssz.CVector
import tech.pegasys.teku.phase1.ssz.SSZComposite
import tech.pegasys.teku.phase1.ssz.boolean
import tech.pegasys.teku.phase1.ssz.uint64
import tech.pegasys.teku.phase1.ssz.uint8

typealias Slot = uint64

fun Slot(x: uint64): Slot = x
fun Slot() = Slot(0uL)

typealias Epoch = uint64

fun Epoch(x: uint64): Epoch = x
fun Epoch() = Epoch(0uL)

typealias CommitteeIndex = uint64

fun CommitteeIndex(x: uint64): CommitteeIndex = x
fun CommitteeIndex() = CommitteeIndex(0uL)

typealias ValidatorIndex = uint64

fun ValidatorIndex(x: uint64): ValidatorIndex = x
fun ValidatorIndex() = ValidatorIndex(0uL)

typealias Gwei = uint64

fun Gwei(x: uint64): Gwei = x
fun Gwei() = Gwei(0uL)

typealias Root = Bytes32

fun Root(x: Bytes32): Root = x
fun Root() = Root(Bytes32())

typealias Version = Bytes4

fun Version(x: Bytes4): Version = x
fun Version() = Version(Bytes4())

typealias DomainType = Bytes4

fun DomainType(x: Bytes4): DomainType = x
fun DomainType() = DomainType(Bytes4())

typealias ForkDigest = Bytes4

fun ForkDigest(x: Bytes4): ForkDigest = x
fun ForkDigest() = ForkDigest(Bytes4())

typealias Domain = Bytes32

fun Domain(x: Bytes32): Domain = x
fun Domain() = Domain(Bytes32())

typealias BLSPubkey = Bytes48

fun BLSPubkey(x: Bytes48): BLSPubkey = x
fun BLSPubkey() = BLSPubkey(Bytes48())

typealias BLSSignature = Bytes96

fun BLSSignature(x: Bytes96): BLSSignature = x
fun BLSSignature() = BLSSignature(Bytes96())

typealias Shard = uint64

fun Shard(x: uint64): Shard = x
fun Shard() = Shard(0uL)

typealias OnlineEpochs = uint8

fun OnlineEpochs(x: uint8): OnlineEpochs = x
fun OnlineEpochs() = OnlineEpochs(0u.toUByte())

interface Fork : SSZComposite {
  val previous_version: Version
  val current_version: Version
  val epoch: Epoch
}

interface ForkData : SSZComposite {
  val current_version: Version
  val genesis_validators_root: Root
}

interface Checkpoint : SSZComposite {
  val epoch: Epoch
  val root: Root
}

interface Validator : SSZComposite {
  val pubkey: BLSPubkey
  val withdrawal_credentials: Bytes32
  var effective_balance: Gwei
  var slashed: boolean
  var activation_eligibility_epoch: Epoch
  var activation_epoch: Epoch
  var exit_epoch: Epoch
  var withdrawable_epoch: Epoch
  var next_custody_secret_to_reveal: uint64
  var max_reveal_lateness: Epoch
}

interface AttestationData {
  val slot: Slot
  val index: CommitteeIndex
  val beacon_block_root: Root
  val source: Checkpoint
  val target: Checkpoint
  val head_shard_root: Root
  val shard_transition_root: Root
}

interface PendingAttestation {
  val aggregation_bits: CBitlist
  val data: AttestationData
  val inclusion_delay: Slot
  val proposer_index: ValidatorIndex
  var crosslink_success: boolean
}

interface Eth1Data {
  var deposit_root: Root
  val deposit_count: uint64
  val block_hash: Bytes32
}

interface HistoricalBatch {
  val block_roots: CVector<Root>
  val state_roots: CVector<Root>
}

interface DepositMessage {
  val pubkey: BLSPubkey
  val withdrawal_credentials: Bytes32
  val amount: Gwei
}

interface DepositData {
  val pubkey: BLSPubkey
  val withdrawal_credentials: Bytes32
  val amount: Gwei
  val signature: BLSSignature
}

interface BeaconBlockHeader {
  val slot: Slot
  val proposer_index: ValidatorIndex
  val parent_root: Root
  var state_root: Root
  val body_root: Root

  fun copy(): BeaconBlockHeader
}

interface SigningRoot {
  val object_root: Root
  val domain: Domain
}

interface Attestation {
  val aggregation_bits: CBitlist
  val data: AttestationData
  val custody_bits_blocks: CList<CBitlist>
  val signature: BLSSignature
}

interface IndexedAttestation {
  val committee: CList<ValidatorIndex>
  val attestation: Attestation
}

interface AttesterSlashing {
  val attestation_1: IndexedAttestation
  val attestation_2: IndexedAttestation
}

interface Deposit {
  val proof: CVector<Bytes32>
  val data: DepositData
}

interface VoluntaryExit {
  val epoch: Epoch
  val validator_index: ValidatorIndex
}

interface SignedVoluntaryExit {
  val message: VoluntaryExit
  val signature: BLSSignature
}

interface SignedBeaconBlockHeader {
  val message: BeaconBlockHeader
  val signature: BLSSignature
}

interface ProposerSlashing {
  val signed_header_1: SignedBeaconBlockHeader
  val signed_header_2: SignedBeaconBlockHeader
}

interface CustodyKeyReveal {
  val revealer_index: ValidatorIndex
  val reveal: BLSSignature
}

interface EarlyDerivedSecretReveal {
  val revealed_index: ValidatorIndex
  val epoch: Epoch
  val reveal: BLSSignature
  val masker_index: ValidatorIndex
  val mask: Bytes32
}

interface ShardBlock {
  val shard_parent_root: Root
  val beacon_parent_root: Root
  val slot: Slot
  val proposer_index: ValidatorIndex
  val body: CByteList
}

interface SignedShardBlock {
  val message: ShardBlock
  val signature: BLSSignature
}

interface ShardBlockHeader {
  val shard_parent_root: Root
  val beacon_parent_root: Root
  val slot: Slot
  val proposer_index: ValidatorIndex
  val body_root: Root
}

interface ShardState {
  var slot: Slot
  var gasprice: Gwei
  var transition_digest: Bytes32
  var latest_block_root: Root

  fun copy(): ShardState
}

interface ShardTransition {
  val start_slot: Slot
  val shard_block_lengths: CList<uint64>
  val shard_data_roots: CList<Bytes32>
  val shard_states: CList<ShardState>
  val proposer_signature_aggregate: BLSSignature
}

interface CustodySlashing {
  val data_index: uint64
  val malefactor_index: ValidatorIndex
  val malefactor_secret: BLSSignature
  val whistleblower_index: ValidatorIndex
  val shard_transition: ShardTransition
  val attestation: Attestation
  val data: CByteList
}

interface SignedCustodySlashing {
  val message: CustodySlashing
  val signature: BLSSignature
}

interface BeaconBlockBody {
  val randao_reveal: BLSSignature
  val eth1_data: Eth1Data
  val graffiti: Bytes32
  val proposer_slashings: CList<ProposerSlashing>
  val attester_slashings: CList<AttesterSlashing>
  val attestations: CList<Attestation>
  val deposits: CList<Deposit>
  val voluntary_exits: CList<SignedVoluntaryExit>
  val custody_slashings: CList<SignedCustodySlashing>
  val custody_key_reveals: CList<CustodyKeyReveal>
  val early_derived_secret_reveals: CList<EarlyDerivedSecretReveal>
  val shard_transitions: CVector<ShardTransition>
  val light_client_signature_bitfield: CBitvector
  val light_client_signature: BLSSignature
}

interface BeaconBlock {
  val slot: Slot
  val proposer_index: ValidatorIndex
  val parent_root: Root
  val state_root: Root
  val body: BeaconBlockBody
}

interface SignedBeaconBlock {
  val message: BeaconBlock
  val signature: BLSSignature
}

interface CompactCommittee {
  val pubkeys: CList<BLSPubkey>
  val compact_validators: CList<uint64>
}

interface BeaconState {
  val genesis_time: uint64
  var genesis_validators_root: Root
  var slot: Slot
  var fork: Fork
  var latest_block_header: BeaconBlockHeader
  val block_roots: CVector<Root>
  val state_roots: CVector<Root>
  val historical_roots: CList<Root>
  var eth1_data: Eth1Data
  var eth1_data_votes: CList<Eth1Data>
  var eth1_deposit_index: uint64
  val validators: CList<Validator>
  val balances: CList<Gwei>
  val randao_mixes: CVector<Root>
  val slashings: CVector<Gwei>
  var previous_epoch_attestations: CList<PendingAttestation>
  var current_epoch_attestations: CList<PendingAttestation>
  val justification_bits: CBitvector
  var previous_justified_checkpoint: Checkpoint
  var current_justified_checkpoint: Checkpoint
  var finalized_checkpoint: Checkpoint
  val shard_states: CList<ShardState>
  val online_countdown: CList<OnlineEpochs>
  var current_light_committee: CompactCommittee
  var next_light_committee: CompactCommittee
  val exposed_derived_secrets: CVector<CListOfValidatorIndices>

  fun copy(): BeaconState
}

interface AttestationCustodyBitWrapper {
  val attestation_data_root: Root
  val block_index: uint64
  val bit: boolean
}

interface LatestMessage {
  val epoch: Epoch
  val root: Root
}

interface Store {
  var time: uint64
  val genesis_time: uint64
  var justified_checkpoint: Checkpoint
  var finalized_checkpoint: Checkpoint
  var best_justified_checkpoint: Checkpoint
  var blocks: CDict<Root, BeaconBlockHeader>
  var block_states: CDict<Root, BeaconState>
  var checkpoint_states: CDict<Checkpoint, BeaconState>
  var latest_messages: CDict<ValidatorIndex, LatestMessage>
}

class CListOfValidatorIndices(max_size: ULong, items: MutableList<ValidatorIndex>)
  : CList<ValidatorIndex>(ValidatorIndex::class, max_size, items) {
  constructor(max_size: ULong) : this(max_size, mutableListOf())
}
