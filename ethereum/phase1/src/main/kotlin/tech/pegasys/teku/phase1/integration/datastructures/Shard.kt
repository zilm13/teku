package tech.pegasys.teku.phase1.integration.datastructures

import tech.pegasys.teku.phase1.integration.BLSSignatureType
import tech.pegasys.teku.phase1.integration.Bytes32Type
import tech.pegasys.teku.phase1.integration.SSZByteListType
import tech.pegasys.teku.phase1.integration.SSZListType
import tech.pegasys.teku.phase1.integration.SSZMutableListType
import tech.pegasys.teku.phase1.integration.ShardBlockType
import tech.pegasys.teku.phase1.integration.ShardStateType
import tech.pegasys.teku.phase1.integration.UInt64Type
import tech.pegasys.teku.phase1.onotole.phase1.BLSSignature
import tech.pegasys.teku.phase1.onotole.phase1.Gwei
import tech.pegasys.teku.phase1.onotole.phase1.Root
import tech.pegasys.teku.phase1.onotole.phase1.ShardBlock
import tech.pegasys.teku.phase1.onotole.phase1.ShardBlockHeader
import tech.pegasys.teku.phase1.onotole.phase1.ShardState
import tech.pegasys.teku.phase1.onotole.phase1.ShardTransition
import tech.pegasys.teku.phase1.onotole.phase1.SignedShardBlock
import tech.pegasys.teku.phase1.onotole.phase1.Slot
import tech.pegasys.teku.phase1.onotole.phase1.ValidatorIndex
import tech.pegasys.teku.phase1.onotole.ssz.Bytes32
import tech.pegasys.teku.phase1.onotole.ssz.SSZByteList
import tech.pegasys.teku.phase1.onotole.ssz.SSZList
import tech.pegasys.teku.phase1.onotole.ssz.SSZMutableList
import tech.pegasys.teku.phase1.onotole.ssz.uint64
import tech.pegasys.teku.datastructures.phase1.shard.ShardBlock as TekuShardBlock
import tech.pegasys.teku.datastructures.phase1.shard.ShardBlockHeader as TekuShardBlockHeader
import tech.pegasys.teku.datastructures.phase1.shard.ShardState as TekuShardState
import tech.pegasys.teku.datastructures.phase1.shard.ShardTransition as TekuShardTransition
import tech.pegasys.teku.datastructures.phase1.shard.SignedShardBlock as TekuSignedShardBlock

