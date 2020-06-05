package tech.pegasys.teku.phase1.core

import com.google.common.primitives.UnsignedLong
import tech.pegasys.teku.bls.BLSPublicKey
import tech.pegasys.teku.phase1.core.TypeConverter.Companion.cast
import tech.pegasys.teku.phase1.ssz.Bytes32
import tech.pegasys.teku.phase1.ssz.CDict
import tech.pegasys.teku.phase1.ssz.SSZBitList
import tech.pegasys.teku.phase1.ssz.SSZBitVector
import tech.pegasys.teku.phase1.ssz.SSZByteList
import tech.pegasys.teku.phase1.ssz.SSZList
import tech.pegasys.teku.phase1.ssz.SSZVector
import tech.pegasys.teku.phase1.ssz.boolean
import tech.pegasys.teku.phase1.ssz.uint64
import tech.pegasys.teku.ssz.SSZTypes.Bitvector
import kotlin.reflect.KClass
import tech.pegasys.teku.datastructures.blocks.BeaconBlockHeader as TekuBeaconBlockHeader
import tech.pegasys.teku.datastructures.state.MutableBeaconState as TekuBeaconState
import tech.pegasys.teku.datastructures.state.Validator as TekuValidator
import tech.pegasys.teku.ssz.SSZTypes.SSZList as TekuSSZList
import tech.pegasys.teku.ssz.SSZTypes.SSZMutableList as TekuSSZMutableList

typealias Callback<T> = (T) -> Unit

internal class ValidatorIndicesSSZListDelegate(
    data: TekuSSZMutableList<UnsignedLong>,
    type: KClass<ValidatorIndex>
) : ValidatorIndicesSSZList, SSZListDelegate<ValidatorIndex, UnsignedLong>(data, type) {
  constructor(items: MutableList<ValidatorIndex>, maxSize: ULong)
      : this(
      TekuSSZList.createMutable(
          items.map { cast(it, TypeConverter.match<UnsignedLong>(ValidatorIndex::class)) }.toList(),
          maxSize.toLong(),
          TypeConverter.match<UnsignedLong>(ValidatorIndex::class).java),
      ValidatorIndex::class)
}

abstract internal class Mutable<T>(internal var callback: Callback<T>?) {
  protected inline fun onUpdate(value: T) = callback?.invoke(value)
}

internal class ValidatorDelegate(
    internal var v: TekuValidator,
    onUpdate: Callback<Validator>? = null
) : Validator, Mutable<Validator>(onUpdate) {
  constructor(pubkey: BLSPubkey,
              withdrawal_credentials: Bytes32,
              effective_balance: Gwei,
              slashed: boolean,
              activation_eligibility_epoch: Epoch,
              activation_epoch: Epoch,
              exit_epoch: Epoch,
              withdrawable_epoch: Epoch,
              next_custody_secret_to_reveal: uint64,
              max_reveal_lateness: Epoch) {
    v = TekuValidator(
        cast(pubkey, BLSPublicKey::class),
        withdrawal_credentials,
        cast(effective_balance, UnsignedLong::class),
        slashed,
        cast(activation_eligibility_epoch, UnsignedLong::class),
        cast(activation_epoch, UnsignedLong::class),
        cast(exit_epoch, UnsignedLong::class),
        cast(withdrawable_epoch, UnsignedLong::class)
    )
  }

  override val pubkey
    get() = cast(v.pubkey, BLSPubkey::class)
  override val withdrawal_credentials: Bytes32
    get() = cast(v.withdrawal_credentials, Bytes32::class)
  override var effective_balance: Gwei
    get() = cast(v.effective_balance, Gwei::class)
    set(value) {
      v = v.withEffective_balance(cast(value, UnsignedLong::class))
      onUpdate(this)
    }
  override var slashed: boolean
    get() = v.isSlashed
    set(value) {
      v = v.withSlashed(value)
      onUpdate(this)
    }
  override var activation_eligibility_epoch: Epoch
    get() = cast(v.activation_eligibility_epoch, Epoch::class)
    set(value) {
      v = v.withActivation_eligibility_epoch(cast(value, UnsignedLong::class))
      onUpdate(this)
    }
  override var activation_epoch: Epoch
    get() = cast(v.activation_epoch, Epoch::class)
    set(value) {
      v = v.withActivation_epoch(cast(value, UnsignedLong::class))
      onUpdate(this)
    }
  override var exit_epoch: Epoch
    get() = cast(v.exit_epoch, Epoch::class)
    set(value) {
      v = v.withExit_epoch(cast(value, UnsignedLong::class))
      onUpdate(this)
    }
  override var withdrawable_epoch: Epoch
    get() = cast(v.withdrawable_epoch, Epoch::class)
    set(value) {
      v = v.withWithdrawable_epoch(cast(value, UnsignedLong::class))
      onUpdate(this)
    }
  override var next_custody_secret_to_reveal: uint64
    get() = TODO("Not yet implemented")
    set(value) {}
  override var max_reveal_lateness: Epoch
    get() = TODO("Not yet implemented")
    set(value) {}

  override fun hash_tree_root() = v.hash_tree_root()
}

