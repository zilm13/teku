package tech.pegasys.teku.phase1.integration.datastructures

import tech.pegasys.teku.phase1.onotole.phase1.Attestation
import tech.pegasys.teku.phase1.onotole.phase1.AttestationCustodyBitWrapper
import tech.pegasys.teku.phase1.onotole.phase1.AttestationData
import tech.pegasys.teku.phase1.onotole.phase1.AttesterSlashing
import tech.pegasys.teku.phase1.onotole.phase1.BLSPubkey
import tech.pegasys.teku.phase1.onotole.phase1.BLSSignature
import tech.pegasys.teku.phase1.onotole.phase1.BeaconBlock
import tech.pegasys.teku.phase1.onotole.phase1.BeaconBlockBody
import tech.pegasys.teku.phase1.onotole.phase1.BeaconBlockHeader
import tech.pegasys.teku.phase1.onotole.phase1.BeaconState
import tech.pegasys.teku.phase1.onotole.phase1.Checkpoint
import tech.pegasys.teku.phase1.onotole.phase1.CommitteeIndex
import tech.pegasys.teku.phase1.onotole.phase1.CompactCommittee
import tech.pegasys.teku.phase1.onotole.phase1.CustodyKeyReveal
import tech.pegasys.teku.phase1.onotole.phase1.CustodySlashing
import tech.pegasys.teku.phase1.onotole.phase1.DataObjectFactory
import tech.pegasys.teku.phase1.onotole.phase1.Deposit
import tech.pegasys.teku.phase1.onotole.phase1.DepositData
import tech.pegasys.teku.phase1.onotole.phase1.DepositMessage
import tech.pegasys.teku.phase1.onotole.phase1.Domain
import tech.pegasys.teku.phase1.onotole.phase1.EarlyDerivedSecretReveal
import tech.pegasys.teku.phase1.onotole.phase1.Epoch
import tech.pegasys.teku.phase1.onotole.phase1.Eth1Data
import tech.pegasys.teku.phase1.onotole.phase1.ExposedValidatorIndices
import tech.pegasys.teku.phase1.onotole.phase1.Fork
import tech.pegasys.teku.phase1.onotole.phase1.ForkData
import tech.pegasys.teku.phase1.onotole.phase1.Gwei
import tech.pegasys.teku.phase1.onotole.phase1.HistoricalBatch
import tech.pegasys.teku.phase1.onotole.phase1.IndexedAttestation
import tech.pegasys.teku.phase1.onotole.phase1.LatestMessage
import tech.pegasys.teku.phase1.onotole.phase1.OnlineEpochs
import tech.pegasys.teku.phase1.onotole.phase1.PendingAttestation
import tech.pegasys.teku.phase1.onotole.phase1.ProposerSlashing
import tech.pegasys.teku.phase1.onotole.phase1.Root
import tech.pegasys.teku.phase1.onotole.phase1.ShardBlock
import tech.pegasys.teku.phase1.onotole.phase1.ShardBlockHeader
import tech.pegasys.teku.phase1.onotole.phase1.ShardState
import tech.pegasys.teku.phase1.onotole.phase1.ShardTransition
import tech.pegasys.teku.phase1.onotole.phase1.SignedBeaconBlock
import tech.pegasys.teku.phase1.onotole.phase1.SignedBeaconBlockHeader
import tech.pegasys.teku.phase1.onotole.phase1.SignedCustodySlashing
import tech.pegasys.teku.phase1.onotole.phase1.SignedShardBlock
import tech.pegasys.teku.phase1.onotole.phase1.SignedVoluntaryExit
import tech.pegasys.teku.phase1.onotole.phase1.SigningRoot
import tech.pegasys.teku.phase1.onotole.phase1.Slot
import tech.pegasys.teku.phase1.onotole.phase1.Store
import tech.pegasys.teku.phase1.onotole.phase1.Validator
import tech.pegasys.teku.phase1.onotole.phase1.ValidatorIndex
import tech.pegasys.teku.phase1.onotole.phase1.Version
import tech.pegasys.teku.phase1.onotole.phase1.VoluntaryExit
import tech.pegasys.teku.phase1.onotole.ssz.Bytes32
import tech.pegasys.teku.phase1.onotole.ssz.CDict
import tech.pegasys.teku.phase1.onotole.ssz.SSZBitList
import tech.pegasys.teku.phase1.onotole.ssz.SSZBitVector
import tech.pegasys.teku.phase1.onotole.ssz.SSZByteList
import tech.pegasys.teku.phase1.onotole.ssz.SSZMutableList
import tech.pegasys.teku.phase1.onotole.ssz.SSZMutableVector
import tech.pegasys.teku.phase1.onotole.ssz.SSZObjectFactory
import tech.pegasys.teku.phase1.onotole.ssz.boolean
import tech.pegasys.teku.phase1.onotole.ssz.uint64

