package tech.pegasys.teku.phase1.integration.datastructures

import com.google.common.primitives.UnsignedLong
import tech.pegasys.teku.phase1.onotole.phase1.Attestation
import tech.pegasys.teku.phase1.onotole.phase1.AttestationCustodyBitWrapper
import tech.pegasys.teku.phase1.onotole.phase1.AttestationData
import tech.pegasys.teku.phase1.onotole.phase1.AttesterSlashing
import tech.pegasys.teku.phase1.onotole.phase1.BLSPubkey
import tech.pegasys.teku.phase1.onotole.phase1.BLSSignature
import tech.pegasys.teku.phase1.onotole.phase1.Checkpoint
import tech.pegasys.teku.phase1.onotole.phase1.CommitteeIndex
import tech.pegasys.teku.phase1.onotole.phase1.CustodyKeyReveal
import tech.pegasys.teku.phase1.onotole.phase1.CustodySlashing
import tech.pegasys.teku.phase1.onotole.phase1.Deposit
import tech.pegasys.teku.phase1.onotole.phase1.DepositData
import tech.pegasys.teku.phase1.onotole.phase1.DepositMessage
import tech.pegasys.teku.phase1.onotole.phase1.EarlyDerivedSecretReveal
import tech.pegasys.teku.phase1.onotole.phase1.Epoch
import tech.pegasys.teku.phase1.onotole.phase1.Gwei
import tech.pegasys.teku.phase1.onotole.phase1.IndexedAttestation
import tech.pegasys.teku.phase1.onotole.phase1.ProposerSlashing
import tech.pegasys.teku.phase1.onotole.phase1.Root
import tech.pegasys.teku.phase1.onotole.phase1.ShardTransition
import tech.pegasys.teku.phase1.onotole.phase1.SignedBeaconBlockHeader
import tech.pegasys.teku.phase1.onotole.phase1.SignedCustodySlashing
import tech.pegasys.teku.phase1.onotole.phase1.SignedVoluntaryExit
import tech.pegasys.teku.phase1.onotole.phase1.Slot
import tech.pegasys.teku.phase1.onotole.phase1.ValidatorIndex
import tech.pegasys.teku.phase1.onotole.phase1.VoluntaryExit
import tech.pegasys.teku.phase1.integration.AttestationDataType
import tech.pegasys.teku.phase1.integration.AttestationType
import tech.pegasys.teku.phase1.integration.BLSPublicKeyType
import tech.pegasys.teku.phase1.integration.BLSSignatureType
import tech.pegasys.teku.phase1.integration.Bytes32Type
import tech.pegasys.teku.phase1.integration.CheckpointType
import tech.pegasys.teku.phase1.integration.CustodySlashingType
import tech.pegasys.teku.phase1.integration.DepositDataType
import tech.pegasys.teku.phase1.integration.IndexedAttestationType
import tech.pegasys.teku.phase1.integration.SSZBitListType
import tech.pegasys.teku.phase1.integration.ShardTransitionType
import tech.pegasys.teku.phase1.integration.SignedBeaconBlockHeaderType
import tech.pegasys.teku.phase1.integration.UInt64Type
import tech.pegasys.teku.phase1.integration.VoluntaryExitType
import tech.pegasys.teku.phase1.integration.ssz.SSZBitListWrapper
import tech.pegasys.teku.phase1.integration.ssz.SSZByteListWrapper
import tech.pegasys.teku.phase1.integration.ssz.SSZListWrapper
import tech.pegasys.teku.phase1.integration.ssz.SSZMutableListWrapper
import tech.pegasys.teku.phase1.integration.ssz.SSZMutableVectorWrapper
import tech.pegasys.teku.phase1.integration.ssz.SSZVectorWrapper
import tech.pegasys.teku.phase1.onotole.ssz.Bytes32
import tech.pegasys.teku.phase1.onotole.ssz.SSZBitList
import tech.pegasys.teku.phase1.onotole.ssz.SSZByteList
import tech.pegasys.teku.phase1.onotole.ssz.SSZList
import tech.pegasys.teku.phase1.onotole.ssz.SSZMutableList
import tech.pegasys.teku.phase1.onotole.ssz.SSZMutableVector
import tech.pegasys.teku.phase1.onotole.ssz.SSZVector
import tech.pegasys.teku.phase1.onotole.ssz.boolean
import tech.pegasys.teku.phase1.onotole.ssz.uint64
import tech.pegasys.teku.datastructures.operations.Deposit as TekuDeposit
import tech.pegasys.teku.datastructures.operations.DepositData as TekuDepositData
import tech.pegasys.teku.datastructures.operations.DepositMessage as TekuDepositMessage
import tech.pegasys.teku.datastructures.operations.ProposerSlashing as TekuProposerSlashing
import tech.pegasys.teku.datastructures.operations.SignedVoluntaryExit as TekuSignedVoluntaryExit
import tech.pegasys.teku.datastructures.operations.VoluntaryExit as TekuVoluntaryExit
import tech.pegasys.teku.datastructures.phase1.operations.AttestationCustodyBitWrapper as TekuAttestationCustodyBitWrapper
import tech.pegasys.teku.datastructures.phase1.operations.AttestationDataPhase1 as TekuAttestationData
import tech.pegasys.teku.datastructures.phase1.operations.AttestationPhase1 as TekuAttestation
import tech.pegasys.teku.datastructures.phase1.operations.AttesterSlashingPhase1 as TekuAttesterSlashing
import tech.pegasys.teku.datastructures.phase1.operations.CustodyKeyReveal as TekuCustodyKeyReveal
import tech.pegasys.teku.datastructures.phase1.operations.CustodySlashing as TekuCustodySlashing
import tech.pegasys.teku.datastructures.phase1.operations.EarlyDerivedSecretReveal as TekuEarlyDerivedSecretReveal
import tech.pegasys.teku.datastructures.phase1.operations.IndexedAttestationPhase1 as TekuIndexedAttestation
import tech.pegasys.teku.datastructures.phase1.operations.SignedCustodySlashing as TekuSignedCustodySlashing
import tech.pegasys.teku.ssz.SSZTypes.Bitlist as TekuBitlist

