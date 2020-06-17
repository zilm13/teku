package tech.pegasys.teku.phase1.integration.datastructures

import com.google.common.primitives.UnsignedLong
import tech.pegasys.teku.phase1.integration.AttestationDataType
import tech.pegasys.teku.phase1.integration.BLSPublicKeyType
import tech.pegasys.teku.phase1.integration.BeaconBlockHeaderType
import tech.pegasys.teku.phase1.integration.Bytes32Type
import tech.pegasys.teku.phase1.integration.Bytes4Type
import tech.pegasys.teku.phase1.integration.CheckpointType
import tech.pegasys.teku.phase1.integration.CompactCommitteeType
import tech.pegasys.teku.phase1.integration.DomainTypePair
import tech.pegasys.teku.phase1.integration.Eth1DataType
import tech.pegasys.teku.phase1.integration.ExposedValidatorIndicesType
import tech.pegasys.teku.phase1.integration.ForkType
import tech.pegasys.teku.phase1.integration.PendingAttestationType
import tech.pegasys.teku.phase1.integration.SSZBitListType
import tech.pegasys.teku.phase1.integration.SSZBitVectorType
import tech.pegasys.teku.phase1.integration.SSZListType
import tech.pegasys.teku.phase1.integration.SSZMutableListType
import tech.pegasys.teku.phase1.integration.SSZMutableVectorType
import tech.pegasys.teku.phase1.integration.SSZVectorType
import tech.pegasys.teku.phase1.integration.ShardStateType
import tech.pegasys.teku.phase1.integration.UInt64Type
import tech.pegasys.teku.phase1.integration.UInt8Type
import tech.pegasys.teku.phase1.integration.ValidatorType
import tech.pegasys.teku.phase1.integration.ssz.copyTo
import tech.pegasys.teku.phase1.onotole.phase1.AttestationData
import tech.pegasys.teku.phase1.onotole.phase1.BLSPubkey
import tech.pegasys.teku.phase1.onotole.phase1.BeaconBlockHeader
import tech.pegasys.teku.phase1.onotole.phase1.BeaconState
import tech.pegasys.teku.phase1.onotole.phase1.Checkpoint
import tech.pegasys.teku.phase1.onotole.phase1.CompactCommittee
import tech.pegasys.teku.phase1.onotole.phase1.Domain
import tech.pegasys.teku.phase1.onotole.phase1.Epoch
import tech.pegasys.teku.phase1.onotole.phase1.Eth1Data
import tech.pegasys.teku.phase1.onotole.phase1.ExposedValidatorIndices
import tech.pegasys.teku.phase1.onotole.phase1.Fork
import tech.pegasys.teku.phase1.onotole.phase1.ForkData
import tech.pegasys.teku.phase1.onotole.phase1.Gwei
import tech.pegasys.teku.phase1.onotole.phase1.HistoricalBatch
import tech.pegasys.teku.phase1.onotole.phase1.OnlineEpochs
import tech.pegasys.teku.phase1.onotole.phase1.PendingAttestation
import tech.pegasys.teku.phase1.onotole.phase1.Root
import tech.pegasys.teku.phase1.onotole.phase1.ShardState
import tech.pegasys.teku.phase1.onotole.phase1.SigningRoot
import tech.pegasys.teku.phase1.onotole.phase1.Slot
import tech.pegasys.teku.phase1.onotole.phase1.Validator
import tech.pegasys.teku.phase1.onotole.phase1.ValidatorIndex
import tech.pegasys.teku.phase1.onotole.phase1.Version
import tech.pegasys.teku.phase1.onotole.ssz.Bytes32
import tech.pegasys.teku.phase1.onotole.ssz.SSZBitList
import tech.pegasys.teku.phase1.onotole.ssz.SSZBitVector
import tech.pegasys.teku.phase1.onotole.ssz.SSZList
import tech.pegasys.teku.phase1.onotole.ssz.SSZMutableList
import tech.pegasys.teku.phase1.onotole.ssz.SSZMutableVector
import tech.pegasys.teku.phase1.onotole.ssz.SSZVector
import tech.pegasys.teku.phase1.onotole.ssz.boolean
import tech.pegasys.teku.phase1.onotole.ssz.uint64
import java.util.stream.Collectors
import tech.pegasys.teku.datastructures.phase1.state.CompactCommittee as TekuCompactCommittee
import tech.pegasys.teku.datastructures.phase1.state.ExposedValidatorIndices as TekuExposedValidatorIndices
import tech.pegasys.teku.datastructures.phase1.state.MutableBeaconStatePhase1 as TekuBeaconState
import tech.pegasys.teku.datastructures.phase1.state.PendingAttestationPhase1 as TekuPendingAttestation
import tech.pegasys.teku.datastructures.phase1.state.ValidatorPhase1 as TekuValidator
import tech.pegasys.teku.datastructures.state.Checkpoint as TekuCheckpoint
import tech.pegasys.teku.datastructures.state.Fork as TekuFork
import tech.pegasys.teku.datastructures.state.ForkData as TekuForkData
import tech.pegasys.teku.datastructures.state.HistoricalBatch as TekuHistoricalBatch
import tech.pegasys.teku.datastructures.state.SigningRoot as TekuSigningRoot
import tech.pegasys.teku.ssz.SSZTypes.SSZList as TekuSSZList