internal class BeaconBlockHeaderDelegate(
    private var h: TekuBeaconBlockHeader,
    onUpdate: Callback<BeaconBlockHeader>? = null
) : BeaconBlockHeader, Mutable<BeaconBlockHeader>(onUpdate) {

  constructor(slot: Slot, proposer_index: ValidatorIndex, parent_root: Root, state_root: Root, body_root: Root)
      : this(
      TekuBeaconBlockHeader(
          cast(slot, UnsignedLong::class),
          cast(proposer_index, UnsignedLong::class),
          parent_root,
          state_root,
          body_root
      )
  )

  override val slot: Slot
    get() = cast(h.slot, Slot::class)
  override val proposer_index: ValidatorIndex
    get() = cast(h.proposer_index, ValidatorIndex::class)
  override val parent_root: Root
    get() = h.parent_root
  override var state_root: Root
    get() = h.state_root
    set(value) {
      h = TekuBeaconBlockHeader(
          h.slot,
          h.proposer_index,
          h.parent_root,
          value,
          h.body_root
      )
    }
  override val body_root: Root
    get() = h.state_root

  override fun copy(
      slot: Slot,
      proposer_index: ValidatorIndex,
      parent_root: Root,
      state_root: Root,
      body_root: Root
  ) = BeaconBlockHeaderDelegate(slot, proposer_index, parent_root, state_root, body_root)

  override fun hash_tree_root() = h.hash_tree_root()
}

class CEth1Data(
    override var deposit_root: Root,
    override val deposit_count: uint64,
    override val block_hash: Bytes32
) : Eth1Data

class BeaconStateDelegate(private var state: TekuBeaconState) : BeaconState {

  constructor(genesis_time: uint64,
              genesis_validators_root: Root,
              slot: Slot,
              fork: Fork,
              latest_block_header: BeaconBlockHeader,
              block_roots: SSZVector<Root>,
              state_roots: SSZVector<Root>,
              historical_roots: SSZList<Root>,
              eth1_data: Eth1Data,
              eth1_data_votes: SSZList<Eth1Data>,
              eth1_deposit_index: uint64,
              validators: SSZList<Validator>,
              balances: SSZList<Gwei>,
              randao_mixes: SSZVector<Root>,
              slashings: SSZVector<Gwei>,
              previous_epoch_attestations: SSZList<PendingAttestation>,
              current_epoch_attestations: SSZList<PendingAttestation>,
              justification_bits: SSZBitVector,
              previous_justified_checkpoint: Checkpoint,
              current_justified_checkpoint: Checkpoint,
              finalized_checkpoint: Checkpoint,
              shard_states: SSZList<ShardState>,
              online_countdown: SSZList<OnlineEpochs>,
              current_light_committee: CompactCommittee,
              next_light_committee: CompactCommittee,
              exposed_derived_secrets: SSZVector<ValidatorIndicesSSZList>) {
    state = TekuBeaconState.createBuilder()
    state.genesis_time = cast(genesis_time, UnsignedLong::class)
    state.genesis_validators_root = genesis_validators_root
    state.slot = cast(slot, UnsignedLong::class)
    state.fork = cast(fork, tech.pegasys.teku.datastructures.state.Fork::class)
    state.latest_block_header = cast(latest_block_header, tech.pegasys.teku.datastructures.blocks.BeaconBlockHeader::class)
    block_roots.copyTo(state.block_roots)
    state_roots.copyTo(state.state_roots)
    historical_roots.copyTo(state.historical_roots)
    state.eth1_data = cast(eth1_data, tech.pegasys.teku.datastructures.blocks.Eth1Data::class)
    eth1_data_votes.copyTo(state.eth1_data_votes)
    state.eth1_deposit_index = cast(eth1_deposit_index, UnsignedLong::class)
    validators.copyTo(state.validators)
    balances.copyTo(state.balances)
    randao_mixes.copyTo(state.randao_mixes)
    slashings.copyTo(state.slashings)
    previous_epoch_attestations.copyTo(state.previous_epoch_attestations)
    current_epoch_attestations.copyTo(state.current_epoch_attestations)
    state.justification_bits = cast(justification_bits, Bitvector::class)
    state.previous_justified_checkpoint = cast(previous_justified_checkpoint, tech.pegasys.teku.datastructures.state.Checkpoint::class)
    state.current_justified_checkpoint = cast(current_justified_checkpoint, tech.pegasys.teku.datastructures.state.Checkpoint::class)
    state.finalized_checkpoint = cast(finalized_checkpoint, tech.pegasys.teku.datastructures.state.Checkpoint::class)
  }

