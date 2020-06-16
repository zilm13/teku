package tech.pegasys.teku.phase1.onotole.phase1

import tech.pegasys.teku.phase1.onotole.ssz.Bytes32
import tech.pegasys.teku.phase1.onotole.ssz.Bytes4
import tech.pegasys.teku.phase1.onotole.ssz.Bytes48
import tech.pegasys.teku.phase1.onotole.ssz.Bytes96
import tech.pegasys.teku.phase1.onotole.ssz.CDict
import tech.pegasys.teku.phase1.onotole.ssz.SSZBitList
import tech.pegasys.teku.phase1.onotole.ssz.SSZBitVector
import tech.pegasys.teku.phase1.onotole.ssz.SSZByteList
import tech.pegasys.teku.phase1.onotole.ssz.SSZComposite
import tech.pegasys.teku.phase1.onotole.ssz.SSZList
import tech.pegasys.teku.phase1.onotole.ssz.SSZMutableList
import tech.pegasys.teku.phase1.onotole.ssz.SSZMutableVector
import tech.pegasys.teku.phase1.onotole.ssz.SSZObjectFactory
import tech.pegasys.teku.phase1.onotole.ssz.SSZVector
import tech.pegasys.teku.phase1.onotole.ssz.boolean
import tech.pegasys.teku.phase1.onotole.ssz.uint64
import tech.pegasys.teku.phase1.onotole.ssz.uint8

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

interface ExposedValidatorIndices : SSZMutableList<ValidatorIndex>

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

interface AttestationData : SSZComposite {
  val slot: Slot
  val index: CommitteeIndex
  val beacon_block_root: Root
  val source: Checkpoint
  val target: Checkpoint
  val shard_head_root: Root
  val shard_transition_root: Root
}

interface PendingAttestation : SSZComposite {
  val aggregation_bits: SSZBitList
  val data: AttestationData
  val inclusion_delay: Slot
  val proposer_index: ValidatorIndex
  var crosslink_success: boolean
}

interface Eth1Data : SSZComposite {
  var deposit_root: Root
  val deposit_count: uint64
  val block_hash: Bytes32
}

interface HistoricalBatch : SSZComposite {
  val block_roots: SSZVector<Root>
  val state_roots: SSZVector<Root>
}

interface DepositMessage : SSZComposite {
  val pubkey: BLSPubkey
  val withdrawal_credentials: Bytes32
  val amount: Gwei
}

interface DepositData : SSZComposite {
  val pubkey: BLSPubkey
  val withdrawal_credentials: Bytes32
  val amount: Gwei
  val signature: BLSSignature
}

interface BeaconBlockHeader : SSZComposite {
  val slot: Slot
  val proposer_index: ValidatorIndex
  val parent_root: Root
  var state_root: Root
  val body_root: Root

  fun copy(
    slot: Slot = this.slot,
    proposer_index: ValidatorIndex = this.proposer_index,
    parent_root: Root = this.parent_root,
    state_root: Root = this.state_root,
    body_root: Root = this.body_root
  ): BeaconBlockHeader
}

interface SigningRoot : SSZComposite {
  val object_root: Root
  val domain: Domain
}

interface Attestation : SSZComposite {
  val aggregation_bits: SSZBitList
  val data: AttestationData
  val custody_bits_blocks: SSZList<SSZBitList>
  val signature: BLSSignature
}

interface IndexedAttestation : SSZComposite {
  val committee: SSZList<ValidatorIndex>
  val attestation: Attestation
}

interface AttesterSlashing : SSZComposite {
  val attestation_1: IndexedAttestation
  val attestation_2: IndexedAttestation
}

interface Deposit : SSZComposite {
  val proof: SSZVector<Bytes32>
  val data: DepositData
}

interface VoluntaryExit : SSZComposite {
  val epoch: Epoch
  val validator_index: ValidatorIndex
}

