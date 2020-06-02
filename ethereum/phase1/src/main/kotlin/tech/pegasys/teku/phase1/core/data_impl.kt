package tech.pegasys.teku.phase1.core

import tech.pegasys.teku.datastructures.state.MutableBeaconState
import tech.pegasys.teku.phase1.ssz.Bytes32
import tech.pegasys.teku.phase1.ssz.CBitlist
import tech.pegasys.teku.phase1.ssz.CBitvector
import tech.pegasys.teku.phase1.ssz.CByteList
import tech.pegasys.teku.phase1.ssz.CDict
import tech.pegasys.teku.phase1.ssz.CList
import tech.pegasys.teku.phase1.ssz.CVector
import tech.pegasys.teku.phase1.ssz.boolean
import tech.pegasys.teku.phase1.ssz.uint64
import kotlin.properties.Delegates

fun Fork(
    previous_version: Version = Version(),
    current_version: Version = Version(),
    epoch: Epoch = Epoch()
): Fork = TODO()

fun ForkData(
    current_version: Version = Version(),
    genesis_validators_root: Root = Root()
): ForkData = TODO()

fun Checkpoint(
    epoch: Epoch = Epoch(),
    root: Root = Root()
): Checkpoint = TODO()

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
): Validator = TODO()

fun AttestationData(
    slot: Slot = Slot(),
    index: CommitteeIndex = CommitteeIndex(),
    beacon_block_root: Root = Root(),
    source: Checkpoint = Checkpoint(),
    target: Checkpoint = Checkpoint(),
    head_shard_root: Root = Root(),
    shard_transition_root: Root = Root()
): AttestationData = TODO()

fun PendingAttestation(
    aggregation_bits: CBitlist = CBitlist(MAX_VALIDATORS_PER_COMMITTEE),
    data: AttestationData = AttestationData(),
    inclusion_delay: Slot = Slot(),
    proposer_index: ValidatorIndex = ValidatorIndex(),
    crosslink_success: boolean = false
): PendingAttestation = TODO()

fun Eth1Data(
    deposit_root: Root = Root(),
    deposit_count: uint64 = 0uL,
    block_hash: Bytes32 = Bytes32()
): Eth1Data = CEth1Data(deposit_root, deposit_count, block_hash)

class CEth1Data(
    override var deposit_root: Root,
    override val deposit_count: uint64,
    override val block_hash: Bytes32
) : Eth1Data

fun HistoricalBatch(
    block_roots: CVector<Root> = CVector(Root::class, MutableList(SLOTS_PER_HISTORICAL_ROOT.toInt()) { Root() }),
    state_roots: CVector<Root> = CVector(Root::class, MutableList(SLOTS_PER_HISTORICAL_ROOT.toInt()) { Root() })
): HistoricalBatch = TODO()

fun DepositMessage(
    pubkey: BLSPubkey = BLSPubkey(),
    withdrawal_credentials: Bytes32 = Bytes32(),
    amount: Gwei = Gwei()
): DepositMessage = TODO()

fun DepositData(
    pubkey: BLSPubkey = BLSPubkey(),
    withdrawal_credentials: Bytes32 = Bytes32(),
    amount: Gwei = Gwei(),
    signature: BLSSignature = BLSSignature()
): DepositData = TODO()

fun BeaconBlockHeader(
    slot: Slot = Slot(),
    proposer_index: ValidatorIndex = ValidatorIndex(),
    parent_root: Root = Root(),
    state_root: Root = Root(),
    body_root: Root = Root()
): BeaconBlockHeader = TODO()

fun SigningRoot(
    object_root: Root = Root(),
    domain: Domain = Domain()
): SigningRoot = TODO()

fun Attestation(
    aggregation_bits: CBitlist = CBitlist(MAX_VALIDATORS_PER_COMMITTEE),
    data: AttestationData = AttestationData(),
    custody_bits_blocks: CList<CBitlist> = CList(CBitlist::class, MAX_SHARD_BLOCKS_PER_ATTESTATION, mutableListOf()),
    signature: BLSSignature = BLSSignature()
): Attestation = TODO()