class DataObjectFactoryImpl(private val sszFactory: SSZObjectFactory) : DataObjectFactory {

  override fun sszFactory(): SSZObjectFactory {
    return sszFactory
  }

  override fun Fork(previous_version: Version, current_version: Version, epoch: Epoch): Fork =
    ForkWrapper(previous_version, current_version, epoch)

  override fun ForkData(current_version: Version, genesis_validators_root: Root): ForkData =
    ForkDataWrapper(current_version, genesis_validators_root)

  override fun Checkpoint(epoch: Epoch, root: Root): Checkpoint =
    CheckpointWrapper(epoch, root)

  override fun Validator(
    pubkey: BLSPubkey,
    withdrawal_credentials: Bytes32,
    effective_balance: Gwei,
    slashed: boolean,
    activation_eligibility_epoch: Epoch,
    activation_epoch: Epoch,
    exit_epoch: Epoch,
    withdrawable_epoch: Epoch,
    next_custody_secret_to_reveal: uint64,
    max_reveal_lateness: Epoch
  ): Validator = ValidatorWrapper(
    pubkey,
    withdrawal_credentials,
    effective_balance,
    slashed,
    activation_eligibility_epoch,
    activation_epoch,
    exit_epoch,
    withdrawable_epoch,
    next_custody_secret_to_reveal,
    max_reveal_lateness
  )

  override fun AttestationData(
    slot: Slot,
    index: CommitteeIndex,
    beacon_block_root: Root,
    source: Checkpoint,
    target: Checkpoint,
    head_shard_root: Root,
    shard_transition_root: Root
  ): AttestationData = AttestationDataWrapper(
    slot, index, beacon_block_root, source, target, head_shard_root, shard_transition_root
  )

  override fun PendingAttestation(
    aggregation_bits: SSZBitList,
    data: AttestationData,
    inclusion_delay: Slot,
    proposer_index: ValidatorIndex,
    crosslink_success: boolean
  ): PendingAttestation = PendingAttestationWrapper(
    aggregation_bits, data, inclusion_delay, proposer_index, crosslink_success
  )

  override fun Eth1Data(deposit_root: Root, deposit_count: uint64, block_hash: Bytes32): Eth1Data =
    Eth1DataWrapper(deposit_root, deposit_count, block_hash)

  override fun HistoricalBatch(
    block_roots: SSZMutableVector<Root>, state_roots: SSZMutableVector<Root>
  ): HistoricalBatch = HistoricalBatchWrapper(block_roots, state_roots)

  override fun DepositMessage(
    pubkey: BLSPubkey, withdrawal_credentials: Bytes32, amount: Gwei
  ): DepositMessage = DepositMessageWrapper(pubkey, withdrawal_credentials, amount)

  override fun DepositData(
    pubkey: BLSPubkey,
    withdrawal_credentials: Bytes32,
    amount: Gwei,
    signature: BLSSignature
  ): DepositData =
    DepositDataWrapper(pubkey, withdrawal_credentials, amount, signature)

  override fun BeaconBlockHeader(
    slot: Slot,
    proposer_index: ValidatorIndex,
    parent_root: Root,
    state_root: Root,
    body_root: Root
  ): BeaconBlockHeader = BeaconBlockHeaderWrapper(
    slot, proposer_index, parent_root, state_root, body_root
  )

  override fun SigningRoot(object_root: Root, domain: Domain): SigningRoot =
    SigningRootWrapper(object_root, domain)

  override fun Attestation(
    aggregation_bits: SSZBitList,
    data: AttestationData,
    custody_bits_blocks: SSZMutableList<SSZBitList>,
    signature: BLSSignature
  ): Attestation = AttestationWrapper(aggregation_bits, data, custody_bits_blocks, signature)

  override fun IndexedAttestation(
    committee: SSZMutableList<ValidatorIndex>,
    attestation: Attestation
  ): IndexedAttestation = IndexedAttestationWrapper(committee, attestation)