internal class ExposedValidatorIndicesWrapper(
  override var v: TekuExposedValidatorIndices,
  onUpdate: Callback<ExposedValidatorIndicesWrapper>? = null
) : Wrapper<TekuExposedValidatorIndices>, ExposedValidatorIndices,
  Mutable<ExposedValidatorIndicesWrapper>(onUpdate) {
  constructor(items: MutableList<ValidatorIndex>, maxSize: ULong)
      : this(
    TekuExposedValidatorIndices(
      TekuSSZList.createMutable(
        items.map { UInt64Type.unwrap(it) }.toList(),
        maxSize.toLong(),
        UnsignedLong::class.java
      )
    )
  )

  override val maxSize: ULong
    get() = v.indices.maxSize.toULong()

  override fun get(index: ULong): ValidatorIndex = UInt64Type.wrap(v.indices.get(index.toInt()))

  override val size: Int
    get() = v.size()

  override fun contains(element: ValidatorIndex): Boolean =
    v.indices.contains(UInt64Type.unwrap(element))

  override fun indexOf(element: ValidatorIndex): Int =
    v.indices.indexOf(UInt64Type.unwrap(element))

  override fun isEmpty(): Boolean = v.indices.isEmpty

  override fun iterator(): MutableIterator<ValidatorIndex> =
    object : MutableIterator<ValidatorIndex> {
      private val iterator = v.indices.iterator()
      override fun hasNext() = iterator.hasNext()
      override fun next() = UInt64Type.wrap(iterator.next())
      override fun remove() = iterator.remove()
    }

  override fun lastIndexOf(element: ValidatorIndex): Int =
    v.indices.lastIndexOf(UInt64Type.unwrap(element))

  override fun subList(fromIndex: Int, toIndex: Int): List<ValidatorIndex> {
    return v.indices.stream()
      .skip(fromIndex.toLong())
      .limit((toIndex - fromIndex + 1).toLong())
      .map { UInt64Type.wrap(it) }
      .collect(Collectors.toList())
  }

  override fun hash_tree_root(): Bytes32 = v.hash_tree_root()

  override fun set(index: ULong, newValue: ValidatorIndex): ValidatorIndex {
    val oldValue = UInt64Type.wrap(v.indices.get(index.toInt()))
    val writableCopy = TekuSSZList.createMutable(v.indices)
    writableCopy.set(index.toInt(), UInt64Type.unwrap(newValue))
    v = TekuExposedValidatorIndices(writableCopy)
    onUpdate(this)
    return oldValue
  }

  override fun append(item: ValidatorIndex) {
    val writableCopy = TekuSSZList.createMutable(v.indices)
    writableCopy.add(UInt64Type.unwrap(item))
    v = TekuExposedValidatorIndices(writableCopy)
    onUpdate(this)
  }

  override fun listIterator() = throw UnsupportedOperationException()
  override fun listIterator(index: Int) = throw UnsupportedOperationException()
  override fun containsAll(elements: Collection<ValidatorIndex>) =
    throw UnsupportedOperationException()
}