internal class ShardTransitionWrapper(override val v: TekuShardTransition) :
  Wrapper<TekuShardTransition>, ShardTransition {

  constructor(
    start_slot: Slot,
    shard_block_lengths: SSZMutableList<uint64>,
    shard_data_roots: SSZMutableList<Bytes32>,
    shard_states: SSZMutableList<ShardState>,
    proposer_signature_aggregate: BLSSignature
  ) : this(
    TekuShardTransition(
      UInt64Type.unwrap(start_slot),
      SSZMutableListType(UInt64Type).unwrap(shard_block_lengths),
      SSZMutableListType(Bytes32Type).unwrap(shard_data_roots),
      SSZMutableListType(ShardStateType).unwrap(shard_states),
      BLSSignatureType.unwrap(proposer_signature_aggregate)
    )
  )

  override val start_slot: Slot
    get() = UInt64Type.wrap(v.start_slot)
  override val shard_block_lengths: SSZList<uint64>
    get() = SSZListType(UInt64Type).wrap(v.shard_block_lengths)
  override val shard_data_roots: SSZList<Bytes32>
    get() = SSZListType(Bytes32Type).wrap(v.shard_data_roots)
  override val shard_states: SSZList<ShardState>
    get() = SSZListType(ShardStateType).wrap(v.shard_states)
  override val proposer_signature_aggregate: BLSSignature
    get() = BLSSignatureType.wrap(v.proposer_signature_aggregate)

  override fun hash_tree_root() = v.hash_tree_root()

  override fun equals(other: Any?): Boolean {
    if (other is ShardTransitionWrapper) {
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

internal class ShardStateWrapper(
  override var v: TekuShardState,
  onUpdate: Callback<ShardState>? = null
) : Wrapper<TekuShardState>, ShardState, Mutable<ShardState>(onUpdate) {

  constructor(
    slot: Slot,
    gasprice: Gwei,
    transition_digest: Bytes32,
    latest_block_root: Root
  ) : this(
    TekuShardState(
      UInt64Type.unwrap(slot),
      UInt64Type.unwrap(gasprice),
      transition_digest,
      latest_block_root
    )
  )

  override var slot: Slot
    get() = UInt64Type.wrap(v.slot)
    set(value) {
      v = TekuShardState(
        UInt64Type.unwrap(value),
        v.gasprice,
        v.transition_digest,
        v.latest_block_root
      )
      onUpdate(this)
    }
  override var gasprice: Gwei
    get() = UInt64Type.wrap(v.gasprice)
    set(value) {
      v = TekuShardState(v.slot, UInt64Type.unwrap(value), v.transition_digest, v.latest_block_root)
      onUpdate(this)
    }
  override var transition_digest: Bytes32
    get() = v.transition_digest
    set(value) {
      v = TekuShardState(v.slot, v.gasprice, value, v.latest_block_root)
      onUpdate(this)
    }
  override var latest_block_root: Root
    get() = Bytes32Type.wrap(v.latest_block_root)
    set(value) {
      v = TekuShardState(v.slot, v.gasprice, v.transition_digest, value)
      onUpdate(this)
    }

  override fun copy(
    slot: Slot,
    gasprice: Gwei,
    transition_digest: Bytes32,
    latest_block_root: Root
  ): ShardState = ShardStateWrapper(slot, gasprice, transition_digest, latest_block_root)

  override fun hash_tree_root() = v.hash_tree_root()

  override fun equals(other: Any?): Boolean {
    if (other is ShardStateWrapper) {
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

internal class ShardBlockHeaderWrapper(override val v: TekuShardBlockHeader) :
  Wrapper<TekuShardBlockHeader>, ShardBlockHeader {

  constructor(
    shard_parent_root: Root,
    beacon_parent_root: Root,
    slot: Slot,
    proposer_index: ValidatorIndex,
    body_root: Root
  ) : this(
    TekuShardBlockHeader(
      shard_parent_root,
      beacon_parent_root,
      UInt64Type.unwrap(slot),
      UInt64Type.unwrap(proposer_index),
      body_root
    )
  )

  override val shard_parent_root: Root
    get() = Bytes32Type.wrap(v.shard_parent_root)
  override val beacon_parent_root: Root
    get() = Bytes32Type.wrap(v.beacon_parent_root)
  override val slot: Slot
    get() = UInt64Type.wrap(v.slot)
  override val proposer_index: ValidatorIndex
    get() = UInt64Type.wrap(v.proposer_index)
  override val body_root: Root
    get() = Bytes32Type.wrap(v.body_root)

  override fun hash_tree_root() = v.hash_tree_root()

  override fun equals(other: Any?): Boolean {
    if (other is ShardBlockHeaderWrapper) {
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

internal class ShardBlockWrapper(override val v: TekuShardBlock) : Wrapper<TekuShardBlock>,
  ShardBlock {

  constructor(
    shard_parent_root: Root,
    beacon_parent_root: Root,
    slot: Slot,
    proposer_index: ValidatorIndex,
    body: SSZByteList
  ) : this(
    TekuShardBlock(
      shard_parent_root,
      beacon_parent_root,
      UInt64Type.unwrap(slot),
      UInt64Type.unwrap(proposer_index),
      SSZByteListType.unwrap(body)
    )
  )

  override val shard_parent_root: Root
    get() = Bytes32Type.wrap(v.shard_parent_root)
  override val beacon_parent_root: Root
    get() = Bytes32Type.wrap(v.beacon_parent_root)
  override val slot: Slot
    get() = UInt64Type.wrap(v.slot)
  override val proposer_index: ValidatorIndex
    get() = UInt64Type.wrap(v.proposer_index)
  override val body: SSZByteList
    get() = SSZByteListType.wrap(v.body)

  override fun hash_tree_root() = v.hash_tree_root()

  override fun equals(other: Any?): Boolean {
    if (other is ShardBlockWrapper) {
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

internal class SignedShardBlockWrapper(override val v: TekuSignedShardBlock) :
  Wrapper<TekuSignedShardBlock>, SignedShardBlock {

  constructor(message: ShardBlock, signature: BLSSignature) : this(
    TekuSignedShardBlock(
      ShardBlockType.unwrap(message),
      BLSSignatureType.unwrap(signature)
    )
  )

  override val message: ShardBlock
    get() = ShardBlockType.wrap(v.message)
  override val signature: BLSSignature
    get() = BLSSignatureType.wrap(v.signature)

  override fun hash_tree_root() = v.hash_tree_root()

  override fun equals(other: Any?): Boolean {
    if (other is SignedShardBlockWrapper) {
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