  override fun AttesterSlashing(
    attestation_1: IndexedAttestation,
    attestation_2: IndexedAttestation
  ): AttesterSlashing = AttesterSlashingWrapper(attestation_1, attestation_2)

  override fun Deposit(proof: SSZMutableVector<Bytes32>, data: DepositData): Deposit =
    DepositWrapper(proof, data)

  override fun VoluntaryExit(epoch: Epoch, validator_index: ValidatorIndex): VoluntaryExit =
    VoluntaryExitWrapper(epoch, validator_index)

  override fun SignedVoluntaryExit(
    message: VoluntaryExit, signature: BLSSignature
  ): SignedVoluntaryExit = SignedVoluntaryExitWrapper(message, signature)

  override fun SignedBeaconBlockHeader(
    message: BeaconBlockHeader, signature: BLSSignature
  ): SignedBeaconBlockHeader = SignedBeaconBlockHeaderWrapper(message, signature)

  override fun ProposerSlashing(
    signed_header_1: SignedBeaconBlockHeader,
    signed_header_2: SignedBeaconBlockHeader
  ): ProposerSlashing = ProposerSlashingWrapper(signed_header_1, signed_header_2)

  override fun CustodyKeyReveal(revealer_index: ValidatorIndex, reveal: BLSSignature)
      : CustodyKeyReveal = CustodyKeyRevealWrapper(revealer_index, reveal)

  override fun EarlyDerivedSecretReveal(
    revealed_index: ValidatorIndex,
    epoch: Epoch,
    reveal: BLSSignature,
    masker_index: ValidatorIndex,
    mask: Bytes32
  ): EarlyDerivedSecretReveal =
    EarlyDerivedSecretRevealWrapper(revealed_index, epoch, reveal, masker_index, mask)

  override fun ShardBlock(
    shard_parent_root: Root,
    beacon_parent_root: Root,
    slot: Slot,
    proposer_index: ValidatorIndex,
    body: SSZByteList
  ): ShardBlock =
    ShardBlockWrapper(shard_parent_root, beacon_parent_root, slot, proposer_index, body)

  override fun SignedShardBlock(message: ShardBlock, signature: BLSSignature): SignedShardBlock =
    SignedShardBlockWrapper(message, signature)

  override fun ShardBlockHeader(
    shard_parent_root: Root,
    beacon_parent_root: Root,
    slot: Slot,
    proposer_index: ValidatorIndex,
    body_root: Root
  ): ShardBlockHeader =
    ShardBlockHeaderWrapper(shard_parent_root, beacon_parent_root, slot, proposer_index, body_root)

  override fun ShardState(
    slot: Slot,
    gasprice: Gwei,
    transition_digest: Bytes32,
    latest_block_root: Root
  ): ShardState = ShardStateWrapper(slot, gasprice, transition_digest, latest_block_root)

  override fun ShardTransition(
    start_slot: Slot,
    shard_block_lengths: SSZMutableList<uint64>,
    shard_data_roots: SSZMutableList<Bytes32>,
    shard_states: SSZMutableList<ShardState>,
    proposer_signature_aggregate: BLSSignature
  ): ShardTransition = ShardTransitionWrapper(
    start_slot,
    shard_block_lengths,
    shard_data_roots,
    shard_states,
    proposer_signature_aggregate
  )

  override fun CustodySlashing(
    data_index: uint64,
    malefactor_index: ValidatorIndex,
    malefactor_secret: BLSSignature,
    whistleblower_index: ValidatorIndex,
    shard_transition: ShardTransition,
    attestation: Attestation,
    data: SSZByteList
  ): CustodySlashing = CustodySlashingWrapper(
    data_index,
    malefactor_index,
    malefactor_secret,
    whistleblower_index,
    shard_transition,
    attestation,
    data
  )

  override fun SignedCustodySlashing(
    message: CustodySlashing,
    signature: BLSSignature
  ): SignedCustodySlashing = SignedCustodySlashingWrapper(message, signature)