internal class ForkWrapper(
  override val v: TekuFork
) : Wrapper<TekuFork>, Fork {

  constructor(previous_version: Version, current_version: Version, epoch: Epoch)
      : this(
    TekuFork(
      Bytes4Type.unwrap(previous_version),
      Bytes4Type.unwrap(current_version),
      UInt64Type.unwrap(epoch)
    )
  )

  override val previous_version: Version
    get() = Bytes4Type.wrap(v.previous_version)
  override val current_version: Version
    get() = Bytes4Type.wrap(v.current_version)
  override val epoch: Epoch
    get() = UInt64Type.wrap(v.epoch)

  override fun hash_tree_root(): Bytes32 = v.hash_tree_root()

  override fun equals(other: Any?): Boolean {
    if (other is ForkWrapper) {
      return v == other.v
    }
    return false
  }

  override fun hashCode(): Int {
    return v.hashCode()
  }

  override fun toString(): String {
    return v.toString()
  }
}

internal class ForkDataWrapper(
  override val v: TekuForkData
) : Wrapper<TekuForkData>, ForkData {

  constructor(current_version: Version, genesis_validators_root: Root)
      : this(TekuForkData(Bytes4Type.unwrap(current_version), genesis_validators_root))

  override val current_version: Version
    get() = Bytes4Type.wrap(v.currentVersion)
  override val genesis_validators_root: Root
    get() = Bytes32Type.wrap(v.genesisValidatorsRoot)

  override fun hash_tree_root(): Bytes32 = v.hash_tree_root()

  override fun equals(other: Any?): Boolean {
    if (other is ForkDataWrapper) {
      return v == other.v
    }
    return false
  }

  override fun hashCode(): Int {
    return v.hashCode()
  }

  override fun toString(): String {
    return v.toString()
  }
}

internal class CheckpointWrapper(
  override val v: TekuCheckpoint
) : Wrapper<TekuCheckpoint>, Checkpoint {

  constructor(epoch: Epoch, root: Root)
      : this(TekuCheckpoint(UInt64Type.unwrap(epoch), root))

  override val epoch: Epoch
    get() = UInt64Type.wrap(v.epoch)
  override val root: Root
    get() = Bytes32Type.wrap(v.root)

  override fun hash_tree_root() = v.hash_tree_root()

  override fun equals(other: Any?): Boolean {
    if (other is CheckpointWrapper) {
      return v == other.v
    }
    return false
  }

  override fun hashCode(): Int {
    return v.hashCode()
  }

  override fun toString(): String {
    return v.toString()
  }
}

internal class ValidatorWrapper(
  override var v: TekuValidator,
  onUpdate: Callback<Validator>? = null
) : Wrapper<TekuValidator>, Validator, Mutable<Validator>(onUpdate) {
  constructor(
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
  ) : this(
    v = TekuValidator(
      BLSPublicKeyType.unwrap(pubkey),
      withdrawal_credentials,
      UInt64Type.unwrap(effective_balance),
      slashed,
      UInt64Type.unwrap(activation_eligibility_epoch),
      UInt64Type.unwrap(activation_epoch),
      UInt64Type.unwrap(exit_epoch),
      UInt64Type.unwrap(withdrawable_epoch),
      UInt64Type.unwrap(next_custody_secret_to_reveal),
      UInt64Type.unwrap(max_reveal_lateness)
    )
  )

  override val pubkey
    get() = BLSPublicKeyType.wrap(v.pubkey)
  override val withdrawal_credentials: Bytes32
    get() = v.withdrawal_credentials
  override var effective_balance: Gwei
    get() = UInt64Type.wrap(v.effective_balance)
    set(value) {
      v = v.withEffective_balance(UInt64Type.unwrap(value))
      onUpdate(this)
    }
  override var slashed: boolean
    get() = v.isSlashed
    set(value) {
      v = v.withSlashed(value)
      onUpdate(this)
    }
  override var activation_eligibility_epoch: Epoch
    get() = UInt64Type.wrap(v.activation_eligibility_epoch)
    set(value) {
      v = v.withActivation_eligibility_epoch(UInt64Type.unwrap(value))
      onUpdate(this)
    }
  override var activation_epoch: Epoch
    get() = UInt64Type.wrap(v.activation_epoch)
    set(value) {
      v = v.withActivation_epoch(UInt64Type.unwrap(value))
      onUpdate(this)
    }
  override var exit_epoch: Epoch
    get() = UInt64Type.wrap(v.exit_epoch)
    set(value) {
      v = v.withExit_epoch(UInt64Type.unwrap(value))
      onUpdate(this)
    }
  override var withdrawable_epoch: Epoch
    get() = UInt64Type.wrap(v.withdrawable_epoch)
    set(value) {
      v = v.withWithdrawable_epoch(UInt64Type.unwrap(value))
      onUpdate(this)
    }
  override var next_custody_secret_to_reveal: uint64
    get() = UInt64Type.wrap(v.next_custody_secret_to_reveal)
    set(value) {
      v = v.withNext_custody_secret_to_reveal(UInt64Type.unwrap(value))
      onUpdate(this)
    }
  override var max_reveal_lateness: Epoch
    get() = UInt64Type.wrap(v.max_reveal_lateness)
    set(value) {
      v = v.withMax_reveal_lateness(UInt64Type.unwrap(value))
      onUpdate(this)
    }

  override fun hash_tree_root() = v.hash_tree_root()

  override fun equals(other: Any?): Boolean {
    if (other is ValidatorWrapper) {
      return v == other.v
    }
    return false
  }

  override fun hashCode(): Int {
    return v.hashCode()
  }

  override fun toString(): String {
    return v.toString()
  }
}