interface SignedVoluntaryExit : SSZComposite {
  val message: VoluntaryExit
  val signature: BLSSignature
}

interface SignedBeaconBlockHeader : SSZComposite {
  val message: BeaconBlockHeader
  val signature: BLSSignature
}

interface ProposerSlashing : SSZComposite {
  val signed_header_1: SignedBeaconBlockHeader
  val signed_header_2: SignedBeaconBlockHeader
}

interface CustodyKeyReveal : SSZComposite {
  val revealer_index: ValidatorIndex
  val reveal: BLSSignature
}

interface EarlyDerivedSecretReveal : SSZComposite {
  val revealed_index: ValidatorIndex
  val epoch: Epoch
  val reveal: BLSSignature
  val masker_index: ValidatorIndex
  val mask: Bytes32
}

interface ShardBlock : SSZComposite {
  val shard_parent_root: Root
  val beacon_parent_root: Root
  val slot: Slot
  val proposer_index: ValidatorIndex
  val body: SSZByteList
}

interface SignedShardBlock : SSZComposite {
  val message: ShardBlock
  val signature: BLSSignature
}

interface ShardBlockHeader : SSZComposite {
  val shard_parent_root: Root
  val beacon_parent_root: Root
  val slot: Slot
  val proposer_index: ValidatorIndex
  val body_root: Root
}

interface ShardState : SSZComposite {
  var slot: Slot
  var gasprice: Gwei
  var transition_digest: Bytes32
  var latest_block_root: Root

  fun copy(
    slot: Slot = this.slot,
    gasprice: Gwei = this.gasprice,
    transition_digest: Bytes32 = this.transition_digest,
    latest_block_root: Root = this.latest_block_root
  ): ShardState
}

interface ShardTransition : SSZComposite {
  val start_slot: Slot
  val shard_block_lengths: SSZList<uint64>
  val shard_data_roots: SSZList<Bytes32>
  val shard_states: SSZList<ShardState>
  val proposer_signature_aggregate: BLSSignature
}

interface CustodySlashing : SSZComposite {
  val data_index: uint64
  val malefactor_index: ValidatorIndex
  val malefactor_secret: BLSSignature
  val whistleblower_index: ValidatorIndex
  val shard_transition: ShardTransition
  val attestation: Attestation
  val data: SSZByteList
}

interface SignedCustodySlashing : SSZComposite {
  val message: CustodySlashing
  val signature: BLSSignature
}

interface BeaconBlockBody : SSZComposite {
  val randao_reveal: BLSSignature
  val eth1_data: Eth1Data
  val graffiti: Bytes32
  val proposer_slashings: SSZList<ProposerSlashing>
  val attester_slashings: SSZList<AttesterSlashing>
  val attestations: SSZList<Attestation>
  val deposits: SSZList<Deposit>
  val voluntary_exits: SSZList<SignedVoluntaryExit>
  val custody_slashings: SSZList<SignedCustodySlashing>
  val custody_key_reveals: SSZList<CustodyKeyReveal>
  val early_derived_secret_reveals: SSZList<EarlyDerivedSecretReveal>
  val shard_transitions: SSZVector<ShardTransition>
  val light_client_signature_bitfield: SSZBitVector
  val light_client_signature: BLSSignature
}

interface BeaconBlock : SSZComposite {
  val slot: Slot
  val proposer_index: ValidatorIndex
  val parent_root: Root
  val state_root: Root
  val body: BeaconBlockBody
}

interface SignedBeaconBlock : SSZComposite {
  val message: BeaconBlock
  val signature: BLSSignature
}

interface CompactCommittee : SSZComposite {
  val pubkeys: SSZList<BLSPubkey>
  val compact_validators: SSZList<uint64>
}