fun IndexedAttestation(
    committee: CList<ValidatorIndex> = CList(ValidatorIndex::class, MAX_VALIDATORS_PER_COMMITTEE),
    attestation: Attestation = Attestation()
): IndexedAttestation = TODO()

fun AttesterSlashing(
    attestation_1: IndexedAttestation = IndexedAttestation(),
    attestation_2: IndexedAttestation = IndexedAttestation()
): AttesterSlashing = TODO()

fun Deposit(
    proof: CVector<Bytes32> = CVector(Bytes32::class, MutableList((DEPOSIT_CONTRACT_TREE_DEPTH + 1uL).toInt()) { Bytes32() }),
    data: DepositData = DepositData()
): Deposit = TODO()

fun VoluntaryExit(
    epoch: Epoch = Epoch(),
    validator_index: ValidatorIndex = ValidatorIndex()
): VoluntaryExit = TODO()

fun SignedVoluntaryExit(
    message: VoluntaryExit = VoluntaryExit(),
    signature: BLSSignature = BLSSignature()
): SignedVoluntaryExit = TODO()

fun SignedBeaconBlockHeader(
    message: BeaconBlockHeader = BeaconBlockHeader(),
    signature: BLSSignature = BLSSignature()
): SignedBeaconBlockHeader = TODO()

fun ProposerSlashing(
    signed_header_1: SignedBeaconBlockHeader = SignedBeaconBlockHeader(),
    signed_header_2: SignedBeaconBlockHeader = SignedBeaconBlockHeader()
): ProposerSlashing = TODO()

fun CustodyKeyReveal(
    revealer_index: ValidatorIndex = ValidatorIndex(),
    reveal: BLSSignature = BLSSignature()
): CustodyKeyReveal = TODO()

fun EarlyDerivedSecretReveal(
    revealed_index: ValidatorIndex = ValidatorIndex(),
    epoch: Epoch = Epoch(),
    reveal: BLSSignature = BLSSignature(),
    masker_index: ValidatorIndex = ValidatorIndex(),
    mask: Bytes32 = Bytes32()
): EarlyDerivedSecretReveal = TODO()

fun ShardBlock(
    shard_parent_root: Root = Root(),
    beacon_parent_root: Root = Root(),
    slot: Slot = Slot(),
    proposer_index: ValidatorIndex = ValidatorIndex(),
    body: CByteList = CByteList(MAX_SHARD_BLOCK_SIZE)
): ShardBlock = TODO()

fun SignedShardBlock(
    message: ShardBlock = ShardBlock(),
    signature: BLSSignature = BLSSignature()
): SignedShardBlock = TODO()

fun ShardBlockHeader(
    shard_parent_root: Root = Root(),
    beacon_parent_root: Root = Root(),
    slot: Slot = Slot(),
    proposer_index: ValidatorIndex = ValidatorIndex(),
    body_root: Root = Root()
): ShardBlockHeader = TODO()

fun ShardState(
    slot: Slot = Slot(),
    gasprice: Gwei = Gwei(),
    transition_digest: Bytes32 = Bytes32(),
    latest_block_root: Root = Root()
): ShardState = TODO()

fun ShardTransition(
    start_slot: Slot = Slot(),
    shard_block_lengths: CList<uint64> = CList(uint64::class, MAX_SHARD_BLOCKS_PER_ATTESTATION),
    shard_data_roots: CList<Bytes32> = CList(Bytes32::class, MAX_SHARD_BLOCKS_PER_ATTESTATION),
    shard_states: CList<ShardState> = CList(ShardState::class, MAX_SHARD_BLOCKS_PER_ATTESTATION),
    proposer_signature_aggregate: BLSSignature = BLSSignature()
): ShardTransition = TODO()

fun CustodySlashing(
    data_index: uint64 = 0uL,
    malefactor_index: ValidatorIndex = ValidatorIndex(),
    malefactor_secret: BLSSignature = BLSSignature(),
    whistleblower_index: ValidatorIndex = ValidatorIndex(),
    shard_transition: ShardTransition = ShardTransition(),
    attestation: Attestation = Attestation(),
    data: CByteList = CByteList(MAX_SHARD_BLOCK_SIZE)
): CustodySlashing = TODO()