  override val genesis_time: uint64
    get() = cast(state.genesis_time, uint64::class)
  override var genesis_validators_root: Root
    get() = state.genesis_validators_root
    set(value) {
      state.genesis_validators_root = value
    }
  override var slot: Slot
    get() = cast(state.slot, uint64::class)
    set(value) {
      state.slot = cast(value, UnsignedLong::class)
    }
  override var fork: Fork
    get() = TODO("Not yet implemented")
    set(value) {}
  override var latest_block_header: BeaconBlockHeader
    get() = BeaconBlockHeaderDelegate(state.latest_block_header) { value -> this.latest_block_header = value }
    set(value) {
      this.state.latest_block_header = cast(value, TekuBeaconBlockHeader::class)
    }
  override val block_roots: SSZVector<Root>
    get() = TODO("Not yet implemented")
  override val state_roots: SSZVector<Root>
    get() = TODO("Not yet implemented")
  override val historical_roots: SSZList<Root>
    get() = TODO("Not yet implemented")
  override var eth1_data: Eth1Data
    get() = TODO("Not yet implemented")
    set(value) {}
  override var eth1_data_votes: SSZList<Eth1Data>
    get() = TODO("Not yet implemented")
    set(value) {}
  override var eth1_deposit_index: uint64
    get() = cast(state.eth1_deposit_index, uint64::class)
    set(value) {
      state.eth1_deposit_index = cast(value, UnsignedLong::class)
    }
  override val validators: SSZList<Validator>
    get() = SSZListDelegate(state.validators, Validator::class)
  override val balances: SSZList<Gwei>
    get() = SSZListDelegate(state.balances, Gwei::class)
  override val randao_mixes: SSZVector<Root>
    get() = SSZVectorDelegate(state.randao_mixes, Root::class)
  override val slashings: SSZVector<Gwei>
    get() = TODO("Not yet implemented")
  override var previous_epoch_attestations: SSZList<PendingAttestation>
    get() = TODO("Not yet implemented")
    set(value) {}
  override var current_epoch_attestations: SSZList<PendingAttestation>
    get() = TODO("Not yet implemented")
    set(value) {}
  override val justification_bits: SSZBitVector
    get() = TODO("Not yet implemented")
  override var previous_justified_checkpoint: Checkpoint
    get() = TODO("Not yet implemented")
    set(value) {}
  override var current_justified_checkpoint: Checkpoint
    get() = TODO("Not yet implemented")
    set(value) {}
  override var finalized_checkpoint: Checkpoint
    get() = TODO("Not yet implemented")
    set(value) {}
  override val shard_states: SSZList<ShardState>
    get() = TODO("Not yet implemented")
  override val online_countdown: SSZList<OnlineEpochs>
    get() = TODO("Not yet implemented")
  override var current_light_committee: CompactCommittee
    get() = TODO("Not yet implemented")
    set(value) {}
  override var next_light_committee: CompactCommittee
    get() = TODO("Not yet implemented")
    set(value) {}
  override val exposed_derived_secrets: SSZVector<ValidatorIndicesSSZList>
    get() = TODO("Not yet implemented")

  override fun copy(): BeaconState {
    TODO("Not yet implemented")
  }
}

class TekuDataObjectFactory : DataObjectFactory {

  override fun ValidatorIndicesSSZList(maxSize: ULong, items: MutableList<ValidatorIndex>): ValidatorIndicesSSZList {
    return ValidatorIndicesSSZListDelegate(items, maxSize)
  }

  override fun Fork(previous_version: Version, current_version: Version, epoch: Epoch): Fork {
    TODO("Not yet implemented")
  }