  override fun BeaconBlockBody(
    randao_reveal: BLSSignature,
    eth1_data: Eth1Data,
    graffiti: Bytes32,
    proposer_slashings: SSZMutableList<ProposerSlashing>,
    attester_slashings: SSZMutableList<AttesterSlashing>,
    attestations: SSZMutableList<Attestation>,
    deposits: SSZMutableList<Deposit>,
    voluntary_exits: SSZMutableList<SignedVoluntaryExit>,
    custody_slashings: SSZMutableList<SignedCustodySlashing>,
    custody_key_reveals: SSZMutableList<CustodyKeyReveal>,
    early_derived_secret_reveals: SSZMutableList<EarlyDerivedSecretReveal>,
    shard_transitions: SSZMutableVector<ShardTransition>,
    light_client_signature_bitfield: SSZBitVector, light_client_signature: BLSSignature
  ): BeaconBlockBody = BeaconBlockBodyWrapper(
    randao_reveal,
    eth1_data,
    graffiti,
    proposer_slashings,
    attester_slashings,
    attestations,
    deposits,
    voluntary_exits,
    custody_slashings,
    custody_key_reveals,
    early_derived_secret_reveals,
    shard_transitions,
    light_client_signature_bitfield,
    light_client_signature
  )

  override fun BeaconBlock(
    slot: Slot,
    proposer_index: ValidatorIndex,
    parent_root: Root,
    state_root: Root,
    body: BeaconBlockBody
  ): BeaconBlock = BeaconBlockWrapper(slot, proposer_index, parent_root, state_root, body)

  override fun SignedBeaconBlock(
    message: BeaconBlock, signature: BLSSignature
  ): SignedBeaconBlock = SignedBeaconBlockWrapper(message, signature)

  override fun CompactCommittee(
    pubkeys: SSZMutableList<BLSPubkey>,
    compact_validators: SSZMutableList<uint64>
  ): CompactCommittee = CompactCommitteeWrapper(pubkeys, compact_validators)

  override fun AttestationCustodyBitWrapper(
    attestation_data_root: Root,
    block_index: uint64,
    bit: boolean
  ): AttestationCustodyBitWrapper =
    AttestationCustodyBitWrapperImpl(attestation_data_root, block_index, bit)

  override fun LatestMessage(epoch: Epoch, root: Root): LatestMessage =
    LatestMessageImpl(epoch, root)

  override fun BeaconState(
    genesis_time: uint64,
    genesis_validators_root: Root,
    slot: Slot,
    fork: Fork,
    latest_block_header: BeaconBlockHeader,
    block_roots: SSZMutableVector<Root>,
    state_roots: SSZMutableVector<Root>,
    historical_roots: SSZMutableList<Root>,
    eth1_data: Eth1Data,
    eth1_data_votes: SSZMutableList<Eth1Data>,
    eth1_deposit_index: uint64,
    validators: SSZMutableList<Validator>,
    balances: SSZMutableList<Gwei>,
    randao_mixes: SSZMutableVector<Root>,
    slashings: SSZMutableVector<Gwei>,
    previous_epoch_attestations: SSZMutableList<PendingAttestation>,
    current_epoch_attestations: SSZMutableList<PendingAttestation>,
    justification_bits: SSZBitVector,
    previous_justified_checkpoint: Checkpoint,
    current_justified_checkpoint: Checkpoint,
    finalized_checkpoint: Checkpoint,
    shard_states: SSZMutableList<ShardState>,
    online_countdown: SSZMutableList<OnlineEpochs>,
    current_light_committee: CompactCommittee,
    next_light_committee: CompactCommittee,
    exposed_derived_secrets: SSZMutableVector<ExposedValidatorIndices>
  ): BeaconState = BeaconStateWrapper.create(
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

  override fun Store(
    time: uint64,
    genesis_time: uint64,
    justified_checkpoint: Checkpoint,
    finalized_checkpoint: Checkpoint,
    best_justified_checkpoint: Checkpoint,
    blocks: CDict<Root, BeaconBlockHeader>,
    block_states: CDict<Root, BeaconState>,
    checkpoint_states: CDict<Checkpoint, BeaconState>,
    latest_messages: CDict<ValidatorIndex, LatestMessage>
  ): Store = StoreImpl(
    time,
    genesis_time,
    justified_checkpoint,
    finalized_checkpoint,
    best_justified_checkpoint,
    blocks,
    block_states,
    checkpoint_states,
    latest_messages
  )


  override fun ExposedValidatorIndices(
    max_size: ULong,
    items: MutableList<ValidatorIndex>
  ): ExposedValidatorIndices {
    return ExposedValidatorIndicesWrapper(items, max_size)
  }
}