fun SignedCustodySlashing(
    message: CustodySlashing = CustodySlashing(),
    signature: BLSSignature = BLSSignature()
): SignedCustodySlashing = TODO()

fun BeaconBlockBody(
    randao_reveal: BLSSignature = BLSSignature(),
    eth1_data: Eth1Data = Eth1Data(),
    graffiti: Bytes32 = Bytes32(),
    proposer_slashings: CList<ProposerSlashing> = CList(ProposerSlashing::class, MAX_PROPOSER_SLASHINGS),
    attester_slashings: CList<AttesterSlashing> = CList(AttesterSlashing::class, MAX_ATTESTER_SLASHINGS),
    attestations: CList<Attestation> = CList(Attestation::class, MAX_ATTESTATIONS),
    deposits: CList<Deposit> = CList(Deposit::class, MAX_DEPOSITS),
    voluntary_exits: CList<SignedVoluntaryExit> = CList(SignedVoluntaryExit::class, MAX_VOLUNTARY_EXITS),
    custody_slashings: CList<SignedCustodySlashing> = CList(SignedCustodySlashing::class, MAX_CUSTODY_SLASHINGS),
    custody_key_reveals: CList<CustodyKeyReveal> = CList(CustodyKeyReveal::class, MAX_CUSTODY_KEY_REVEALS),
    early_derived_secret_reveals: CList<EarlyDerivedSecretReveal> = CList(EarlyDerivedSecretReveal::class, MAX_EARLY_DERIVED_SECRET_REVEALS),
    shard_transitions: CVector<ShardTransition> = CVector(ShardTransition::class, MutableList(MAX_SHARDS.toInt()) { ShardTransition() }),
    light_client_signature_bitfield: CBitvector = CBitvector(MutableList(LIGHT_CLIENT_COMMITTEE_SIZE.toInt()) { false }),
    light_client_signature: BLSSignature = BLSSignature()
): BeaconBlockBody = TODO()

fun BeaconBlock(
    slot: Slot = Slot(),
    proposer_index: ValidatorIndex = ValidatorIndex(),
    parent_root: Root = Root(),
    state_root: Root = Root(),
    body: BeaconBlockBody = BeaconBlockBody()
): BeaconBlock = TODO()

fun SignedBeaconBlock(
    message: BeaconBlock = BeaconBlock(),
    signature: BLSSignature = BLSSignature()
): SignedBeaconBlock = TODO()

fun CompactCommittee(
    pubkeys: CList<BLSPubkey> = CList(BLSPubkey::class, MAX_VALIDATORS_PER_COMMITTEE),
    compact_validators: CList<uint64> = CList(uint64::class, MAX_VALIDATORS_PER_COMMITTEE)
): CompactCommittee = TODO()

fun AttestationCustodyBitWrapper(
    attestation_data_root: Root = Root(),
    block_index: uint64 = 0uL,
    bit: boolean = false
): AttestationCustodyBitWrapper = TODO()

fun LatestMessage(
    epoch: Epoch = Epoch(),
    root: Root = Root()
): LatestMessage = TODO()