interface BeaconState : SSZComposite {
  val genesis_time: uint64
  var genesis_validators_root: Root
  var slot: Slot
  var fork: Fork
  var latest_block_header: BeaconBlockHeader
  val block_roots: SSZMutableVector<Root>
  val state_roots: SSZMutableVector<Root>
  val historical_roots: SSZMutableList<Root>
  var eth1_data: Eth1Data
  var eth1_data_votes: SSZMutableList<Eth1Data>
  var eth1_deposit_index: uint64
  val validators: SSZMutableList<Validator>
  val balances: SSZMutableList<Gwei>
  val randao_mixes: SSZMutableVector<Root>
  val slashings: SSZMutableVector<Gwei>
  var previous_epoch_attestations: SSZMutableList<PendingAttestation>
  var current_epoch_attestations: SSZMutableList<PendingAttestation>
  val justification_bits: SSZBitVector
  var previous_justified_checkpoint: Checkpoint
  var current_justified_checkpoint: Checkpoint
  var finalized_checkpoint: Checkpoint
  val shard_states: SSZMutableList<ShardState>
  val online_countdown: SSZMutableList<OnlineEpochs>
  var current_light_committee: CompactCommittee
  var next_light_committee: CompactCommittee
  val exposed_derived_secrets: SSZMutableVector<ExposedValidatorIndices>

  fun copy(
    genesis_time: uint64 = this.genesis_time,
    genesis_validators_root: Root = this.genesis_validators_root,
    slot: Slot = this.slot,
    fork: Fork = this.fork,
    latest_block_header: BeaconBlockHeader = this.latest_block_header,
    block_roots: SSZMutableVector<Root> = this.block_roots,
    state_roots: SSZMutableVector<Root> = this.state_roots,
    historical_roots: SSZMutableList<Root> = this.historical_roots,
    eth1_data: Eth1Data = this.eth1_data,
    eth1_data_votes: SSZMutableList<Eth1Data> = this.eth1_data_votes,
    eth1_deposit_index: uint64 = this.eth1_deposit_index,
    validators: SSZMutableList<Validator> = this.validators,
    balances: SSZMutableList<Gwei> = this.balances,
    randao_mixes: SSZMutableVector<Root> = this.randao_mixes,
    slashings: SSZMutableVector<Gwei> = this.slashings,
    previous_epoch_attestations: SSZMutableList<PendingAttestation> = this.previous_epoch_attestations,
    current_epoch_attestations: SSZMutableList<PendingAttestation> = this.current_epoch_attestations,
    justification_bits: SSZBitVector = this.justification_bits,
    previous_justified_checkpoint: Checkpoint = this.previous_justified_checkpoint,
    current_justified_checkpoint: Checkpoint = this.current_justified_checkpoint,
    finalized_checkpoint: Checkpoint = this.finalized_checkpoint,
    shard_states: SSZMutableList<ShardState> = this.shard_states,
    online_countdown: SSZMutableList<OnlineEpochs> = this.online_countdown,
    current_light_committee: CompactCommittee = this.current_light_committee,
    next_light_committee: CompactCommittee = this.next_light_committee,
    exposed_derived_secrets: SSZMutableVector<ExposedValidatorIndices> = this.exposed_derived_secrets
  ): BeaconState
}

interface AttestationCustodyBitWrapper : SSZComposite {
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

interface DataObjectFactory {

  fun sszFactory(): SSZObjectFactory

  fun Fork(
    previous_version: Version = Version(),
    current_version: Version = Version(),
    epoch: Epoch = Epoch()
  ): Fork

  fun ForkData(
    current_version: Version = Version(),
    genesis_validators_root: Root = Root()
  ): ForkData

  fun Checkpoint(epoch: Epoch = Epoch(), root: Root = Root()): Checkpoint
  fun Validator(
    pubkey: BLSPubkey = BLSPubkey(),
    withdrawal_credentials: Bytes32 = Bytes32(),
    effective_balance: Gwei = Gwei(),
    slashed: boolean = false,
    activation_eligibility_epoch: Epoch = Epoch(),
    activation_epoch: Epoch = Epoch(),
    exit_epoch: Epoch = Epoch(),
    withdrawable_epoch: Epoch = Epoch(),
    next_custody_secret_to_reveal: uint64 = 0uL,
    max_reveal_lateness: Epoch = Epoch()
  ): Validator

