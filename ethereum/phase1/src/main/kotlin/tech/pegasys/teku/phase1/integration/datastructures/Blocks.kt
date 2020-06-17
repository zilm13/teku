package tech.pegasys.teku.phase1.integration.datastructures

import tech.pegasys.teku.phase1.integration.AttestationType
import tech.pegasys.teku.phase1.integration.AttesterSlashingType
import tech.pegasys.teku.phase1.integration.BLSSignatureType
import tech.pegasys.teku.phase1.integration.BeaconBlockBodyType
import tech.pegasys.teku.phase1.integration.BeaconBlockHeaderType
import tech.pegasys.teku.phase1.integration.BeaconBlockType
import tech.pegasys.teku.phase1.integration.CustodyKeyRevealType
import tech.pegasys.teku.phase1.integration.DepositType
import tech.pegasys.teku.phase1.integration.EarlyDerivedSecretRevealType
import tech.pegasys.teku.phase1.integration.Eth1DataType
import tech.pegasys.teku.phase1.integration.ProposerSlashingType
import tech.pegasys.teku.phase1.integration.SSZBitVectorType
import tech.pegasys.teku.phase1.integration.SSZListType
import tech.pegasys.teku.phase1.integration.SSZMutableListType
import tech.pegasys.teku.phase1.integration.SSZMutableVectorType
import tech.pegasys.teku.phase1.integration.SSZVectorType
import tech.pegasys.teku.phase1.integration.ShardTransitionType
import tech.pegasys.teku.phase1.integration.SignedCustodySlashingType
import tech.pegasys.teku.phase1.integration.SignedVoluntaryExitType
import tech.pegasys.teku.phase1.integration.UInt64Type
import tech.pegasys.teku.phase1.onotole.phase1.Attestation
import tech.pegasys.teku.phase1.onotole.phase1.AttesterSlashing
import tech.pegasys.teku.phase1.onotole.phase1.BLSSignature
import tech.pegasys.teku.phase1.onotole.phase1.BeaconBlock
import tech.pegasys.teku.phase1.onotole.phase1.BeaconBlockBody
import tech.pegasys.teku.phase1.onotole.phase1.BeaconBlockHeader
import tech.pegasys.teku.phase1.onotole.phase1.CustodyKeyReveal
import tech.pegasys.teku.phase1.onotole.phase1.Deposit
import tech.pegasys.teku.phase1.onotole.phase1.EarlyDerivedSecretReveal
import tech.pegasys.teku.phase1.onotole.phase1.Eth1Data
import tech.pegasys.teku.phase1.onotole.phase1.ProposerSlashing
import tech.pegasys.teku.phase1.onotole.phase1.Root
import tech.pegasys.teku.phase1.onotole.phase1.ShardTransition
import tech.pegasys.teku.phase1.onotole.phase1.SignedBeaconBlock
import tech.pegasys.teku.phase1.onotole.phase1.SignedBeaconBlockHeader
import tech.pegasys.teku.phase1.onotole.phase1.SignedCustodySlashing
import tech.pegasys.teku.phase1.onotole.phase1.SignedVoluntaryExit
import tech.pegasys.teku.phase1.onotole.phase1.Slot
import tech.pegasys.teku.phase1.onotole.phase1.ValidatorIndex
import tech.pegasys.teku.phase1.onotole.ssz.Bytes32
import tech.pegasys.teku.phase1.onotole.ssz.SSZBitVector
import tech.pegasys.teku.phase1.onotole.ssz.SSZList
import tech.pegasys.teku.phase1.onotole.ssz.SSZMutableList
import tech.pegasys.teku.phase1.onotole.ssz.SSZMutableVector
import tech.pegasys.teku.phase1.onotole.ssz.SSZVector
import tech.pegasys.teku.phase1.onotole.ssz.uint64
import tech.pegasys.teku.datastructures.blocks.BeaconBlockHeader as TekuBeaconBlockHeader
import tech.pegasys.teku.datastructures.blocks.Eth1Data as TekuEth1Data
import tech.pegasys.teku.datastructures.blocks.SignedBeaconBlockHeader as TekuSignedBeaconBlockHeader
import tech.pegasys.teku.datastructures.phase1.blocks.BeaconBlockBodyPhase1 as TekuBeaconBlockBody
import tech.pegasys.teku.datastructures.phase1.blocks.BeaconBlockPhase1 as TekuBeaconBlock
import tech.pegasys.teku.datastructures.phase1.blocks.SignedBeaconBlockPhase1 as TekuSignedBeaconBlock

