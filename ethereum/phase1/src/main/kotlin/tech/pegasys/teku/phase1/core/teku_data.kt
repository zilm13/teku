package tech.pegasys.teku.phase1.core

import com.google.common.primitives.UnsignedLong
import tech.pegasys.teku.bls.BLSPublicKey
import tech.pegasys.teku.phase1.core.TypeConverter.Companion.cast
import tech.pegasys.teku.phase1.ssz.Bytes32
import tech.pegasys.teku.phase1.ssz.CDict
import tech.pegasys.teku.phase1.ssz.SSZBitList
import tech.pegasys.teku.phase1.ssz.SSZBitVector
import tech.pegasys.teku.phase1.ssz.SSZByteList
import tech.pegasys.teku.phase1.ssz.SSZMutableList
import tech.pegasys.teku.phase1.ssz.SSZMutableVector
import tech.pegasys.teku.phase1.ssz.boolean
import tech.pegasys.teku.phase1.ssz.uint64
import kotlin.reflect.KClass
import tech.pegasys.teku.bls.BLSSignature as TekuBLSSignature
import tech.pegasys.teku.datastructures.blocks.BeaconBlock as TekuBeaconBlock
import tech.pegasys.teku.datastructures.blocks.BeaconBlockBody as TekuBeaconBlockBody
import tech.pegasys.teku.datastructures.blocks.BeaconBlockHeader as TekuBeaconBlockHeader
import tech.pegasys.teku.datastructures.blocks.Eth1Data as TekuEth1Data
import tech.pegasys.teku.datastructures.blocks.SignedBeaconBlock as TekuSignedBeaconBlock
import tech.pegasys.teku.datastructures.blocks.SignedBeaconBlockHeader as TekuSignedBeaconBlockHeader
import tech.pegasys.teku.datastructures.operations.Attestation as TekuAttestation
import tech.pegasys.teku.datastructures.operations.AttestationData as TekuAttestationData
import tech.pegasys.teku.datastructures.operations.AttesterSlashing as TekuAttesterSlashing
import tech.pegasys.teku.datastructures.operations.Deposit as TekuDeposit
import tech.pegasys.teku.datastructures.operations.DepositData as TekuDepositData
import tech.pegasys.teku.datastructures.operations.DepositMessage as TekuDepositMessage
import tech.pegasys.teku.datastructures.operations.IndexedAttestation as TekuIndexedAttestation
import tech.pegasys.teku.datastructures.operations.ProposerSlashing as TekuProposerSlashing
import tech.pegasys.teku.datastructures.operations.SignedVoluntaryExit as TekuSignedVoluntaryExit
import tech.pegasys.teku.datastructures.operations.VoluntaryExit as TekuVoluntaryExit
import tech.pegasys.teku.datastructures.state.Checkpoint as TekuCheckpoint
import tech.pegasys.teku.datastructures.state.Fork as TekuFork
import tech.pegasys.teku.datastructures.state.ForkData as TekuForkData
import tech.pegasys.teku.datastructures.state.HistoricalBatch as TekuHistoricalBatch
import tech.pegasys.teku.datastructures.state.MutableBeaconState as TekuBeaconState
import tech.pegasys.teku.datastructures.state.PendingAttestation as TekuPendingAttestation
import tech.pegasys.teku.datastructures.state.SigningRoot as TekuSigningRoot
import tech.pegasys.teku.datastructures.state.Validator as TekuValidator
import tech.pegasys.teku.ssz.SSZTypes.Bitlist as TekuBitlist
import tech.pegasys.teku.ssz.SSZTypes.Bitvector as TekuBitvector
import tech.pegasys.teku.ssz.SSZTypes.Bytes4 as TekuBytes4
import tech.pegasys.teku.ssz.SSZTypes.SSZList as TekuSSZList
import tech.pegasys.teku.ssz.SSZTypes.SSZMutableList as TekuSSZMutableList
import tech.pegasys.teku.ssz.SSZTypes.SSZVector as TekuSSZVector

typealias Callback<T> = (T) -> Unit

internal class TekuValidatorIndicesSSZList(internal val delegate: TekuSSZMutableList<UnsignedLong>)
  : TekuSSZMutableList<UnsignedLong> {

  override fun clear() {
    delegate.clear()
  }

  override fun getElementType(): Class<out UnsignedLong> {
    return delegate.elementType
  }

  override fun add(element: UnsignedLong?) {
    delegate.add(element)
  }

  override fun size(): Int {
    return delegate.size()
  }

  override fun get(index: Int): UnsignedLong {
    return delegate.get(index)
  }

  override fun hash_tree_root(): org.apache.tuweni.bytes.Bytes32 {
    return delegate.hash_tree_root()
  }

  override fun set(index: Int, element: UnsignedLong?) {
    delegate.set(index, element)
  }

  override fun getMaxSize(): Long {
    return delegate.maxSize
  }
}