  fun AttestationData(
    slot: Slot = Slot(),
    index: CommitteeIndex = CommitteeIndex(),
    beacon_block_root: Root = Root(),
    source: Checkpoint = Checkpoint(),
    target: Checkpoint = Checkpoint(),
    head_shard_root: Root = Root(),
    shard_transition_root: Root = Root()
  ): AttestationData

  fun PendingAttestation(
    aggregation_bits: SSZBitList = sszFactory().SSZBitList(MAX_VALIDATORS_PER_COMMITTEE),
    data: AttestationData = AttestationData(),
    inclusion_delay: Slot = Slot(),
    proposer_index: ValidatorIndex = ValidatorIndex(),
    crosslink_success: boolean = false
  ): PendingAttestation

  fun Eth1Data(
    deposit_root: Root = Root(),
    deposit_count: uint64 = 0uL,
    block_hash: Bytes32 = Bytes32()
  ): Eth1Data

  fun HistoricalBatch(
    block_roots: SSZMutableVector<Root> = sszFactory().SSZVector(
      Root::class,
      MutableList(SLOTS_PER_HISTORICAL_ROOT.toInt()) { Root() }),
    state_roots: SSZMutableVector<Root> = sszFactory().SSZVector(
      Root::class,
      MutableList(SLOTS_PER_HISTORICAL_ROOT.toInt()) { Root() })
  ): HistoricalBatch

  fun DepositMessage(
    pubkey: BLSPubkey = BLSPubkey(),
    withdrawal_credentials: Bytes32 = Bytes32(),
    amount: Gwei = Gwei()
  ): DepositMessage

  fun DepositData(
    pubkey: BLSPubkey = BLSPubkey(),
    withdrawal_credentials: Bytes32 = Bytes32(),
    amount: Gwei = Gwei(),
    signature: BLSSignature = BLSSignature()
  ): DepositData

  fun BeaconBlockHeader(
    slot: Slot = Slot(),
    proposer_index: ValidatorIndex = ValidatorIndex(),
    parent_root: Root = Root(),
    state_root: Root = Root(),
    body_root: Root = Root()
  ): BeaconBlockHeader

  fun SigningRoot(
    object_root: Root = Root(),
    domain: Domain = Domain()
  ): SigningRoot

  fun Attestation(
    aggregation_bits: SSZBitList = sszFactory().SSZBitList(MAX_VALIDATORS_PER_COMMITTEE),
    data: AttestationData = AttestationData(),
    custody_bits_blocks: SSZMutableList<SSZBitList> = sszFactory().SSZList(
      SSZBitList::class,
      MAX_SHARD_BLOCKS_PER_ATTESTATION,
      mutableListOf()
    ),
    signature: BLSSignature = BLSSignature()
  ): Attestation

  fun IndexedAttestation(
    committee: SSZMutableList<ValidatorIndex> = sszFactory().SSZList(
      ValidatorIndex::class,
      MAX_VALIDATORS_PER_COMMITTEE
    ),
    attestation: Attestation = Attestation()
  ): IndexedAttestation

  fun AttesterSlashing(
    attestation_1: IndexedAttestation = IndexedAttestation(),
    attestation_2: IndexedAttestation = IndexedAttestation()
  ): AttesterSlashing

  fun Deposit(
    proof: SSZMutableVector<Bytes32> = sszFactory().SSZVector(
      Bytes32::class,
      MutableList((DEPOSIT_CONTRACT_TREE_DEPTH + 1uL).toInt()) { Bytes32() }),
    data: DepositData = DepositData()
  ): Deposit

