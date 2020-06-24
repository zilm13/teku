package tech.pegasys.teku.phase1.integration.datastructures

import tech.pegasys.teku.phase1.integration.ssz.SSZBitvectorImpl
import tech.pegasys.teku.phase1.integration.Bytes96Type
import tech.pegasys.teku.phase1.integration.getBasicValue
import tech.pegasys.teku.phase1.integration.wrapValues
import tech.pegasys.teku.phase1.onotole.phase1.BLSSignature
import tech.pegasys.teku.phase1.onotole.phase1.LIGHT_CLIENT_COMMITTEE_SIZE
import tech.pegasys.teku.phase1.onotole.phase1.Root
import tech.pegasys.teku.phase1.onotole.phase1.Slot
import tech.pegasys.teku.phase1.onotole.phase1.ValidatorIndex
import tech.pegasys.teku.phase1.onotole.ssz.SSZBitvector
import tech.pegasys.teku.phase1.onotole.ssz.uint64
import tech.pegasys.teku.ssz.backing.VectorViewRead
import tech.pegasys.teku.ssz.backing.tree.TreeNode
import tech.pegasys.teku.ssz.backing.type.BasicViewTypes
import tech.pegasys.teku.ssz.backing.type.ContainerViewType
import tech.pegasys.teku.ssz.backing.type.VectorViewType
import tech.pegasys.teku.ssz.backing.view.AbstractImmutableContainer
import tech.pegasys.teku.ssz.backing.view.BasicViews.BitView

data class Eth1Block(
  val timestamp: uint64 = 0uL,
  val deposit_root: Root = Root(),
  val deposit_count: uint64 = 0uL
)

class AggregateAndProof : AbstractImmutableContainer {
  val aggregator_index: ValidatorIndex
    get() = getBasicValue(get(0))
  val aggregate: Attestation
    get() = getAny(1)
  val selection_proof: BLSSignature
    get() = getBasicValue(get(2))

  constructor(
    aggregator_index: ValidatorIndex,
    aggregate: Attestation,
    selection_proof: BLSSignature
  ) : super(TYPE, *wrapValues(
    aggregator_index,
    aggregate,
    selection_proof
  )
  )

  constructor() : super(TYPE)

  constructor(
    type: ContainerViewType<out AbstractImmutableContainer>?,
    backingNode: TreeNode?
  ) : super(type, backingNode)

  companion object {
    val TYPE = ContainerViewType<AggregateAndProof>(
      listOf(BasicViewTypes.UINT64_TYPE, Attestation.TYPE,
        Bytes96Type
      ),
      ::AggregateAndProof
    )
  }
}

class LightClientVoteData : AbstractImmutableContainer {
  val slot: Slot
    get() = getBasicValue(get(0))
  val beacon_block_root: Root
    get() = getBasicValue(get(1))

  constructor() : super(TYPE)
  constructor(
    type: ContainerViewType<out AbstractImmutableContainer>?,
    backingNode: TreeNode?
  ) : super(type, backingNode)

  constructor(
    slot: Slot,
    beacon_block_root: Root
  ) : super(TYPE, *wrapValues(
    slot,
    beacon_block_root
  )
  )

  companion object {
    val TYPE = ContainerViewType<LightClientVoteData>(
      listOf(BasicViewTypes.UINT64_TYPE, BasicViewTypes.BYTES32_TYPE),
      ::LightClientVoteData
    )
  }
}

class LightClientVote : AbstractImmutableContainer {
  val data: LightClientVoteData
    get() = getBasicValue(get(0))
  val aggregation_bits: SSZBitvector
    get() = SSZBitvectorImpl(getAny<VectorViewRead<BitView>>(1))
  val signature: BLSSignature
    get() = getBasicValue(get(2))

  constructor() : super(TYPE)
  constructor(
    type: ContainerViewType<out AbstractImmutableContainer>?,
    backingNode: TreeNode?
  ) : super(type, backingNode)

  constructor(
    data: LightClientVoteData,
    aggregation_bits: SSZBitvector,
    signature: BLSSignature
  ) : super(TYPE, *wrapValues(
    data,
    aggregation_bits,
    signature
  )
  )

  companion object {
    val TYPE = ContainerViewType<LightClientVote>(
      listOf(
        LightClientVoteData.TYPE,
        VectorViewType<BitView>(BasicViewTypes.BIT_TYPE, LIGHT_CLIENT_COMMITTEE_SIZE.toLong()),
        Bytes96Type
      ),
      ::LightClientVote
    )
  }
}

class LightAggregateAndProof : AbstractImmutableContainer {
  val aggregator_index: ValidatorIndex
    get() = getBasicValue(get(0))
  val aggregate: LightClientVote
    get() = getAny(1)
  val selection_proof: BLSSignature
    get() = getBasicValue(get(2))

  constructor() : super(TYPE)
  constructor(
    type: ContainerViewType<out AbstractImmutableContainer>?,
    backingNode: TreeNode?
  ) : super(type, backingNode)

  constructor(
    aggregator_index: ValidatorIndex,
    aggregate: LightClientVote,
    selection_proof: BLSSignature
  ) : super(TYPE, *wrapValues(
    aggregator_index,
    aggregate,
    selection_proof
  )
  )

  companion object {
    val TYPE = ContainerViewType<LightAggregateAndProof>(
      listOf(BasicViewTypes.UINT64_TYPE, LightClientVote.TYPE,
        Bytes96Type
      ),
      ::LightAggregateAndProof
    )
  }
}

class SignedLightAggregateAndProof : AbstractImmutableContainer {
  val message: LightAggregateAndProof
    get() = getBasicValue(get(0))
  val signature: BLSSignature
    get() = getBasicValue(get(1))

  constructor() : super(TYPE)
  constructor(
    type: ContainerViewType<out AbstractImmutableContainer>?,
    backingNode: TreeNode?
  ) : super(type, backingNode)

  constructor(
    message: LightAggregateAndProof,
    signature: BLSSignature
  ) : super(TYPE, *wrapValues(
    message,
    signature
  )
  )

  companion object {
    val TYPE = ContainerViewType<SignedLightAggregateAndProof>(
      listOf(LightAggregateAndProof.TYPE,
        Bytes96Type
      ),
      ::SignedLightAggregateAndProof
    )
  }
}