internal class AttestationDataWrapper(
  override val v: TekuAttestationData
) : Wrapper<TekuAttestationData>, AttestationData {

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
      UInt64Type.unwrap(slot),
      UInt64Type.unwrap(index),
      beacon_block_root,
      CheckpointType.unwrap(source),
      CheckpointType.unwrap(target),
      head_shard_root,
      shard_transition_root
    )
  )

  override val slot: Slot
    get() = UInt64Type.wrap(v.slot)
  override val index: CommitteeIndex
    get() = UInt64Type.wrap(v.index)
  override val beacon_block_root: Root
    get() = v.beacon_block_root
  override val source: Checkpoint
    get() = CheckpointType.wrap(v.source)
  override val target: Checkpoint
    get() = CheckpointType.wrap(v.target)
  override val shard_head_root: Root
    get() = v.shard_head_root
  override val shard_transition_root: Root
    get() = v.shard_transition_root

  override fun hash_tree_root() = v.hash_tree_root()

  override fun equals(other: Any?): Boolean {
    if (other is AttestationDataWrapper) {
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

internal class DepositMessageWrapper(
  override val v: TekuDepositMessage
) : Wrapper<TekuDepositMessage>, DepositMessage {

  constructor(pubkey: BLSPubkey, withdrawal_credentials: Bytes32, amount: Gwei)
      : this(
    TekuDepositMessage(
      BLSPublicKeyType.unwrap(pubkey),
      withdrawal_credentials,
      UInt64Type.unwrap(amount)
    )
  )

  override val pubkey: BLSPubkey
    get() = BLSPublicKeyType.wrap(v.pubkey)
  override val withdrawal_credentials: Bytes32
    get() = v.withdrawal_credentials
  override val amount: Gwei
    get() = UInt64Type.wrap(v.amount)

  override fun hash_tree_root() = v.hash_tree_root()

  override fun equals(other: Any?): Boolean {
    if (other is DepositMessageWrapper) {
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

internal class DepositDataWrapper(
  override val v: TekuDepositData
) : Wrapper<TekuDepositData>, DepositData {

  constructor(
    pubkey: BLSPubkey,
    withdrawal_credentials: Bytes32,
    amount: Gwei,
    signature: BLSSignature
  ) : this(
    TekuDepositData(
      BLSPublicKeyType.unwrap(pubkey),
      withdrawal_credentials,
      UInt64Type.unwrap(amount),
      BLSSignatureType.unwrap(signature)
    )
  )

  override val pubkey: BLSPubkey
    get() = BLSPublicKeyType.wrap(v.pubkey)
  override val withdrawal_credentials: Bytes32
    get() = v.withdrawal_credentials
  override val amount: Gwei
    get() = UInt64Type.wrap(v.amount)
  override val signature: BLSSignature
    get() = BLSSignatureType.wrap(v.signature)

  override fun hash_tree_root() = v.hash_tree_root()

  override fun equals(other: Any?): Boolean {
    if (other is DepositDataWrapper) {
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

internal class AttestationWrapper(
  override val v: TekuAttestation
) : Wrapper<TekuAttestation>, Attestation {

  constructor(
    aggregation_bits: SSZBitList,
    data: AttestationData,
    custody_bits_blocks: SSZMutableList<SSZBitList>,
    signature: BLSSignature
  ) : this(
    TekuAttestation(
      SSZBitListType.unwrap(aggregation_bits),
      AttestationDataType.unwrap(data),
      (custody_bits_blocks as SSZMutableListWrapper<SSZBitList, TekuBitlist>).collection,
      BLSSignatureType.unwrap(signature)
    )
  )

  override val aggregation_bits: SSZBitList
    get() = SSZBitListWrapper(v.aggregation_bits)
  override val data: AttestationData
    get() = AttestationDataType.wrap(v.data)
  override val custody_bits_blocks: SSZList<SSZBitList>
    get() = SSZListWrapper(v.custody_bits_blocks, SSZBitListType)
  override val signature: BLSSignature
    get() = BLSSignatureType.wrap(v.aggregate_signature)

  override fun hash_tree_root() = v.hash_tree_root()

  override fun equals(other: Any?): Boolean {
    if (other is AttestationWrapper) {
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

internal class IndexedAttestationWrapper(
  override val v: TekuIndexedAttestation
) : Wrapper<TekuIndexedAttestation>, IndexedAttestation {

  constructor(committee: SSZMutableList<ValidatorIndex>, attestation: Attestation)
      : this(
    TekuIndexedAttestation(
      (committee as SSZMutableListWrapper<ValidatorIndex, UnsignedLong>).collection,
      AttestationType.unwrap(attestation)
    )
  )

  override val committee: SSZList<ValidatorIndex>
    get() = SSZListWrapper(v.committee, UInt64Type)
  override val attestation: Attestation
    get() = AttestationType.wrap(v.attestation)

  override fun hash_tree_root() = v.hash_tree_root()

  override fun equals(other: Any?): Boolean {
    if (other is IndexedAttestationWrapper) {
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

internal class AttesterSlashingWrapper(
  override val v: TekuAttesterSlashing
) : Wrapper<TekuAttesterSlashing>, AttesterSlashing {

  constructor(attestation_1: IndexedAttestation, attestation_2: IndexedAttestation)
      : this(
    TekuAttesterSlashing(
      IndexedAttestationType.unwrap(attestation_1),
      IndexedAttestationType.unwrap(attestation_2)
    )
  )

  override val attestation_1: IndexedAttestation
    get() = IndexedAttestationType.wrap(v.attestation_1)
  override val attestation_2: IndexedAttestation
    get() = IndexedAttestationType.wrap(v.attestation_2)

  override fun hash_tree_root() = v.hash_tree_root()

  override fun equals(other: Any?): Boolean {
    if (other is AttesterSlashingWrapper) {
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

internal class DepositWrapper(
  override val v: TekuDeposit
) : Wrapper<TekuDeposit>, Deposit {

  constructor(proof: SSZMutableVector<Bytes32>, data: DepositData)
      : this(
    TekuDeposit(
      (proof as SSZMutableVectorWrapper<Bytes32, Bytes32>).collection,
      DepositDataType.unwrap(data)
    )
  )

  override val proof: SSZVector<Bytes32>
    get() = SSZVectorWrapper(v.proof, Bytes32Type)
  override val data: DepositData
    get() = DepositDataType.wrap(v.data)

  override fun hash_tree_root() = v.hash_tree_root()

  override fun equals(other: Any?): Boolean {
    if (other is DepositWrapper) {
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

internal class VoluntaryExitWrapper(
  override val v: TekuVoluntaryExit
) : Wrapper<TekuVoluntaryExit>, VoluntaryExit {

  constructor(epoch: Epoch, validator_index: ValidatorIndex)
      : this(
    TekuVoluntaryExit(
      UInt64Type.unwrap(epoch),
      UInt64Type.unwrap(validator_index)
    )
  )

  override val epoch: Epoch
    get() = UInt64Type.wrap(v.epoch)
  override val validator_index: ValidatorIndex
    get() = UInt64Type.wrap(v.validator_index)

  override fun hash_tree_root() = v.hash_tree_root()

  override fun equals(other: Any?): Boolean {
    if (other is VoluntaryExitWrapper) {
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

internal class SignedVoluntaryExitWrapper(
  override val v: TekuSignedVoluntaryExit
) : Wrapper<TekuSignedVoluntaryExit>, SignedVoluntaryExit {

  constructor(message: VoluntaryExit, signature: BLSSignature)
      : this(
    TekuSignedVoluntaryExit(
      VoluntaryExitType.unwrap(message),
      BLSSignatureType.unwrap(signature)
    )
  )

  override val message: VoluntaryExit
    get() = VoluntaryExitType.wrap(v.message)
  override val signature: BLSSignature
    get() = BLSSignatureType.wrap(v.signature)

  override fun hash_tree_root() = v.hash_tree_root()

  override fun equals(other: Any?): Boolean {
    if (other is SignedVoluntaryExitWrapper) {
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

internal class ProposerSlashingWrapper(
  override val v: TekuProposerSlashing
) : ProposerSlashing, Wrapper<TekuProposerSlashing> {

  constructor(signed_header_1: SignedBeaconBlockHeader, signed_header_2: SignedBeaconBlockHeader)
      : this(
    TekuProposerSlashing(
      SignedBeaconBlockHeaderType.unwrap(signed_header_1),
      SignedBeaconBlockHeaderType.unwrap(signed_header_2)
    )
  )

  override val signed_header_1: SignedBeaconBlockHeader
    get() = SignedBeaconBlockHeaderType.wrap(v.header_1)
  override val signed_header_2: SignedBeaconBlockHeader
    get() = SignedBeaconBlockHeaderType.wrap(v.header_2)

  override fun hash_tree_root() = v.hash_tree_root()

  override fun equals(other: Any?): Boolean {
    if (other is ProposerSlashingWrapper) {
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

internal class CustodyKeyRevealWrapper(override val v: TekuCustodyKeyReveal) :
  Wrapper<TekuCustodyKeyReveal>, CustodyKeyReveal {

  constructor(revealer_index: ValidatorIndex, reveal: BLSSignature)
      : this(
    TekuCustodyKeyReveal(
      UInt64Type.unwrap(revealer_index),
      BLSSignatureType.unwrap(reveal)
    )
  )

  override val revealer_index: ValidatorIndex
    get() = UInt64Type.wrap(v.revealer_index)
  override val reveal: BLSSignature
    get() = BLSSignatureType.wrap(v.reveal)

  override fun hash_tree_root() = v.hash_tree_root()

  override fun equals(other: Any?): Boolean {
    if (other is CustodyKeyRevealWrapper) {
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

internal class EarlyDerivedSecretRevealWrapper(override val v: TekuEarlyDerivedSecretReveal) :
  Wrapper<TekuEarlyDerivedSecretReveal>, EarlyDerivedSecretReveal {

  constructor(
    revealed_index: ValidatorIndex,
    epoch: Epoch,
    reveal: BLSSignature,
    masker_index: ValidatorIndex,
    mask: Bytes32
  ) : this(
    TekuEarlyDerivedSecretReveal(
      UInt64Type.unwrap(revealed_index),
      UInt64Type.unwrap(epoch),
      BLSSignatureType.unwrap(reveal),
      UInt64Type.unwrap(masker_index), mask
    )
  )

  override val revealed_index: ValidatorIndex
    get() = UInt64Type.wrap(v.revealed_index)
  override val epoch: Epoch
    get() = UInt64Type.wrap(v.epoch)
  override val reveal: BLSSignature
    get() = BLSSignatureType.wrap(v.reveal)
  override val masker_index: ValidatorIndex
    get() = UInt64Type.wrap(v.masker_index)
  override val mask: Bytes32
    get() = v.mask

  override fun hash_tree_root() = v.hash_tree_root()

  override fun equals(other: Any?): Boolean {
    if (other is EarlyDerivedSecretRevealWrapper) {
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

internal class CustodySlashingWrapper(override val v: TekuCustodySlashing) :
  Wrapper<TekuCustodySlashing>, CustodySlashing {

  constructor(
    data_index: uint64,
    malefactor_index: ValidatorIndex,
    malefactor_secret: BLSSignature,
    whistleblower_index: ValidatorIndex,
    shard_transition: ShardTransition,
    attestation: Attestation,
    data: SSZByteList
  ) : this(
    TekuCustodySlashing(
      UInt64Type.unwrap(data_index),
      UInt64Type.unwrap(malefactor_index),
      BLSSignatureType.unwrap(malefactor_secret),
      UInt64Type.unwrap(whistleblower_index),
      ShardTransitionType.unwrap(shard_transition),
      AttestationType.unwrap(attestation),
      (data as SSZByteListWrapper).collection
    )
  )

  override val data_index: uint64
    get() = UInt64Type.wrap(v.data_index)
  override val malefactor_index: ValidatorIndex
    get() = UInt64Type.wrap(v.malefactor_index)
  override val malefactor_secret: BLSSignature
    get() = BLSSignatureType.wrap(v.malefactor_secret)
  override val whistleblower_index: ValidatorIndex
    get() = UInt64Type.wrap(v.whistleblower_index)
  override val shard_transition: ShardTransition
    get() = ShardTransitionType.wrap(v.shard_transition)
  override val attestation: Attestation
    get() = AttestationType.wrap(v.attestation)
  override val data: SSZByteList
    get() = SSZByteListWrapper(v.data)

  override fun hash_tree_root() = v.hash_tree_root()

  override fun equals(other: Any?): Boolean {
    if (other is CustodySlashingWrapper) {
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

internal class SignedCustodySlashingWrapper(override val v: TekuSignedCustodySlashing) :
  Wrapper<TekuSignedCustodySlashing>, SignedCustodySlashing {

  constructor(message: CustodySlashing, signature: BLSSignature)
      : this(
    TekuSignedCustodySlashing(
      CustodySlashingType.unwrap(message),
      BLSSignatureType.unwrap(signature)
    )
  )

  override val message: CustodySlashing
    get() = CustodySlashingType.wrap(v.message)
  override val signature: BLSSignature
    get() = BLSSignatureType.wrap(v.signature)

  override fun hash_tree_root() = v.hash_tree_root()

  override fun equals(other: Any?): Boolean {
    if (other is SignedCustodySlashingWrapper) {
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

internal class AttestationCustodyBitWrapperImpl(
  override val attestation_data_root: Root,
  override val block_index: uint64,
  override val bit: boolean
) : AttestationCustodyBitWrapper {

  override fun hash_tree_root(): Bytes32 {
    return TekuAttestationCustodyBitWrapper(
      attestation_data_root, UInt64Type.unwrap(block_index), bit
    ).hash_tree_root()
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is AttestationCustodyBitWrapperImpl) return false

    if (attestation_data_root != other.attestation_data_root) return false
    if (block_index != other.block_index) return false
    if (bit != other.bit) return false

    return true
  }

  override fun hashCode(): Int {
    var result = attestation_data_root.hashCode()
    result = 31 * result + block_index.hashCode()
    result = 31 * result + bit.hashCode()
    return result
  }

  override fun toString(): String {
    return "AttestationCustodyBitWrapperImpl(attestation_data_root=$attestation_data_root, block_index=$block_index, bit=$bit)"
  }
}