fun BeaconState(
    genesis_time: uint64 = 0uL,
    genesis_validators_root: Root = Root(),
    slot: Slot = Slot(),
    fork: Fork = Fork(),
    latest_block_header: BeaconBlockHeader = BeaconBlockHeader(),
    block_roots: CVector<Root> = CVector(Root::class, MutableList(SLOTS_PER_HISTORICAL_ROOT.toInt()) { Root() }),
    state_roots: CVector<Root> = CVector(Root::class, MutableList(SLOTS_PER_HISTORICAL_ROOT.toInt()) { Root() }),
    historical_roots: CList<Root> = CList(Root::class, HISTORICAL_ROOTS_LIMIT),
    eth1_data: Eth1Data = Eth1Data(),
    eth1_data_votes: CList<Eth1Data> = CList(Eth1Data::class, EPOCHS_PER_ETH1_VOTING_PERIOD * SLOTS_PER_EPOCH),
    eth1_deposit_index: uint64 = 0uL,
    validators: CList<Validator> = CList(Validator::class, VALIDATOR_REGISTRY_LIMIT),
    balances: CList<Gwei> = CList(Gwei::class, VALIDATOR_REGISTRY_LIMIT),
    randao_mixes: CVector<Root> = CVector(Root::class, MutableList(EPOCHS_PER_HISTORICAL_VECTOR.toInt()) { Root() }),
    slashings: CVector<Gwei> = CVector(Gwei::class, MutableList(EPOCHS_PER_SLASHINGS_VECTOR.toInt()) { Gwei() }),
    previous_epoch_attestations: CList<PendingAttestation> = CList(PendingAttestation::class, MAX_ATTESTATIONS * SLOTS_PER_EPOCH),
    current_epoch_attestations: CList<PendingAttestation> = CList(PendingAttestation::class, MAX_ATTESTATIONS * SLOTS_PER_EPOCH),
    justification_bits: CBitvector = CBitvector(MutableList(JUSTIFICATION_BITS_LENGTH.toInt()) { false } ),
    previous_justified_checkpoint: Checkpoint = Checkpoint(),
    current_justified_checkpoint: Checkpoint = Checkpoint(),
    finalized_checkpoint: Checkpoint = Checkpoint(),
    shard_states: CList<ShardState> = CList(ShardState::class, MAX_SHARDS),
    online_countdown: CList<OnlineEpochs> = CList(OnlineEpochs::class, VALIDATOR_REGISTRY_LIMIT),
    current_light_committee: CompactCommittee = CompactCommittee(),
    next_light_committee: CompactCommittee = CompactCommittee(),
    exposed_derived_secrets: CVector<CListOfValidatorIndices> = CVector(CListOfValidatorIndices::class, MutableList(EARLY_DERIVED_SECRET_PENALTY_MAX_FUTURE_EPOCHS.toInt()) { CListOfValidatorIndices(MAX_EARLY_DERIVED_SECRET_REVEALS * SLOTS_PER_EPOCH) })
): BeaconState = CBeaconState(
    genesis_time,
    genesis_validators_root,
    slot,
    fork,
    latest_block_header,
    block_roots,
    state_roots,
    historical_roots,
    eth1_data,
    eth1_data_votes,
    eth1_deposit_index,
    validators,
    balances,
    randao_mixes,
    slashings,
    previous_epoch_attestations,
    current_epoch_attestations,
    justification_bits,
    previous_justified_checkpoint,
    current_justified_checkpoint,
    finalized_checkpoint,
    shard_states,
    online_countdown,
    current_light_committee,
    next_light_committee,
    exposed_derived_secrets
)

class CBeaconState(private var data: MutableBeaconState): BeaconState {

  constructor(genesis_time: uint64,
              genesis_validators_root: Root,
              slot: Slot,
              fork: Fork,
              latest_block_header: BeaconBlockHeader,
              block_roots: CVector<Root>,
              state_roots: CVector<Root>,
              historical_roots: CList<Root>,
              eth1_data: Eth1Data,
              eth1_data_votes: CList<Eth1Data>,
              eth1_deposit_index: uint64,
              validators: CList<Validator>,
              balances: CList<Gwei>,
              randao_mixes: CVector<Root>,
              slashings: CVector<Gwei>,
              previous_epoch_attestations: CList<PendingAttestation>,
              current_epoch_attestations: CList<PendingAttestation>,
              justification_bits: CBitvector,
              previous_justified_checkpoint: Checkpoint,
              current_justified_checkpoint: Checkpoint,
              finalized_checkpoint: Checkpoint,
              shard_states: CList<ShardState>,
              online_countdown: CList<OnlineEpochs>,
              current_light_committee: CompactCommittee,
              next_light_committee: CompactCommittee,
              exposed_derived_secrets: CVector<CListOfValidatorIndices>) {
    data = MutableBeaconState.createBuilder()
    data.genesis_time = UnsignedLong(genesis_time)
    data.genesis_validators_root = genesis_validators_root
    data.slot = UnsignedLong(slot)
    data.fork = fork.asTeku()
    data.latest_block_header = latest_block_header.asTeku()
    block_roots.copyTo(data.block_roots)
    state_roots.copyTo(data.state_roots)
    historical_roots.copyTo(data.historical_roots)
    data.eth1_data = eth1_data.asTeku()
    eth1_data_votes.copyTo(data.eth1_data_votes, Eth1Data::asTeku)
    data.eth1_deposit_index = UnsignedLong(eth1_deposit_index)
    validators.copyTo(data.validators, Validator::asTeku)
    balances.copyTo(data.balances) { b -> UnsignedLong(b) }
    randao_mixes.copyTo(data.randao_mixes)
    slashings.copyTo(data.slashings) { b -> UnsignedLong(b) }
    previous_epoch_attestations.copyTo(data.previous_epoch_attestations, PendingAttestation::asTeku)
    current_epoch_attestations.copyTo(data.current_epoch_attestations, PendingAttestation::asTeku)
    data.justification_bits = justification_bits.asTeku()
    data.previous_justified_checkpoint = previous_justified_checkpoint.asTeku()
    data.current_justified_checkpoint = current_justified_checkpoint.asTeku()
    data.finalized_checkpoint = finalized_checkpoint.asTeku()
  }