  override fun ForkData(current_version: Version, genesis_validators_root: Root): ForkData {
    TODO("Not yet implemented")
  }

  override fun Checkpoint(epoch: Epoch, root: Root): Checkpoint {
    TODO("Not yet implemented")
  }

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
  ): Validator {
    return ValidatorDelegate(
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
  }

  override fun AttestationData(slot: Slot, index: CommitteeIndex, beacon_block_root: Root, source: Checkpoint, target: Checkpoint, head_shard_root: Root, shard_transition_root: Root): AttestationData {
    TODO("Not yet implemented")
  }

  override fun PendingAttestation(aggregation_bits: SSZBitList, data: AttestationData, inclusion_delay: Slot, proposer_index: ValidatorIndex, crosslink_success: boolean): PendingAttestation {
    TODO("Not yet implemented")
  }

  override fun Eth1Data(deposit_root: Root, deposit_count: uint64, block_hash: Bytes32) = CEth1Data(deposit_root, deposit_count, block_hash)

  override fun HistoricalBatch(block_roots: SSZVector<Root>, state_roots: SSZVector<Root>): HistoricalBatch {
    TODO("Not yet implemented")
  }

  override fun DepositMessage(pubkey: BLSPubkey, withdrawal_credentials: Bytes32, amount: Gwei): DepositMessage {
    TODO("Not yet implemented")
  }

  override fun DepositData(pubkey: BLSPubkey, withdrawal_credentials: Bytes32, amount: Gwei, signature: BLSSignature): DepositData {
    TODO("Not yet implemented")
  }

  override fun BeaconBlockHeader(slot: Slot, proposer_index: ValidatorIndex, parent_root: Root, state_root: Root, body_root: Root): BeaconBlockHeader {
    TODO("Not yet implemented")
  }

  override fun SigningRoot(object_root: Root, domain: Domain): SigningRoot {
    TODO("Not yet implemented")
  }

  override fun Attestation(aggregation_bits: SSZBitList, data: AttestationData, custody_bits_blocks: SSZList<SSZBitList>, signature: BLSSignature): Attestation {
    TODO("Not yet implemented")
  }

  override fun IndexedAttestation(committee: SSZList<ValidatorIndex>, attestation: Attestation): IndexedAttestation {
    TODO("Not yet implemented")
  }

  override fun AttesterSlashing(attestation_1: IndexedAttestation, attestation_2: IndexedAttestation): AttesterSlashing {
    TODO("Not yet implemented")
  }

  override fun Deposit(proof: SSZVector<Bytes32>, data: DepositData): Deposit {
    TODO("Not yet implemented")
  }

  override fun VoluntaryExit(epoch: Epoch, validator_index: ValidatorIndex): VoluntaryExit {
    TODO("Not yet implemented")
  }

  override fun SignedVoluntaryExit(message: VoluntaryExit, signature: BLSSignature): SignedVoluntaryExit {
    TODO("Not yet implemented")
  }

  override fun SignedBeaconBlockHeader(message: BeaconBlockHeader, signature: BLSSignature): SignedBeaconBlockHeader {
    TODO("Not yet implemented")
  }

  override fun ProposerSlashing(signed_header_1: SignedBeaconBlockHeader, signed_header_2: SignedBeaconBlockHeader): ProposerSlashing {
    TODO("Not yet implemented")
  }

  override fun CustodyKeyReveal(revealer_index: ValidatorIndex, reveal: BLSSignature): CustodyKeyReveal {
    TODO("Not yet implemented")
  }

  override fun EarlyDerivedSecretReveal(revealed_index: ValidatorIndex, epoch: Epoch, reveal: BLSSignature, masker_index: ValidatorIndex, mask: Bytes32): EarlyDerivedSecretReveal {
    TODO("Not yet implemented")
  }

  override fun ShardBlock(shard_parent_root: Root, beacon_parent_root: Root, slot: Slot, proposer_index: ValidatorIndex, body: SSZByteList): ShardBlock {
    TODO("Not yet implemented")
  }

  override fun SignedShardBlock(message: ShardBlock, signature: BLSSignature): SignedShardBlock {
    TODO("Not yet implemented")
  }

  override fun ShardBlockHeader(shard_parent_root: Root, beacon_parent_root: Root, slot: Slot, proposer_index: ValidatorIndex, body_root: Root): ShardBlockHeader {
    TODO("Not yet implemented")
  }

  override fun ShardState(slot: Slot, gasprice: Gwei, transition_digest: Bytes32, latest_block_root: Root): ShardState {
    TODO("Not yet implemented")
  }

  override fun ShardTransition(start_slot: Slot, shard_block_lengths: SSZList<uint64>, shard_data_roots: SSZList<Bytes32>, shard_states: SSZList<ShardState>, proposer_signature_aggregate: BLSSignature): ShardTransition {
    TODO("Not yet implemented")
  }

  override fun CustodySlashing(data_index: uint64, malefactor_index: ValidatorIndex, malefactor_secret: BLSSignature, whistleblower_index: ValidatorIndex, shard_transition: ShardTransition, attestation: Attestation, data: SSZByteList): CustodySlashing {
    TODO("Not yet implemented")
  }

  override fun SignedCustodySlashing(message: CustodySlashing, signature: BLSSignature): SignedCustodySlashing {
    TODO("Not yet implemented")
  }

  override fun BeaconBlockBody(randao_reveal: BLSSignature, eth1_data: Eth1Data, graffiti: Bytes32, proposer_slashings: SSZList<ProposerSlashing>, attester_slashings: SSZList<AttesterSlashing>, attestations: SSZList<Attestation>, deposits: SSZList<Deposit>, voluntary_exits: SSZList<SignedVoluntaryExit>, custody_slashings: SSZList<SignedCustodySlashing>, custody_key_reveals: SSZList<CustodyKeyReveal>, early_derived_secret_reveals: SSZList<EarlyDerivedSecretReveal>, shard_transitions: SSZVector<ShardTransition>, light_client_signature_bitfield: SSZBitVector, light_client_signature: BLSSignature): BeaconBlockBody {
    TODO("Not yet implemented")
  }

  override fun BeaconBlock(slot: Slot, proposer_index: ValidatorIndex, parent_root: Root, state_root: Root, body: BeaconBlockBody): BeaconBlock {
    TODO("Not yet implemented")
  }

  override fun SignedBeaconBlock(message: BeaconBlock, signature: BLSSignature): SignedBeaconBlock {
    TODO("Not yet implemented")
  }

  override fun CompactCommittee(pubkeys: SSZList<BLSPubkey>, compact_validators: SSZList<uint64>): CompactCommittee {
    TODO("Not yet implemented")
  }

  override fun AttestationCustodyBitWrapper(attestation_data_root: Root, block_index: uint64, bit: boolean): AttestationCustodyBitWrapper {
    TODO("Not yet implemented")
  }

  override fun LatestMessage(epoch: Epoch, root: Root): LatestMessage {
    TODO("Not yet implemented")
  }

  override fun ValidatorIndicesSSZList(items: MutableList<ValidatorIndex>, maxSize: ULong): ValidatorIndicesSSZList {
    return ValidatorIndicesSSZListDelegate(items, maxSize)
  }

  override fun BeaconState(genesis_time: uint64, genesis_validators_root: Root, slot: Slot, fork: Fork, latest_block_header: BeaconBlockHeader, block_roots: SSZVector<Root>, state_roots: SSZVector<Root>, historical_roots: SSZList<Root>, eth1_data: Eth1Data, eth1_data_votes: SSZList<Eth1Data>, eth1_deposit_index: uint64, validators: SSZList<Validator>, balances: SSZList<Gwei>, randao_mixes: SSZVector<Root>, slashings: SSZVector<Gwei>, previous_epoch_attestations: SSZList<PendingAttestation>, current_epoch_attestations: SSZList<PendingAttestation>, justification_bits: SSZBitVector, previous_justified_checkpoint: Checkpoint, current_justified_checkpoint: Checkpoint, finalized_checkpoint: Checkpoint, shard_states: SSZList<ShardState>, online_countdown: SSZList<OnlineEpochs>, current_light_committee: CompactCommittee, next_light_committee: CompactCommittee, exposed_derived_secrets: SSZVector<ValidatorIndicesSSZList>) =
      BeaconStateDelegate(
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

  override fun Store(time: uint64, genesis_time: uint64, justified_checkpoint: Checkpoint, finalized_checkpoint: Checkpoint, best_justified_checkpoint: Checkpoint, blocks: CDict<Root, BeaconBlockHeader>, block_states: CDict<Root, BeaconState>, checkpoint_states: CDict<Checkpoint, BeaconState>, latest_messages: CDict<ValidatorIndex, LatestMessage>): Store {
    TODO("Not yet implemented")
  }
}