  fun VoluntaryExit(
    epoch: Epoch = Epoch(),
    validator_index: ValidatorIndex = ValidatorIndex()
  ): VoluntaryExit

  fun SignedVoluntaryExit(
    message: VoluntaryExit = VoluntaryExit(),
    signature: BLSSignature = BLSSignature()
  ): SignedVoluntaryExit

  fun SignedBeaconBlockHeader(
    message: BeaconBlockHeader = BeaconBlockHeader(),
    signature: BLSSignature = BLSSignature()
  ): SignedBeaconBlockHeader

  fun ProposerSlashing(
    signed_header_1: SignedBeaconBlockHeader = SignedBeaconBlockHeader(),
    signed_header_2: SignedBeaconBlockHeader = SignedBeaconBlockHeader()
  ): ProposerSlashing

  fun CustodyKeyReveal(
    revealer_index: ValidatorIndex = ValidatorIndex(),
    reveal: BLSSignature = BLSSignature()
  ): CustodyKeyReveal

  fun EarlyDerivedSecretReveal(
    revealed_index: ValidatorIndex = ValidatorIndex(),
    epoch: Epoch = Epoch(),
    reveal: BLSSignature = BLSSignature(),
    masker_index: ValidatorIndex = ValidatorIndex(),
    mask: Bytes32 = Bytes32()
  ): EarlyDerivedSecretReveal

  fun ShardBlock(
    shard_parent_root: Root = Root(),
    beacon_parent_root: Root = Root(),
    slot: Slot = Slot(),
    proposer_index: ValidatorIndex = ValidatorIndex(),
    body: SSZByteList = sszFactory().SSZByteList(MAX_SHARD_BLOCK_SIZE)
  ): ShardBlock

  fun SignedShardBlock(
    message: ShardBlock = ShardBlock(),
    signature: BLSSignature = BLSSignature()
  ): SignedShardBlock

  fun ShardBlockHeader(
    shard_parent_root: Root = Root(),
    beacon_parent_root: Root = Root(),
    slot: Slot = Slot(),
    proposer_index: ValidatorIndex = ValidatorIndex(),
    body_root: Root = Root()
  ): ShardBlockHeader

  fun ShardState(
    slot: Slot = Slot(),
    gasprice: Gwei = Gwei(),
    transition_digest: Bytes32 = Bytes32(),
    latest_block_root: Root = Root()
  ): ShardState

  fun ShardTransition(
    start_slot: Slot = Slot(),
    shard_block_lengths: SSZMutableList<uint64> = sszFactory().SSZList(
      uint64::class,
      MAX_SHARD_BLOCKS_PER_ATTESTATION
    ),
    shard_data_roots: SSZMutableList<Bytes32> = sszFactory().SSZList(
      Bytes32::class,
      MAX_SHARD_BLOCKS_PER_ATTESTATION
    ),
    shard_states: SSZMutableList<ShardState> = sszFactory().SSZList(
      ShardState::class,
      MAX_SHARD_BLOCKS_PER_ATTESTATION
    ),
    proposer_signature_aggregate: BLSSignature = BLSSignature()
  ): ShardTransition

  fun CustodySlashing(
    data_index: uint64 = 0uL,
    malefactor_index: ValidatorIndex = ValidatorIndex(),
    malefactor_secret: BLSSignature = BLSSignature(),
    whistleblower_index: ValidatorIndex = ValidatorIndex(),
    shard_transition: ShardTransition = ShardTransition(),
    attestation: Attestation = Attestation(),
    data: SSZByteList = sszFactory().SSZByteList(MAX_SHARD_BLOCK_SIZE)
  ): CustodySlashing

  fun SignedCustodySlashing(
    message: CustodySlashing = CustodySlashing(),
    signature: BLSSignature = BLSSignature()
  ): SignedCustodySlashing