  override val genesis_time: uint64
    get() = uint64(data.genesis_time)
  override var genesis_validators_root: Root
    get() = data.genesis_validators_root
    set(value) { data.genesis_validators_root = value }
  override var slot: Slot
    get() = uint64(data.slot)
    set(value) { data.slot = UnsignedLong(value) }
  override var fork: Fork
    get() = data.fork.asOnotole()
    set(value) { this.data.fork = value.asTeku() }
  override var latest_block_header: BeaconBlockHeader
    get() = this.data.latest_block_header.asOnotole()
    set(value) { this.data.latest_block_header = value.asTeku() }
  override val block_roots: CVector<Root>
    get() = TODO("Not yet implemented")
  override val state_roots: CVector<Root>
    get() = TODO("Not yet implemented")
  override val historical_roots: CList<Root>
    get() = TODO("Not yet implemented")
  override var eth1_data: Eth1Data
    get() = data.eth1_data.asOnotole()
    set(value) { data.eth1_data = value.asTeku() }
  override var eth1_data_votes: CList<Eth1Data>
    get() = TODO("Not yet implemented")
    set(value) {}
  override var eth1_deposit_index: uint64
    get() = uint64(data.eth1_deposit_index)
    set(value) { data.eth1_deposit_index = UnsignedLong(value) }
  override val validators: CList<Validator>
    get() = TODO("Not yet implemented")
  override val balances: CList<Gwei>
    get() = TODO("Not yet implemented")
  override val randao_mixes: CVector<Root>
    get() = TODO("Not yet implemented")
  override val slashings: CVector<Gwei>
    get() = TODO("Not yet implemented")
  override var previous_epoch_attestations: CList<PendingAttestation>
    get() = TODO("Not yet implemented")
    set(value) {}
  override var current_epoch_attestations: CList<PendingAttestation>
    get() = TODO("Not yet implemented")
    set(value) {}
  override val justification_bits: CBitvector
    get() = TODO("Not yet implemented")
  override var previous_justified_checkpoint: Checkpoint
    get() = data.previous_justified_checkpoint.asOnotole()
    set(value) { data.previous_justified_checkpoint = value.asTeku() }
  override var current_justified_checkpoint: Checkpoint
    get() = data.current_justified_checkpoint.asOnotole()
    set(value) { data.current_justified_checkpoint = value.asTeku() }
  override var finalized_checkpoint: Checkpoint
    get() = data.finalized_checkpoint.asOnotole()
    set(value) { data.finalized_checkpoint = value.asTeku() }
  override val shard_states: CList<ShardState>
    get() = TODO("Not yet implemented")
  override val online_countdown: CList<OnlineEpochs>
    get() = TODO("Not yet implemented")
  override var current_light_committee: CompactCommittee
    get() = TODO("Not yet implemented")
    set(value) {}
  override var next_light_committee: CompactCommittee
    get() = TODO("Not yet implemented")
    set(value) {}
  override val exposed_derived_secrets: CVector<CListOfValidatorIndices>
    get() = TODO("Not yet implemented")

  override fun copy(): BeaconState {
    TODO("Not yet implemented")
  }
}

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
): Store = TODO()