internal class PendingAttestationWrapper(
  override var v: TekuPendingAttestation,
  onUpdate: Callback<PendingAttestation>? = null
) : Wrapper<TekuPendingAttestation>, PendingAttestation, Mutable<PendingAttestation>(onUpdate) {

  constructor(
    aggregation_bits: SSZBitList,
    data: AttestationData,
    inclusion_delay: Slot,
    proposer_index: ValidatorIndex,
    crosslink_success: boolean
  ) : this(
    TekuPendingAttestation(
      SSZBitListType.unwrap(aggregation_bits),
      AttestationDataType.unwrap(data),
      UInt64Type.unwrap(inclusion_delay),
      UInt64Type.unwrap(proposer_index),
      crosslink_success
    )
  )

  override val aggregation_bits: SSZBitList
    get() = SSZBitListType.wrap(v.aggregation_bits)
  override val data: AttestationData
    get() = AttestationDataType.wrap(v.data)
  override val inclusion_delay: Slot
    get() = UInt64Type.wrap(v.inclusion_delay)
  override val proposer_index: ValidatorIndex
    get() = UInt64Type.wrap(v.proposer_index)
  override var crosslink_success: boolean
    get() = v.crosslink_success
    set(value) {
      v = TekuPendingAttestation(
        v.aggregation_bits, v.data, v.inclusion_delay, v.proposer_index, value
      )
      onUpdate(this)
    }

  override fun hash_tree_root() = v.hash_tree_root()

  override fun equals(other: Any?): Boolean {
    if (other is PendingAttestationWrapper) {
      return v == other.v
    }
    return false
  }

  override fun hashCode(): Int {
    return v.hashCode()
  }

  override fun toString(): String {
    return v.toString()
  }
}

internal class HistoricalBatchWrapper(
  override val v: TekuHistoricalBatch
) : Wrapper<TekuHistoricalBatch>, HistoricalBatch {

  constructor(block_roots: SSZMutableVector<Root>, state_roots: SSZMutableVector<Root>)
      : this(
    TekuHistoricalBatch(
      SSZMutableVectorType(Bytes32Type).unwrap(block_roots),
      SSZMutableVectorType(Bytes32Type).unwrap(state_roots)
    )
  )

  override val block_roots: SSZVector<Root>
    get() = SSZVectorType(Bytes32Type).wrap(v.blockRoots)
  override val state_roots: SSZVector<Root>
    get() = SSZVectorType(Bytes32Type).wrap(v.stateRoots)

  override fun hash_tree_root() = v.hash_tree_root()

  override fun equals(other: Any?): Boolean {
    if (other is HistoricalBatchWrapper) {
      return v == other.v
    }
    return false
  }

  override fun hashCode(): Int {
    return v.hashCode()
  }

  override fun toString(): String {
    return v.toString()
  }
}

internal class SigningRootWrapper(
  override val v: TekuSigningRoot
) : Wrapper<TekuSigningRoot>, SigningRoot {

  constructor(object_root: Root, domain: Domain) : this(TekuSigningRoot(object_root, domain))

  override val object_root: Root
    get() = Bytes32Type.wrap(v.objectRoot)
  override val domain: Domain
    get() = DomainTypePair.wrap(v.domain)

  override fun hash_tree_root() = v.hash_tree_root()

  override fun equals(other: Any?): Boolean {
    if (other is SigningRootWrapper) {
      return v == other.v
    }
    return false
  }

  override fun hashCode(): Int {
    return v.hashCode()
  }

  override fun toString(): String {
    return v.toString()
  }
}