  fun BeaconBlockBody(
    randao_reveal: BLSSignature = BLSSignature(),
    eth1_data: Eth1Data = Eth1Data(),
    graffiti: Bytes32 = Bytes32(),
    proposer_slashings: SSZMutableList<ProposerSlashing> = sszFactory().SSZList(
      ProposerSlashing::class,
      MAX_PROPOSER_SLASHINGS
    ),
    attester_slashings: SSZMutableList<AttesterSlashing> = sszFactory().SSZList(
      AttesterSlashing::class,
      MAX_ATTESTER_SLASHINGS
    ),
    attestations: SSZMutableList<Attestation> = sszFactory().SSZList(
      Attestation::class,
      MAX_ATTESTATIONS
    ),
    deposits: SSZMutableList<Deposit> = sszFactory().SSZList(
      Deposit::class,
      MAX_DEPOSITS
    ),
    voluntary_exits: SSZMutableList<SignedVoluntaryExit> = sszFactory().SSZList(
      SignedVoluntaryExit::class,
      MAX_VOLUNTARY_EXITS
    ),
    custody_slashings: SSZMutableList<SignedCustodySlashing> = sszFactory().SSZList(
      SignedCustodySlashing::class,
      MAX_CUSTODY_SLASHINGS
    ),
    custody_key_reveals: SSZMutableList<CustodyKeyReveal> = sszFactory().SSZList(
      CustodyKeyReveal::class,
      MAX_CUSTODY_KEY_REVEALS
    ),
    early_derived_secret_reveals: SSZMutableList<EarlyDerivedSecretReveal> = sszFactory().SSZList(
      EarlyDerivedSecretReveal::class,
      MAX_EARLY_DERIVED_SECRET_REVEALS
    ),
    shard_transitions: SSZMutableVector<ShardTransition> = sszFactory().SSZVector(
      ShardTransition::class,
      MutableList(MAX_SHARDS.toInt()) { ShardTransition() }),
    light_client_signature_bitfield: SSZBitVector = sszFactory().SSZBitVector(
      MutableList(LIGHT_CLIENT_COMMITTEE_SIZE.toInt()) { false }),
    light_client_signature: BLSSignature = BLSSignature()
  ): BeaconBlockBody

  fun BeaconBlock(
    slot: Slot = Slot(),
    proposer_index: ValidatorIndex = ValidatorIndex(),
    parent_root: Root = Root(),
    state_root: Root = Root(),
    body: BeaconBlockBody = BeaconBlockBody()
  ): BeaconBlock

  fun SignedBeaconBlock(
    message: BeaconBlock = BeaconBlock(),
    signature: BLSSignature = BLSSignature()
  ): SignedBeaconBlock

  fun CompactCommittee(
    pubkeys: SSZMutableList<BLSPubkey> = sszFactory().SSZList(
      BLSPubkey::class,
      MAX_VALIDATORS_PER_COMMITTEE
    ),
    compact_validators: SSZMutableList<uint64> = sszFactory().SSZList(
      uint64::class,
      MAX_VALIDATORS_PER_COMMITTEE
    )
  ): CompactCommittee

  fun AttestationCustodyBitWrapper(
    attestation_data_root: Root = Root(),
    block_index: uint64 = 0uL,
    bit: boolean = false
  ): AttestationCustodyBitWrapper

  fun LatestMessage(
    epoch: Epoch = Epoch(),
    root: Root = Root()
  ): LatestMessage