internal class Eth1DataWrapper(
  override var v: TekuEth1Data,
  onUpdate: Callback<Eth1Data>? = null
) : Eth1Data, Mutable<Eth1Data>(onUpdate), Wrapper<TekuEth1Data> {

  constructor(
    deposit_root: Root,
    deposit_count: uint64,
    block_hash: Bytes32
  ) : this(
    TekuEth1Data(
      deposit_root,
      UInt64Type.unwrap(deposit_count),
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
    get() = UInt64Type.wrap(v.deposit_count)
  override val block_hash: Bytes32
    get() = v.block_hash

  override fun hash_tree_root() = v.hash_tree_root()

  override fun equals(other: Any?): Boolean {
    if (other is Eth1DataWrapper) {
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

internal class BeaconBlockHeaderWrapper(
  override var v: TekuBeaconBlockHeader,
  onUpdate: Callback<BeaconBlockHeader>? = null
) : BeaconBlockHeader, Wrapper<TekuBeaconBlockHeader>, Mutable<BeaconBlockHeader>(onUpdate) {

  constructor(
    slot: Slot,
    proposer_index: ValidatorIndex,
    parent_root: Root,
    state_root: Root,
    body_root: Root
  ) : this(
    TekuBeaconBlockHeader(
      UInt64Type.unwrap(slot),
      UInt64Type.unwrap(proposer_index),
      parent_root,
      state_root,
      body_root
    )
  )

  override val slot: Slot
    get() = UInt64Type.wrap(v.slot)
  override val proposer_index: ValidatorIndex
    get() = UInt64Type.wrap(v.proposer_index)
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
  ) = BeaconBlockHeaderWrapper(slot, proposer_index, parent_root, state_root, body_root)

  override fun hash_tree_root() = v.hash_tree_root()

  override fun equals(other: Any?): Boolean {
    if (other is BeaconBlockHeaderWrapper) {
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

internal class SignedBeaconBlockHeaderWrapper(
  override val v: TekuSignedBeaconBlockHeader
) : Wrapper<TekuSignedBeaconBlockHeader>, SignedBeaconBlockHeader {

  constructor(message: BeaconBlockHeader, signature: BLSSignature)
      : this(
    TekuSignedBeaconBlockHeader(
      BeaconBlockHeaderType.unwrap(message),
      BLSSignatureType.unwrap(signature)
    )
  )

  override val message: BeaconBlockHeader
    get() = BeaconBlockHeaderType.wrap(v.message)
  override val signature: BLSSignature
    get() = BLSSignatureType.wrap(v.signature)

  override fun hash_tree_root() = v.hash_tree_root()

  override fun equals(other: Any?): Boolean {
    if (other is SignedBeaconBlockHeaderWrapper) {
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

internal class BeaconBlockBodyWrapper(
  override val v: TekuBeaconBlockBody
) : Wrapper<TekuBeaconBlockBody>, BeaconBlockBody {

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
      BLSSignatureType.unwrap(randao_reveal),
      Eth1DataType.unwrap(eth1_data),
      graffiti,
      SSZMutableListType(ProposerSlashingType).unwrap(proposer_slashings),
      SSZMutableListType(AttesterSlashingType).unwrap(attester_slashings),
      SSZMutableListType(AttestationType).unwrap(attestations),
      SSZMutableListType(DepositType).unwrap(deposits),
      SSZMutableListType(SignedVoluntaryExitType).unwrap(voluntary_exits),
      SSZMutableListType(SignedCustodySlashingType).unwrap(custody_slashings),
      SSZMutableListType(CustodyKeyRevealType).unwrap(custody_key_reveals),
      SSZMutableListType(EarlyDerivedSecretRevealType).unwrap(early_derived_secret_reveals),
      SSZMutableVectorType(ShardTransitionType).unwrap(shard_transitions),
      SSZBitVectorType.unwrap(light_client_signature_bitfield),
      BLSSignatureType.unwrap(light_client_signature)
    )
  )

  override val randao_reveal: BLSSignature
    get() = BLSSignatureType.wrap(v.randao_reveal)
  override val eth1_data: Eth1Data
    get() = Eth1DataType.wrap(v.eth1_data)
  override val graffiti: Bytes32
    get() = v.graffiti
  override val proposer_slashings: SSZList<ProposerSlashing>
    get() = SSZListType(ProposerSlashingType).wrap(v.proposer_slashings)
  override val attester_slashings: SSZList<AttesterSlashing>
    get() = SSZListType(AttesterSlashingType).wrap(v.attester_slashings)
  override val attestations: SSZList<Attestation>
    get() = SSZListType(AttestationType).wrap(v.attestations)
  override val deposits: SSZList<Deposit>
    get() = SSZListType(DepositType).wrap(v.deposits)
  override val voluntary_exits: SSZList<SignedVoluntaryExit>
    get() = SSZListType(SignedVoluntaryExitType).wrap(v.voluntary_exits)
  override val custody_slashings: SSZList<SignedCustodySlashing>
    get() = SSZListType(SignedCustodySlashingType).wrap(v.custody_slashings)
  override val custody_key_reveals: SSZList<CustodyKeyReveal>
    get() = SSZListType(CustodyKeyRevealType).wrap(v.custody_key_reveals)
  override val early_derived_secret_reveals: SSZList<EarlyDerivedSecretReveal>
    get() = SSZListType(EarlyDerivedSecretRevealType).wrap(v.early_derived_secret_reveals)
  override val shard_transitions: SSZVector<ShardTransition>
    get() = SSZVectorType(ShardTransitionType).wrap(v.shard_transitions)
  override val light_client_signature_bitfield: SSZBitVector
    get() = SSZBitVectorType.wrap(v.light_client_signature_bitfield)
  override val light_client_signature: BLSSignature
    get() = BLSSignatureType.wrap(v.light_client_signature)

  override fun hash_tree_root() = v.hash_tree_root()

  override fun equals(other: Any?): Boolean {
    if (other is BeaconBlockBodyWrapper) {
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

internal class BeaconBlockWrapper(
  override val v: TekuBeaconBlock
) : Wrapper<TekuBeaconBlock>, BeaconBlock {

  constructor(
    slot: Slot,
    proposer_index: ValidatorIndex,
    parent_root: Root,
    state_root: Root,
    body: BeaconBlockBody
  ) : this(
    TekuBeaconBlock(
      UInt64Type.unwrap(slot),
      UInt64Type.unwrap(proposer_index),
      parent_root,
      state_root,
      BeaconBlockBodyType.unwrap(body)
    )
  )

  override val slot: Slot
    get() = UInt64Type.wrap(v.slot)
  override val proposer_index: ValidatorIndex
    get() = UInt64Type.wrap(v.proposer_index)
  override val parent_root: Root
    get() = v.parent_root
  override val state_root: Root
    get() = v.state_root
  override val body: BeaconBlockBody
    get() = BeaconBlockBodyType.wrap(v.body)

  override fun hash_tree_root() = v.hash_tree_root()

  override fun equals(other: Any?): Boolean {
    if (other is BeaconBlockWrapper) {
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

internal class SignedBeaconBlockWrapper(
  override val v: TekuSignedBeaconBlock
) : Wrapper<TekuSignedBeaconBlock>, SignedBeaconBlock {

  constructor(message: BeaconBlock, signature: BLSSignature)
      : this(
    TekuSignedBeaconBlock(
      BeaconBlockType.unwrap(message),
      BLSSignatureType.unwrap(signature)
    )
  )

  override val message: BeaconBlock
    get() = BeaconBlockType.wrap(v.message)
  override val signature: BLSSignature
    get() = BLSSignatureType.wrap(v.signature)

  override fun hash_tree_root() = v.hash_tree_root()

  override fun equals(other: Any?): Boolean {
    if (other is SignedBeaconBlockWrapper) {
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