internal class ValidatorIndicesSSZListDelegate(
    data: TekuValidatorIndicesSSZList,
    type: KClass<ValidatorIndex>
) : ValidatorIndicesSSZList, SSZMutableListDelegate<ValidatorIndex, UnsignedLong>(data, type) {
  constructor(items: MutableList<ValidatorIndex>, maxSize: ULong)
      : this(TekuValidatorIndicesSSZList(
      TekuSSZList.createMutable(
          items.map { cast(it, UnsignedLong::class) }.toList(),
          maxSize.toLong(),
          UnsignedLong::class.java
      )
  ),
      ValidatorIndex::class)
}

abstract internal class Mutable<T>(internal var callback: Callback<T>?) {
  protected inline fun onUpdate(value: T) = callback?.invoke(value)
}

internal class ForkDelegate(
    internal val v: TekuFork
) : Fork {

  constructor(previous_version: Version, current_version: Version, epoch: Epoch)
      : this(TekuFork(
      cast(previous_version, TekuBytes4::class),
      cast(current_version, TekuBytes4::class),
      cast(epoch, UnsignedLong::class)
  ))

  override val previous_version: Version
    get() = cast(v.previous_version, Version::class)
  override val current_version: Version
    get() = cast(v.current_version, Version::class)
  override val epoch: Epoch
    get() = cast(v.epoch, Epoch::class)

  override fun hash_tree_root(): Bytes32 = v.hash_tree_root()
}

internal class ForkDataDelegate(
    internal val v: TekuForkData
) : ForkData {

  constructor(current_version: Version, genesis_validators_root: Root)
      : this(TekuForkData(cast(current_version, TekuBytes4::class), genesis_validators_root))

  override val current_version: Version
    get() = cast(v.currentVersion, Version::class)
  override val genesis_validators_root: Root
    get() = cast(v.genesisValidatorsRoot, Root::class)

  override fun hash_tree_root(): Bytes32 = v.hash_tree_root()
}