  fun BeaconState(
    genesis_time: uint64 = 0uL,
    genesis_validators_root: Root = Root(),
    slot: Slot = Slot(),
    fork: Fork = Fork(),
    latest_block_header: BeaconBlockHeader = BeaconBlockHeader(),
    block_roots: SSZMutableVector<Root> = sszFactory().SSZVector(
      Root::class,
      MutableList(SLOTS_PER_HISTORICAL_ROOT.toInt()) { Root() }),
    state_roots: SSZMutableVector<Root> = sszFactory().SSZVector(
      Root::class,
      MutableList(SLOTS_PER_HISTORICAL_ROOT.toInt()) { Root() }),
    historical_roots: SSZMutableList<Root> = sszFactory().SSZList(
      Root::class,
      HISTORICAL_ROOTS_LIMIT
    ),
    eth1_data: Eth1Data = Eth1Data(),
    eth1_data_votes: SSZMutableList<Eth1Data> = sszFactory().SSZList(
      Eth1Data::class,
      EPOCHS_PER_ETH1_VOTING_PERIOD * SLOTS_PER_EPOCH
    ),
    eth1_deposit_index: uint64 = 0uL,
    validators: SSZMutableList<Validator> = sszFactory().SSZList(
      Validator::class,
      VALIDATOR_REGISTRY_LIMIT
    ),
    balances: SSZMutableList<Gwei> = sszFactory().SSZList(
      Gwei::class,
      VALIDATOR_REGISTRY_LIMIT
    ),
    randao_mixes: SSZMutableVector<Root> = sszFactory().SSZVector(
      Root::class,
      MutableList(EPOCHS_PER_HISTORICAL_VECTOR.toInt()) { Root() }),
    slashings: SSZMutableVector<Gwei> = sszFactory().SSZVector(
      Gwei::class,
      MutableList(EPOCHS_PER_SLASHINGS_VECTOR.toInt()) { Gwei() }),
    previous_epoch_attestations: SSZMutableList<PendingAttestation> = sszFactory().SSZList(
      PendingAttestation::class,
      MAX_ATTESTATIONS * SLOTS_PER_EPOCH
    ),
    current_epoch_attestations: SSZMutableList<PendingAttestation> = sszFactory().SSZList(
      PendingAttestation::class,
      MAX_ATTESTATIONS * SLOTS_PER_EPOCH
    ),
    justification_bits: SSZBitVector = sszFactory().SSZBitVector(
      MutableList(
        JUSTIFICATION_BITS_LENGTH.toInt()
      ) { false }),
    previous_justified_checkpoint: Checkpoint = Checkpoint(),
    current_justified_checkpoint: Checkpoint = Checkpoint(),
    finalized_checkpoint: Checkpoint = Checkpoint(),
    shard_states: SSZMutableList<ShardState> = sszFactory().SSZList(
      ShardState::class,
      MAX_SHARDS
    ),
    online_countdown: SSZMutableList<OnlineEpochs> = sszFactory().SSZList(
      OnlineEpochs::class,
      VALIDATOR_REGISTRY_LIMIT
    ),
    current_light_committee: CompactCommittee = CompactCommittee(),
    next_light_committee: CompactCommittee = CompactCommittee(),
    exposed_derived_secrets: SSZMutableVector<ExposedValidatorIndices> = sszFactory().SSZVector(
      ExposedValidatorIndices::class,
      MutableList(EARLY_DERIVED_SECRET_PENALTY_MAX_FUTURE_EPOCHS.toInt()) {
        this.ExposedValidatorIndices(max_size = MAX_EARLY_DERIVED_SECRET_REVEALS * SLOTS_PER_EPOCH)
      })
  ): BeaconState

  fun Store(
    time: uint64 = 0uL,
    genesis_time: uint64 = 0uL,
    justified_checkpoint: Checkpoint = Checkpoint(),
    finalized_checkpoint: Checkpoint = Checkpoint(),
    best_justified_checkpoint: Checkpoint = Checkpoint(),
    blocks: CDict<Root, BeaconBlockHeader> = CDict(),
    block_states: CDict<Root, BeaconState> = CDict(),
    checkpoint_states: CDict<Checkpoint, BeaconState> = CDict(),
    latest_messages: CDict<ValidatorIndex, LatestMessage> = CDict()
  ): Store

  fun ExposedValidatorIndices(
    max_size: ULong,
    items: MutableList<ValidatorIndex> = mutableListOf()
  ): ExposedValidatorIndices
}