internal class CompactCommitteeWrapper(override val v: TekuCompactCommittee) :
  Wrapper<TekuCompactCommittee>, CompactCommittee {

  constructor(pubkeys: SSZMutableList<BLSPubkey>, compact_validators: SSZMutableList<uint64>)
      : this(
    tech.pegasys.teku.datastructures.phase1.state.CompactCommittee(
      SSZMutableListType(BLSPublicKeyType).unwrap(pubkeys),
      SSZMutableListType(UInt64Type).unwrap(compact_validators)
    )
  )

  override val pubkeys: SSZList<BLSPubkey>
    get() = SSZListType(BLSPublicKeyType).wrap(v.pubkeys)
  override val compact_validators: SSZList<uint64>
    get() = SSZListType(UInt64Type).wrap(v.compact_validators)

  override fun hash_tree_root() = v.hash_tree_root()

  override fun equals(other: Any?): Boolean {
    if (other is CompactCommitteeWrapper) {
      return other.v == v
    }
    return false
  }

  override fun hashCode(): Int {
    return v.hashCode()
  }

  override fun toString(): String {
    return v.toString()
  }
}


internal class BeaconStateWrapper(override var v: TekuBeaconState) : Wrapper<TekuBeaconState>,
  BeaconState {

  companion object {
    fun create(
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
    ): BeaconState {
      val v = TekuBeaconState.createBuilder()
      v.genesis_time = UInt64Type.unwrap(genesis_time)
      v.genesis_validators_root = genesis_validators_root
      v.slot = UInt64Type.unwrap(slot)
      v.fork = ForkType.unwrap(fork)
      v.latest_block_header = BeaconBlockHeaderType.unwrap(latest_block_header)
      block_roots.copyTo(v.block_roots)
      state_roots.copyTo(v.state_roots)
      historical_roots.copyTo(v.historical_roots)
      v.eth1_data = Eth1DataType.unwrap(eth1_data)
      eth1_data_votes.copyTo(v.eth1_data_votes)
      v.eth1_deposit_index = UInt64Type.unwrap(eth1_deposit_index)
      validators.copyTo(v.validators)
      balances.copyTo(v.balances)
      randao_mixes.copyTo(v.randao_mixes)
      slashings.copyTo(v.slashings)
      previous_epoch_attestations.copyTo(v.previous_epoch_attestations)
      current_epoch_attestations.copyTo(v.current_epoch_attestations)
      v.justification_bits = SSZBitVectorType.unwrap(justification_bits)
      v.previous_justified_checkpoint = CheckpointType.unwrap(previous_justified_checkpoint)
      v.current_justified_checkpoint = CheckpointType.unwrap(current_justified_checkpoint)
      v.finalized_checkpoint = CheckpointType.unwrap(finalized_checkpoint)
      shard_states.copyTo(v.shard_states)
      online_countdown.copyTo(v.online_countdown)
      v.current_light_committee = CompactCommitteeType.unwrap(current_light_committee)
      v.next_light_committee = CompactCommitteeType.unwrap(next_light_committee)
      exposed_derived_secrets.copyTo(v.exposed_derived_secrets)
      return BeaconStateWrapper(v)
    }
  }

  override val genesis_time: uint64
    get() = UInt64Type.wrap(v.genesis_time)
  override var genesis_validators_root: Root
    get() = Bytes32Type.wrap(v.genesis_validators_root)
    set(value) {
      v.genesis_validators_root = value
    }
  override var slot: Slot
    get() = UInt64Type.wrap(v.slot)
    set(value) {
      v.slot = UInt64Type.unwrap(value)
    }
  override var fork: Fork
    get() = ForkType.wrap(v.fork)
    set(value) {
      v.fork = ForkType.unwrap(value)
    }
  override var latest_block_header: BeaconBlockHeader
    get() = BeaconBlockHeaderType.wrap(v.latest_block_header) { value ->
      this.latest_block_header = value
    }
    set(value) {
      this.v.latest_block_header = BeaconBlockHeaderType.unwrap(value)
    }
  override val block_roots: SSZMutableVector<Root>
    get() = SSZMutableVectorType(Bytes32Type).wrap(v.block_roots)
  override val state_roots: SSZMutableVector<Root>
    get() = SSZMutableVectorType(Bytes32Type).wrap(v.state_roots)
  override val historical_roots: SSZMutableList<Root>
    get() = SSZMutableListType(Bytes32Type).wrap(v.historical_roots)
  override var eth1_data: Eth1Data
    get() = Eth1DataType.wrap(v.eth1_data) { value -> this.eth1_data = value }
    set(value) {
      v.eth1_data = Eth1DataType.unwrap(value)
    }
  override var eth1_data_votes: SSZMutableList<Eth1Data>
    get() = SSZMutableListType(Eth1DataType).wrap(v.eth1_data_votes)
    set(value) {
      v.eth1_data_votes.clear()
      value.copyTo(v.eth1_data_votes)
    }
  override var eth1_deposit_index: uint64
    get() = UInt64Type.wrap(v.eth1_deposit_index)
    set(value) {
      v.eth1_deposit_index = UInt64Type.unwrap(value)
    }
  override val validators: SSZMutableList<Validator>
    get() = SSZMutableListType(ValidatorType).wrap(v.validators)
  override val balances: SSZMutableList<Gwei>
    get() = SSZMutableListType(UInt64Type).wrap(v.balances)
  override val randao_mixes: SSZMutableVector<Root>
    get() = SSZMutableVectorType(Bytes32Type).wrap(v.randao_mixes)
  override val slashings: SSZMutableVector<Gwei>
    get() = SSZMutableVectorType(UInt64Type).wrap(v.slashings)
  override var previous_epoch_attestations: SSZMutableList<PendingAttestation>
    get() = SSZMutableListType(PendingAttestationType).wrap(v.previous_epoch_attestations)
    set(value) {
      v.previous_epoch_attestations.clear()
      value.copyTo(v.previous_epoch_attestations)
    }
  override var current_epoch_attestations: SSZMutableList<PendingAttestation>
    get() = SSZMutableListType(PendingAttestationType).wrap(v.current_epoch_attestations)
    set(value) {
      v.current_epoch_attestations.clear()
      value.copyTo(v.current_epoch_attestations)
    }
  override val justification_bits: SSZBitVector
    get() = SSZBitVectorType.wrap(v.justification_bits)
  override var previous_justified_checkpoint: Checkpoint
    get() = CheckpointType.wrap(v.previous_justified_checkpoint)
    set(value) {
      v.previous_justified_checkpoint = CheckpointType.unwrap(value)
    }
  override var current_justified_checkpoint: Checkpoint
    get() = CheckpointType.wrap(v.current_justified_checkpoint)
    set(value) {
      v.current_justified_checkpoint = CheckpointType.unwrap(value)
    }
  override var finalized_checkpoint: Checkpoint
    get() = CheckpointType.wrap(v.finalized_checkpoint)
    set(value) {
      v.finalized_checkpoint = CheckpointType.unwrap(value)
    }
  override val shard_states: SSZMutableList<ShardState>
    get() = SSZMutableListType(ShardStateType).wrap(v.shard_states)
  override val online_countdown: SSZMutableList<OnlineEpochs>
    get() = SSZMutableListType(UInt8Type).wrap(v.online_countdown)
  override var current_light_committee: CompactCommittee
    get() = CompactCommitteeType.wrap(v.current_light_committee)
    set(value) {
      v.current_light_committee = CompactCommitteeType.unwrap(value)
    }
  override var next_light_committee: CompactCommittee
    get() = CompactCommitteeType.wrap(v.next_light_committee)
    set(value) {
      v.next_light_committee = CompactCommitteeType.unwrap(value)
    }
  override val exposed_derived_secrets: SSZMutableVector<ExposedValidatorIndices>
    get() = SSZMutableVectorType(ExposedValidatorIndicesType).wrap(v.exposed_derived_secrets)

  override fun hash_tree_root() = v.hash_tree_root()

  override fun copy(
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
  ): BeaconState = create(
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

  override fun equals(other: Any?): Boolean {
    if (other is BeaconStateWrapper) {
      return v == other.v
    }
    return false
  }

  override fun hashCode(): Int {
    return v.hashCode()
  }

  override fun toString(): String {
    return v.toString()
  }
}