internal class CheckpointDelegate(
    internal val v: TekuCheckpoint
) : Checkpoint {

  constructor(epoch: Epoch, root: Root)
      : this(TekuCheckpoint(cast(epoch, UnsignedLong::class), root))

  override val epoch: Epoch
    get() = cast(v.epoch, Epoch::class)
  override val root: Root
    get() = v.root

  override fun hash_tree_root() = v.hash_tree_root()
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
    get() = v.withdrawal_credentials
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

internal class AttestationDataDelegate(
    internal val v: TekuAttestationData
) : AttestationData {

  constructor(
      slot: Slot,
      index: CommitteeIndex,
      beacon_block_root: Root,
      source: Checkpoint,
      target: Checkpoint,
      head_shard_root: Root,
      shard_transition_root: Root
  ) : this(
      TekuAttestationData(
          cast(slot, UnsignedLong::class),
          cast(index, UnsignedLong::class),
          beacon_block_root,
          cast(source, TekuCheckpoint::class),
          cast(target, TekuCheckpoint::class)
      )
  )

  override val slot: Slot
    get() = cast(v.slot, Slot::class)
  override val index: CommitteeIndex
    get() = cast(v.index, CommitteeIndex::class)
  override val beacon_block_root: Root
    get() = v.beacon_block_root
  override val source: Checkpoint
    get() = CheckpointDelegate(v.source)
  override val target: Checkpoint
    get() = CheckpointDelegate(v.target)
  override val head_shard_root: Root
    get() = TODO("Not yet implemented")
  override val shard_transition_root: Root
    get() = TODO("Not yet implemented")

  override fun hash_tree_root() = v.hash_tree_root()
}

internal class PendingAttestationDelegate(
    internal val v: TekuPendingAttestation,
    onUpdate: Callback<PendingAttestation>? = null
) : PendingAttestation, Mutable<PendingAttestation>(onUpdate) {

  constructor(
      aggregation_bits: SSZBitList,
      data: AttestationData,
      inclusion_delay: Slot,
      proposer_index: ValidatorIndex,
      crosslink_success: boolean
  ) : this(
      TekuPendingAttestation(
          cast(aggregation_bits, TekuBitlist::class),
          cast(data, TekuAttestationData::class),
          cast(inclusion_delay, UnsignedLong::class),
          cast(proposer_index, UnsignedLong::class)
      )
  )

  override val aggregation_bits: SSZBitList
    get() = SSZBitListDelegate(v.aggregation_bits)
  override val data: AttestationData
    get() = AttestationDataDelegate(v.data)
  override val inclusion_delay: Slot
    get() = cast(v.inclusion_delay, Slot::class)
  override val proposer_index: ValidatorIndex
    get() = cast(v.proposer_index, ValidatorIndex::class)
  override var crosslink_success: boolean
    get() = TODO("Not yet implemented")
    set(value) {}

  override fun hash_tree_root() = v.hash_tree_root()
}

internal class Eth1DataDelegate(
    internal var v: TekuEth1Data,
    onUpdate: Callback<Eth1Data>? = null
) : Eth1Data, Mutable<Eth1Data>(onUpdate) {

  constructor(
      deposit_root: Root,
      deposit_count: uint64,
      block_hash: Bytes32
  ) : this(
      TekuEth1Data(
          deposit_root,
          cast(deposit_count, UnsignedLong::class),
          block_hash
      )
  )

  override var deposit_root: Root
    get() = v.deposit_root
    set(value) {
      v = TekuEth1Data(
          value,
          v.deposit_count,
          v.block_hash
      )
      onUpdate(this)
    }
  override val deposit_count: uint64
    get() = cast(v.deposit_count, uint64::class)
  override val block_hash: Bytes32
    get() = v.block_hash

  override fun hash_tree_root() = v.hash_tree_root()
}

internal class HistoricalBatchDelegate(
    internal val v: TekuHistoricalBatch
) : HistoricalBatch {

  constructor(block_roots: SSZMutableVector<Root>, state_roots: SSZMutableVector<Root>)
      : this(TekuHistoricalBatch(
      cast(block_roots, TekuSSZVector::class) as TekuSSZVector<Root>,
      cast(state_roots, TekuSSZVector::class) as TekuSSZVector<Root>
  ))

  override val block_roots: SSZMutableVector<Root>
    get() = SSZMutableVectorDelegate(v.blockRoots, Root::class)
  override val state_roots: SSZMutableVector<Root>
    get() = SSZMutableVectorDelegate(v.stateRoots, Root::class)

  override fun hash_tree_root() = v.hash_tree_root()
}

internal class DepositMessageDelegate(
    internal val v: TekuDepositMessage
) : DepositMessage {

  constructor(pubkey: BLSPubkey, withdrawal_credentials: Bytes32, amount: Gwei)
      : this(
      TekuDepositMessage(
          cast(pubkey, BLSPublicKey::class),
          withdrawal_credentials,
          cast(amount, UnsignedLong::class)
      )
  )

  override val pubkey: BLSPubkey
    get() = cast(v.pubkey, BLSPubkey::class)
  override val withdrawal_credentials: Bytes32
    get() = v.withdrawal_credentials
  override val amount: Gwei
    get() = cast(v.amount, Gwei::class)

  override fun hash_tree_root() = v.hash_tree_root()
}

internal class DepositDataDelegate(
    internal val v: TekuDepositData
) : DepositData {

  constructor(
      pubkey: BLSPubkey,
      withdrawal_credentials: Bytes32,
      amount: Gwei,
      signature: BLSSignature
  ) : this(
      TekuDepositData(
          cast(pubkey, BLSPublicKey::class),
          withdrawal_credentials,
          cast(amount, UnsignedLong::class),
          cast(signature, TekuBLSSignature::class)
      )
  )

  override val pubkey: BLSPubkey
    get() = cast(v.pubkey, BLSPubkey::class)
  override val withdrawal_credentials: Bytes32
    get() = v.withdrawal_credentials
  override val amount: Gwei
    get() = cast(v.amount, Gwei::class)
  override val signature: BLSSignature
    get() = cast(v.signature, BLSSignature::class)

  override fun hash_tree_root() = v.hash_tree_root()
}

internal class BeaconBlockHeaderDelegate(
    internal var v: TekuBeaconBlockHeader,
    onUpdate: Callback<BeaconBlockHeader>? = null
) : BeaconBlockHeader, Mutable<BeaconBlockHeader>(onUpdate) {

  constructor(
      slot: Slot,
      proposer_index: ValidatorIndex,
      parent_root: Root,
      state_root: Root,
      body_root: Root
  ) : this(
      TekuBeaconBlockHeader(
          cast(slot, UnsignedLong::class),
          cast(proposer_index, UnsignedLong::class),
          parent_root,
          state_root,
          body_root
      )
  )

  override val slot: Slot
    get() = cast(v.slot, Slot::class)
  override val proposer_index: ValidatorIndex
    get() = cast(v.proposer_index, ValidatorIndex::class)
  override val parent_root: Root
    get() = v.parent_root
  override var state_root: Root
    get() = v.state_root
    set(value) {
      v = TekuBeaconBlockHeader(
          v.slot,
          v.proposer_index,
          v.parent_root,
          value,
          v.body_root
      )
    }
  override val body_root: Root
    get() = v.state_root

  override fun copy(
      slot: Slot,
      proposer_index: ValidatorIndex,
      parent_root: Root,
      state_root: Root,
      body_root: Root
  ) = BeaconBlockHeaderDelegate(slot, proposer_index, parent_root, state_root, body_root)

  override fun hash_tree_root() = v.hash_tree_root()
}

internal class SigningRootDelegate(
    internal val v: TekuSigningRoot
) : SigningRoot {

  constructor(object_root: Root, domain: Domain) : this(TekuSigningRoot(object_root, domain))

  override val object_root: Root
    get() = v.objectRoot
  override val domain: Domain
    get() = cast(v.domain, Domain::class)

  override fun hash_tree_root() = v.hash_tree_root()
}

internal class AttestationDelegate(
    internal val v: TekuAttestation
) : Attestation {

  constructor(
      aggregation_bits: SSZBitList,
      data: AttestationData,
      custody_bits_blocks: SSZMutableList<SSZBitList>,
      signature: BLSSignature
  ) : this(
      TekuAttestation(
          cast(aggregation_bits, TekuBitlist::class),
          cast(data, TekuAttestationData::class),
          cast(signature, TekuBLSSignature::class)
      )
  )

  override val aggregation_bits: SSZBitList
    get() = SSZBitListDelegate(v.aggregation_bits)
  override val data: AttestationData
    get() = AttestationDataDelegate(v.data)
  override val custody_bits_blocks: SSZMutableList<SSZBitList>
    get() = TODO("Not yet implemented")
  override val signature: BLSSignature
    get() = cast(v.aggregate_signature, BLSSignature::class)

  override fun hash_tree_root() = v.hash_tree_root()
}

internal class IndexedAttestationDelegate(
    internal val v: TekuIndexedAttestation
) : IndexedAttestation {

  constructor(committee: SSZMutableList<ValidatorIndex>, attestation: Attestation)
      : this(
      TekuIndexedAttestation(
          cast(committee, TekuSSZList::class) as TekuSSZList<UnsignedLong>,
          cast(attestation.data, TekuAttestationData::class),
          cast(attestation.signature, TekuBLSSignature::class)
      )
  )

  override val committee: SSZMutableList<ValidatorIndex>
    get() = SSZMutableListDelegate(v.attesting_indices, ValidatorIndex::class)
  override val attestation: Attestation
    get() = TODO("Not yet implemented")

  override fun hash_tree_root() = v.hash_tree_root()
}

internal class AttesterSlashingDelegate(
    internal val v: TekuAttesterSlashing
) : AttesterSlashing {

  constructor(attestation_1: IndexedAttestation, attestation_2: IndexedAttestation)
      : this(
      TekuAttesterSlashing(
          cast(attestation_1, TekuIndexedAttestation::class),
          cast(attestation_2, TekuIndexedAttestation::class)
      )
  )

  override val attestation_1: IndexedAttestation
    get() = IndexedAttestationDelegate(v.attestation_1)
  override val attestation_2: IndexedAttestation
    get() = IndexedAttestationDelegate(v.attestation_2)

  override fun hash_tree_root() = v.hash_tree_root()
}

internal class DepositDelegate(
    internal val v: TekuDeposit
) : Deposit {

  constructor(proof: SSZMutableVector<Bytes32>, data: DepositData)
      : this(
      TekuDeposit(
          cast(proof, TekuSSZVector::class) as TekuSSZVector<Bytes32>,
          cast(data, TekuDepositData::class)
      )
  )

  override val proof: SSZMutableVector<Bytes32>
    get() = SSZMutableVectorDelegate(v.proof, Bytes32::class)
  override val data: DepositData
    get() = DepositDataDelegate(v.data)

  override fun hash_tree_root() = v.hash_tree_root()
}

internal class VoluntaryExitDelegate(
    internal val v: TekuVoluntaryExit
) : VoluntaryExit {

  constructor(epoch: Epoch, validator_index: ValidatorIndex)
      : this(
      TekuVoluntaryExit(
          cast(epoch, UnsignedLong::class),
          cast(validator_index, UnsignedLong::class)
      )
  )

  override val epoch: Epoch
    get() = cast(v.epoch, Epoch::class)
  override val validator_index: ValidatorIndex
    get() = cast(v.validator_index, ValidatorIndex::class)

  override fun hash_tree_root() = v.hash_tree_root()
}

internal class SignedVoluntaryExitDelegate(
    internal val v: TekuSignedVoluntaryExit
) : SignedVoluntaryExit {

  constructor(message: VoluntaryExit, signature: BLSSignature)
      : this(
      TekuSignedVoluntaryExit(
          cast(message, TekuVoluntaryExit::class),
          cast(signature, TekuBLSSignature::class)
      )
  )

  override val message: VoluntaryExit
    get() = cast(v.message, VoluntaryExit::class)
  override val signature: BLSSignature
    get() = cast(v.signature, BLSSignature::class)

  override fun hash_tree_root() = v.hash_tree_root()
}

internal class SignedBeaconBlockHeaderDelegate(
    internal val v: TekuSignedBeaconBlockHeader
) : SignedBeaconBlockHeader {

  constructor(message: BeaconBlockHeader, signature: BLSSignature)
      : this(
      TekuSignedBeaconBlockHeader(
          cast(message, TekuBeaconBlockHeader::class),
          cast(signature, TekuBLSSignature::class)
      )
  )

  override val message: BeaconBlockHeader
    get() = BeaconBlockHeaderDelegate(v.message)
  override val signature: BLSSignature
    get() = cast(v.signature, BLSSignature::class)

  override fun hash_tree_root() = v.hash_tree_root()
}

internal class ProposerSlashingDelegate(
    internal val v: TekuProposerSlashing
) : ProposerSlashing {

  constructor(signed_header_1: SignedBeaconBlockHeader, signed_header_2: SignedBeaconBlockHeader)
      : this(
      TekuProposerSlashing(
          cast(signed_header_1, TekuSignedBeaconBlockHeader::class),
          cast(signed_header_2, TekuSignedBeaconBlockHeader::class)
      )
  )

  override val signed_header_1: SignedBeaconBlockHeader
    get() = SignedBeaconBlockHeaderDelegate(v.header_1)
  override val signed_header_2: SignedBeaconBlockHeader
    get() = SignedBeaconBlockHeaderDelegate(v.header_2)

  override fun hash_tree_root() = v.hash_tree_root()
}

internal class BeaconBlockBodyDelegate(
    internal val v: TekuBeaconBlockBody
) : BeaconBlockBody {

  constructor(
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
      light_client_signature_bitfield: SSZBitVector,
      light_client_signature: BLSSignature
  ) : this(
      TekuBeaconBlockBody(
          cast(randao_reveal, TekuBLSSignature::class),
          cast(eth1_data, TekuEth1Data::class),
          graffiti,
          cast(proposer_slashings, TekuSSZList::class) as TekuSSZList<TekuProposerSlashing>,
          cast(attester_slashings, TekuSSZList::class) as TekuSSZList<TekuAttesterSlashing>,
          cast(attestations, TekuSSZList::class) as TekuSSZList<TekuAttestation>,
          cast(deposits, TekuSSZList::class) as TekuSSZList<TekuDeposit>,
          cast(voluntary_exits, TekuSSZList::class) as TekuSSZList<TekuSignedVoluntaryExit>
      )
  )

  override val randao_reveal: BLSSignature
    get() = cast(v.randao_reveal, BLSSignature::class)
  override val eth1_data: Eth1Data
    get() = cast(v.eth1_data, Eth1Data::class)
  override val graffiti: Bytes32
    get() = v.graffiti
  override val proposer_slashings: SSZMutableList<ProposerSlashing>
    get() = SSZMutableListDelegate(v.proposer_slashings, ProposerSlashing::class)
  override val attester_slashings: SSZMutableList<AttesterSlashing>
    get() = SSZMutableListDelegate(v.attester_slashings, AttesterSlashing::class)
  override val attestations: SSZMutableList<Attestation>
    get() = SSZMutableListDelegate(v.attestations, Attestation::class)
  override val deposits: SSZMutableList<Deposit>
    get() = SSZMutableListDelegate(v.deposits, Deposit::class)
  override val voluntary_exits: SSZMutableList<SignedVoluntaryExit>
    get() = SSZMutableListDelegate(v.voluntary_exits, SignedVoluntaryExit::class)
  override val custody_slashings: SSZMutableList<SignedCustodySlashing>
    get() = TODO("Not yet implemented")
  override val custody_key_reveals: SSZMutableList<CustodyKeyReveal>
    get() = TODO("Not yet implemented")
  override val early_derived_secret_reveals: SSZMutableList<EarlyDerivedSecretReveal>
    get() = TODO("Not yet implemented")
  override val shard_transitions: SSZMutableVector<ShardTransition>
    get() = TODO("Not yet implemented")
  override val light_client_signature_bitfield: SSZBitVector
    get() = TODO("Not yet implemented")
  override val light_client_signature: BLSSignature
    get() = TODO("Not yet implemented")

  override fun hash_tree_root() = v.hash_tree_root()
}

internal class BeaconBlockDelegate(
    internal val v: TekuBeaconBlock
) : BeaconBlock {

  constructor(
      slot: Slot,
      proposer_index: ValidatorIndex,
      parent_root: Root,
      state_root: Root,
      body: BeaconBlockBody
  ) : this(
      TekuBeaconBlock(
          cast(slot, UnsignedLong::class),
          cast(proposer_index, UnsignedLong::class),
          parent_root,
          state_root,
          cast(body, TekuBeaconBlockBody::class)
      )
  )

  override val slot: Slot
    get() = cast(v.slot, Slot::class)
  override val proposer_index: ValidatorIndex
    get() = cast(v.proposer_index, ValidatorIndex::class)
  override val parent_root: Root
    get() = v.parent_root
  override val state_root: Root
    get() = v.state_root
  override val body: BeaconBlockBody
    get() = BeaconBlockBodyDelegate(v.body)

  override fun hash_tree_root() = v.hash_tree_root()
}

internal class SignedBeaconBlockDelegate(
    internal val v: TekuSignedBeaconBlock
) : SignedBeaconBlock {

  constructor(message: BeaconBlock, signature: BLSSignature)
      : this(
      TekuSignedBeaconBlock(
          cast(message, TekuBeaconBlock::class),
          cast(signature, TekuBLSSignature::class)
      )
  )

  override val message: BeaconBlock
    get() = BeaconBlockDelegate(v.message)
  override val signature: BLSSignature
    get() = cast(v.signature, BLSSignature::class)

  override fun hash_tree_root() = v.hash_tree_root()
}

internal class BeaconStateDelegate(internal var v: TekuBeaconState) : BeaconState {

  constructor(genesis_time: uint64,
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
              exposed_derived_secrets: SSZMutableVector<ValidatorIndicesSSZList>) {
    v = TekuBeaconState.createBuilder()
    v.genesis_time = cast(genesis_time, UnsignedLong::class)
    v.genesis_validators_root = genesis_validators_root
    v.slot = cast(slot, UnsignedLong::class)
    v.fork = cast(fork, tech.pegasys.teku.datastructures.state.Fork::class)
    v.latest_block_header = cast(latest_block_header, tech.pegasys.teku.datastructures.blocks.BeaconBlockHeader::class)
    block_roots.copyTo(v.block_roots)
    state_roots.copyTo(v.state_roots)
    historical_roots.copyTo(v.historical_roots)
    v.eth1_data = cast(eth1_data, tech.pegasys.teku.datastructures.blocks.Eth1Data::class)
    eth1_data_votes.copyTo(v.eth1_data_votes)
    v.eth1_deposit_index = cast(eth1_deposit_index, UnsignedLong::class)
    validators.copyTo(v.validators)
    balances.copyTo(v.balances)
    randao_mixes.copyTo(v.randao_mixes)
    slashings.copyTo(v.slashings)
    previous_epoch_attestations.copyTo(v.previous_epoch_attestations)
    current_epoch_attestations.copyTo(v.current_epoch_attestations)
    v.justification_bits = cast(justification_bits, TekuBitvector::class)
    v.previous_justified_checkpoint = cast(previous_justified_checkpoint, tech.pegasys.teku.datastructures.state.Checkpoint::class)
    v.current_justified_checkpoint = cast(current_justified_checkpoint, tech.pegasys.teku.datastructures.state.Checkpoint::class)
    v.finalized_checkpoint = cast(finalized_checkpoint, tech.pegasys.teku.datastructures.state.Checkpoint::class)
  }

  override val genesis_time: uint64
    get() = cast(v.genesis_time, uint64::class)
  override var genesis_validators_root: Root
    get() = v.genesis_validators_root
    set(value) {
      v.genesis_validators_root = value
    }
  override var slot: Slot
    get() = cast(v.slot, uint64::class)
    set(value) {
      v.slot = cast(value, UnsignedLong::class)
    }
  override var fork: Fork
    get() = ForkDelegate(v.fork)
    set(value) {
      v.fork = cast(value, TekuFork::class)
    }
  override var latest_block_header: BeaconBlockHeader
    get() = BeaconBlockHeaderDelegate(v.latest_block_header) { value -> this.latest_block_header = value }
    set(value) {
      this.v.latest_block_header = cast(value, TekuBeaconBlockHeader::class)
    }
  override val block_roots: SSZMutableVector<Root>
    get() = SSZMutableVectorDelegate(v.block_roots, Root::class)
  override val state_roots: SSZMutableVector<Root>
    get() = SSZMutableVectorDelegate(v.state_roots, Root::class)
  override val historical_roots: SSZMutableList<Root>
    get() = SSZMutableListDelegate(v.historical_roots, Root::class)
  override var eth1_data: Eth1Data
    get() = Eth1DataDelegate(v.eth1_data) { value -> this.eth1_data = value }
    set(value) {
      v.eth1_data = cast(eth1_data, TekuEth1Data::class)
    }
  override var eth1_data_votes: SSZMutableList<Eth1Data>
    get() = SSZMutableListDelegate(v.eth1_data_votes, Eth1Data::class)
    set(value) {
      v.eth1_data_votes.clear()
      value.copyTo(v.eth1_data_votes)
    }
  override var eth1_deposit_index: uint64
    get() = cast(v.eth1_deposit_index, uint64::class)
    set(value) {
      v.eth1_deposit_index = cast(value, UnsignedLong::class)
    }
  override val validators: SSZMutableList<Validator>
    get() = SSZMutableListDelegate(v.validators, Validator::class)
  override val balances: SSZMutableList<Gwei>
    get() = SSZMutableListDelegate(v.balances, Gwei::class)
  override val randao_mixes: SSZMutableVector<Root>
    get() = SSZMutableVectorDelegate(v.randao_mixes, Root::class)
  override val slashings: SSZMutableVector<Gwei>
    get() = SSZMutableVectorDelegate(v.slashings, Gwei::class)
  override var previous_epoch_attestations: SSZMutableList<PendingAttestation>
    get() = SSZMutableListDelegate(v.previous_epoch_attestations, PendingAttestation::class)
    set(value) {
      v.previous_epoch_attestations.clear()
      value.copyTo(v.previous_epoch_attestations)
    }
  override var current_epoch_attestations: SSZMutableList<PendingAttestation>
    get() = SSZMutableListDelegate(v.current_epoch_attestations, PendingAttestation::class)
    set(value) {
      v.current_epoch_attestations.clear()
      value.copyTo(v.current_epoch_attestations)
    }
  override val justification_bits: SSZBitVector
    get() = SSZBitVectorDelegate(v.justification_bits)
  override var previous_justified_checkpoint: Checkpoint
    get() = CheckpointDelegate(v.previous_justified_checkpoint)
    set(value) {
      v.previous_justified_checkpoint = cast(value, TekuCheckpoint::class)
    }
  override var current_justified_checkpoint: Checkpoint
    get() = CheckpointDelegate(v.current_justified_checkpoint)
    set(value) {
      v.current_justified_checkpoint = cast(value, TekuCheckpoint::class)
    }
  override var finalized_checkpoint: Checkpoint
    get() = CheckpointDelegate(v.finalized_checkpoint)
    set(value) {
      v.finalized_checkpoint = cast(value, TekuCheckpoint::class)
    }
  override val shard_states: SSZMutableList<ShardState>
    get() = TODO("Not yet implemented")
  override val online_countdown: SSZMutableList<OnlineEpochs>
    get() = TODO("Not yet implemented")
  override var current_light_committee: CompactCommittee
    get() = TODO("Not yet implemented")
    set(value) {}
  override var next_light_committee: CompactCommittee
    get() = TODO("Not yet implemented")
    set(value) {}
  override val exposed_derived_secrets: SSZMutableVector<ValidatorIndicesSSZList>
    get() = TODO("Not yet implemented")

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
      exposed_derived_secrets: SSZMutableVector<ValidatorIndicesSSZList>
  ): BeaconState = BeaconStateDelegate(
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
}

internal class StoreDelegate(
    override var time: uint64,
    override val genesis_time: uint64,
    override var justified_checkpoint: Checkpoint,
    override var finalized_checkpoint: Checkpoint,
    override var best_justified_checkpoint: Checkpoint,
    override var blocks: CDict<Root, BeaconBlockHeader>,
    override var block_states: CDict<Root, BeaconState>,
    override var checkpoint_states: CDict<Checkpoint, BeaconState>,
    override var latest_messages: CDict<ValidatorIndex, LatestMessage>
) : Store

internal class LatestMessageImpl(override val epoch: Epoch, override val root: Root) : LatestMessage {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as LatestMessageImpl

    if (epoch != other.epoch) return false
    if (root != other.root) return false

    return true
  }

  override fun hashCode(): Int {
    var result = epoch.hashCode()
    result = 31 * result + root.hashCode()
    return result
  }

  override fun toString(): String {
    return "LatestMessageImpl(epoch=$epoch, root=$root)"
  }
}

class TekuDataObjectFactory : DataObjectFactory {

  override fun ValidatorIndicesSSZList(
      maxSize: ULong, items: MutableList<ValidatorIndex>): ValidatorIndicesSSZList {
    return ValidatorIndicesSSZListDelegate(items, maxSize)
  }

  override fun Fork(previous_version: Version, current_version: Version, epoch: Epoch): Fork =
      ForkDelegate(previous_version, current_version, epoch)

  override fun ForkData(current_version: Version, genesis_validators_root: Root): ForkData =
      ForkDataDelegate(current_version, genesis_validators_root)

  override fun Checkpoint(epoch: Epoch, root: Root): Checkpoint =
      CheckpointDelegate(epoch, root)

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
  ): Validator = ValidatorDelegate(
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
  ): AttestationData = AttestationDataDelegate(
      slot, index, beacon_block_root, source, target, head_shard_root, shard_transition_root)

  override fun PendingAttestation(
      aggregation_bits: SSZBitList,
      data: AttestationData,
      inclusion_delay: Slot,
      proposer_index: ValidatorIndex,
      crosslink_success: boolean
  ): PendingAttestation = PendingAttestationDelegate(
      aggregation_bits, data, inclusion_delay, proposer_index, crosslink_success)

  override fun Eth1Data(deposit_root: Root, deposit_count: uint64, block_hash: Bytes32): Eth1Data =
      Eth1DataDelegate(deposit_root, deposit_count, block_hash)

  override fun HistoricalBatch(
      block_roots: SSZMutableVector<Root>, state_roots: SSZMutableVector<Root>
  ): HistoricalBatch = HistoricalBatchDelegate(block_roots, state_roots)

  override fun DepositMessage(
      pubkey: BLSPubkey, withdrawal_credentials: Bytes32, amount: Gwei
  ): DepositMessage = DepositMessageDelegate(pubkey, withdrawal_credentials, amount)

  override fun DepositData(
      pubkey: BLSPubkey,
      withdrawal_credentials: Bytes32,
      amount: Gwei,
      signature: BLSSignature
  ): DepositData =
      DepositDataDelegate(pubkey, withdrawal_credentials, amount, signature)

  override fun BeaconBlockHeader(
      slot: Slot,
      proposer_index: ValidatorIndex,
      parent_root: Root,
      state_root: Root,
      body_root: Root
  ): BeaconBlockHeader = BeaconBlockHeaderDelegate(
      slot, proposer_index, parent_root, state_root, body_root)

  override fun SigningRoot(object_root: Root, domain: Domain): SigningRoot =
      SigningRootDelegate(object_root, domain)

  override fun Attestation(
      aggregation_bits: SSZBitList,
      data: AttestationData,
      custody_bits_blocks: SSZMutableList<SSZBitList>,
      signature: BLSSignature
  ): Attestation = AttestationDelegate(aggregation_bits, data, custody_bits_blocks, signature)

  override fun IndexedAttestation(
      committee: SSZMutableList<ValidatorIndex>,
      attestation: Attestation
  ): IndexedAttestation = IndexedAttestationDelegate(committee, attestation)

  override fun AttesterSlashing(
      attestation_1: IndexedAttestation,
      attestation_2: IndexedAttestation
  ): AttesterSlashing = AttesterSlashingDelegate(attestation_1, attestation_2)

  override fun Deposit(proof: SSZMutableVector<Bytes32>, data: DepositData): Deposit =
      DepositDelegate(proof, data)

  override fun VoluntaryExit(epoch: Epoch, validator_index: ValidatorIndex): VoluntaryExit =
      VoluntaryExitDelegate(epoch, validator_index)

  override fun SignedVoluntaryExit(
      message: VoluntaryExit, signature: BLSSignature
  ): SignedVoluntaryExit = SignedVoluntaryExitDelegate(message, signature)

  override fun SignedBeaconBlockHeader(
      message: BeaconBlockHeader, signature: BLSSignature
  ): SignedBeaconBlockHeader = SignedBeaconBlockHeaderDelegate(message, signature)

  override fun ProposerSlashing(
      signed_header_1: SignedBeaconBlockHeader,
      signed_header_2: SignedBeaconBlockHeader
  ): ProposerSlashing = ProposerSlashingDelegate(signed_header_1, signed_header_2)

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

  override fun ShardTransition(start_slot: Slot, shard_block_lengths: SSZMutableList<uint64>, shard_data_roots: SSZMutableList<Bytes32>, shard_states: SSZMutableList<ShardState>, proposer_signature_aggregate: BLSSignature): ShardTransition {
    TODO("Not yet implemented")
  }

  override fun CustodySlashing(data_index: uint64, malefactor_index: ValidatorIndex, malefactor_secret: BLSSignature, whistleblower_index: ValidatorIndex, shard_transition: ShardTransition, attestation: Attestation, data: SSZByteList): CustodySlashing {
    TODO("Not yet implemented")
  }

  override fun SignedCustodySlashing(message: CustodySlashing, signature: BLSSignature): SignedCustodySlashing {
    TODO("Not yet implemented")
  }

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
  ): BeaconBlockBody = BeaconBlockBodyDelegate(
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
  ): BeaconBlock = BeaconBlockDelegate(slot, proposer_index, parent_root, state_root, body)

  override fun SignedBeaconBlock(
      message: BeaconBlock, signature: BLSSignature
  ): SignedBeaconBlock = SignedBeaconBlockDelegate(message, signature)

  override fun CompactCommittee(pubkeys: SSZMutableList<BLSPubkey>, compact_validators: SSZMutableList<uint64>): CompactCommittee {
    TODO("Not yet implemented")
  }

  override fun AttestationCustodyBitWrapper(attestation_data_root: Root, block_index: uint64, bit: boolean): AttestationCustodyBitWrapper {
    TODO("Not yet implemented")
  }

  override fun LatestMessage(epoch: Epoch, root: Root): LatestMessage = LatestMessageImpl(epoch, root)

  override fun ValidatorIndicesSSZList(items: MutableList<ValidatorIndex>, maxSize: ULong): ValidatorIndicesSSZList {
    return ValidatorIndicesSSZListDelegate(items, maxSize)
  }

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
      exposed_derived_secrets: SSZMutableVector<ValidatorIndicesSSZList>
  ): BeaconState = BeaconStateDelegate(
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
  ): Store = StoreDelegate(
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
}