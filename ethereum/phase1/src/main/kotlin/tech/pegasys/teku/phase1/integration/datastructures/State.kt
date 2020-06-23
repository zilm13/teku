package tech.pegasys.teku.phase1.integration.datastructures

import org.apache.tuweni.bytes.Bytes48
import tech.pegasys.teku.phase1.integration.ssz.SSZAbstractCollection
import tech.pegasys.teku.phase1.integration.ssz.SSZBitlistImpl
import tech.pegasys.teku.phase1.integration.ssz.SSZListImpl
import tech.pegasys.teku.phase1.integration.ssz.SSZMutableBitvectorImpl
import tech.pegasys.teku.phase1.integration.ssz.SSZMutableListImpl
import tech.pegasys.teku.phase1.integration.ssz.SSZMutableVectorImpl
import tech.pegasys.teku.phase1.integration.ssz.SSZVectorImpl
import tech.pegasys.teku.phase1.integration.Bytes48Type
import tech.pegasys.teku.phase1.integration.getBasicValue
import tech.pegasys.teku.phase1.integration.toUInt64
import tech.pegasys.teku.phase1.integration.toUnsignedLong
import tech.pegasys.teku.phase1.integration.wrapBasicValue
import tech.pegasys.teku.phase1.integration.wrapValues
import tech.pegasys.teku.phase1.onotole.phase1.BLSPubkey
import tech.pegasys.teku.phase1.onotole.phase1.Domain
import tech.pegasys.teku.phase1.onotole.phase1.EARLY_DERIVED_SECRET_PENALTY_MAX_FUTURE_EPOCHS
import tech.pegasys.teku.phase1.onotole.phase1.EPOCHS_PER_ETH1_VOTING_PERIOD
import tech.pegasys.teku.phase1.onotole.phase1.EPOCHS_PER_HISTORICAL_VECTOR
import tech.pegasys.teku.phase1.onotole.phase1.EPOCHS_PER_SLASHINGS_VECTOR
import tech.pegasys.teku.phase1.onotole.phase1.Epoch
import tech.pegasys.teku.phase1.onotole.phase1.Gwei
import tech.pegasys.teku.phase1.onotole.phase1.HISTORICAL_ROOTS_LIMIT
import tech.pegasys.teku.phase1.onotole.phase1.JUSTIFICATION_BITS_LENGTH
import tech.pegasys.teku.phase1.onotole.phase1.MAX_ATTESTATIONS
import tech.pegasys.teku.phase1.onotole.phase1.MAX_CUSTODY_CHUNK_CHALLENGE_RECORDS
import tech.pegasys.teku.phase1.onotole.phase1.MAX_EARLY_DERIVED_SECRET_REVEALS
import tech.pegasys.teku.phase1.onotole.phase1.MAX_SHARDS
import tech.pegasys.teku.phase1.onotole.phase1.MAX_VALIDATORS_PER_COMMITTEE
import tech.pegasys.teku.phase1.onotole.phase1.OnlineEpochs
import tech.pegasys.teku.phase1.onotole.phase1.Root
import tech.pegasys.teku.phase1.onotole.phase1.SLOTS_PER_EPOCH
import tech.pegasys.teku.phase1.onotole.phase1.SLOTS_PER_HISTORICAL_ROOT
import tech.pegasys.teku.phase1.onotole.phase1.Shard
import tech.pegasys.teku.phase1.onotole.phase1.Slot
import tech.pegasys.teku.phase1.onotole.phase1.VALIDATOR_REGISTRY_LIMIT
import tech.pegasys.teku.phase1.onotole.phase1.ValidatorIndex
import tech.pegasys.teku.phase1.onotole.phase1.Version
import tech.pegasys.teku.phase1.onotole.ssz.Bytes32
import tech.pegasys.teku.phase1.onotole.ssz.Bytes4
import tech.pegasys.teku.phase1.onotole.ssz.SSZBitlist
import tech.pegasys.teku.phase1.onotole.ssz.SSZBitvector
import tech.pegasys.teku.phase1.onotole.ssz.SSZList
import tech.pegasys.teku.phase1.onotole.ssz.SSZMutableBitvector
import tech.pegasys.teku.phase1.onotole.ssz.SSZMutableList
import tech.pegasys.teku.phase1.onotole.ssz.SSZMutableVector
import tech.pegasys.teku.phase1.onotole.ssz.SSZVector
import tech.pegasys.teku.phase1.onotole.ssz.boolean
import tech.pegasys.teku.phase1.onotole.ssz.uint64
import tech.pegasys.teku.ssz.backing.ContainerViewRead
import tech.pegasys.teku.ssz.backing.ListViewRead
import tech.pegasys.teku.ssz.backing.ListViewWrite
import tech.pegasys.teku.ssz.backing.VectorViewRead
import tech.pegasys.teku.ssz.backing.VectorViewWrite
import tech.pegasys.teku.ssz.backing.tree.TreeNode
import tech.pegasys.teku.ssz.backing.type.BasicViewTypes
import tech.pegasys.teku.ssz.backing.type.ContainerViewType
import tech.pegasys.teku.ssz.backing.type.ListViewType
import tech.pegasys.teku.ssz.backing.type.VectorViewType
import tech.pegasys.teku.ssz.backing.view.AbstractImmutableContainer
import tech.pegasys.teku.ssz.backing.view.AbstractMutableContainer
import tech.pegasys.teku.ssz.backing.view.BasicViews.BitView
import tech.pegasys.teku.ssz.backing.view.BasicViews.ByteView
import tech.pegasys.teku.ssz.backing.view.BasicViews.Bytes32View
import tech.pegasys.teku.ssz.backing.view.BasicViews.UInt64View
import tech.pegasys.teku.ssz.backing.view.ViewUtils
import tech.pegasys.teku.util.config.Constants

class Fork : AbstractImmutableContainer {
  val previous_version: Version
    get() = getBasicValue(get(0))
  val current_version: Version
    get() = getBasicValue(get(1))
  val epoch: Epoch
    get() = getBasicValue(get(2))

  constructor(previous_version: Bytes4, current_version: Bytes4, epoch: Epoch) : super(
    TYPE,
    *wrapValues(
      previous_version,
      current_version,
      epoch
    )
  )

  constructor(
    type: ContainerViewType<out AbstractImmutableContainer>?,
    backingNode: TreeNode?
  ) : super(type, backingNode)

  constructor() : super(TYPE)

  companion object {
    val TYPE = ContainerViewType<Fork>(
      listOf(BasicViewTypes.BYTES4_TYPE, BasicViewTypes.BYTES4_TYPE, BasicViewTypes.UINT64_TYPE),
      ::Fork
    )
  }
}

class ForkData : AbstractImmutableContainer {
  val current_version: Version
    get() = getBasicValue(get(0))
  val genesis_validators_root: Root
    get() = getBasicValue(get(1))

  constructor(current_version: Version, genesis_validators_root: Root) : super(
    TYPE,
    *wrapValues(
      current_version,
      genesis_validators_root
    )
  )

  constructor(
    type: ContainerViewType<out AbstractImmutableContainer>?,
    backingNode: TreeNode?
  ) : super(type, backingNode)

  constructor() : super(TYPE)

  companion object {
    val TYPE = ContainerViewType<ForkData>(
      listOf(BasicViewTypes.BYTES4_TYPE, BasicViewTypes.BYTES32_TYPE),
      ::ForkData
    )
  }
}

class Checkpoint : AbstractImmutableContainer {
  val epoch: Epoch
    get() = getBasicValue(get(0))
  val root: Root
    get() = getBasicValue(get(1))

  constructor(epoch: Epoch, root: Root) : super(TYPE, *wrapValues(
    epoch,
    root
  )
  )

  constructor(
    type: ContainerViewType<out AbstractImmutableContainer>?,
    backingNode: TreeNode?
  ) : super(type, backingNode)

  constructor() : super(TYPE)

  companion object {
    val TYPE = ContainerViewType(
      listOf(BasicViewTypes.UINT64_TYPE, BasicViewTypes.BYTES32_TYPE), ::Checkpoint
    )
  }
}

class Validator : AbstractMutableContainer {
  val pubkey: BLSPubkey
    get() = BLSPubkey(Bytes48.wrap(ViewUtils.getAllBytes(getAny(0))))
  val withdrawal_credentials: Bytes32
    get() = getBasicValue(get(1))
  var effective_balance: Gwei
    get() = getBasicValue(get(2))
    set(value) {
      set(1, wrapBasicValue(value))
    }
  var slashed: boolean
    get() = getBasicValue(get(3))
    set(value) {
      set(2, wrapBasicValue(value))
    }
  var activation_eligibility_epoch: Epoch
    get() = getBasicValue(get(4))
    set(value) {
      set(3, wrapBasicValue(value))
    }
  var activation_epoch: Epoch
    get() = getBasicValue(get(5))
    set(value) {
      set(4, wrapBasicValue(value))
    }
  var exit_epoch: Epoch
    get() = getBasicValue(get(6))
    set(value) {
      set(5, wrapBasicValue(value))
    }
  var withdrawable_epoch: Epoch
    get() = getBasicValue(get(7))
    set(value) {
      set(6, wrapBasicValue(value))
    }
  var next_custody_secret_to_reveal: uint64
    get() = getBasicValue(get(8))
    set(value) {
      set(7, wrapBasicValue(value))
    }
  var all_custody_secrets_revealed_epoch: Epoch
    get() = getBasicValue(get(9))
    set(value) {
      set(8, wrapBasicValue(value))
    }

  constructor(
    pubkey: BLSPubkey,
    withdrawal_credentials: Bytes32,
    effective_balance: Gwei,
    slashed: boolean = false,
    activation_eligibility_epoch: Epoch,
    activation_epoch: Epoch,
    exit_epoch: Epoch,
    withdrawable_epoch: Epoch,
    next_custody_secret_to_reveal: uint64,
    all_custody_secrets_revealed_epoch: Epoch
  ) : super(
    TYPE,
    *wrapValues(
      pubkey,
      withdrawal_credentials,
      effective_balance,
      slashed,
      activation_eligibility_epoch,
      activation_epoch,
      exit_epoch,
      withdrawable_epoch,
      next_custody_secret_to_reveal,
      all_custody_secrets_revealed_epoch
    )
  )

  constructor(type: ContainerViewType<out ContainerViewRead>?, backingNode: TreeNode?) : super(
    type,
    backingNode
  )

  constructor() : super(TYPE)

  companion object {
    val TYPE = ContainerViewType<Validator>(
      listOf(
        VectorViewType<ByteView>(BasicViewTypes.BYTE_TYPE, 48),
        BasicViewTypes.BYTES32_TYPE,
        BasicViewTypes.UINT64_TYPE,
        BasicViewTypes.BIT_TYPE,
        BasicViewTypes.UINT64_TYPE,
        BasicViewTypes.UINT64_TYPE,
        BasicViewTypes.UINT64_TYPE,
        BasicViewTypes.UINT64_TYPE,
        BasicViewTypes.UINT64_TYPE,
        BasicViewTypes.UINT64_TYPE
      ), ::Validator
    )
  }
}

class PendingAttestation : AbstractMutableContainer {
  val aggregation_bits: SSZBitlist
    get() = SSZBitlistImpl(getAny(0))
  val data: AttestationData
    get() = getAny(1)
  val inclusion_delay: Slot
    get() = (get(2) as UInt64View).get().toUInt64()
  val proposer_index: ValidatorIndex
    get() = (get(3) as UInt64View).get().toUInt64()
  var crosslink_success: boolean
    get() = (get(4) as BitView).get()
    set(value) {
      set(4, BitView(value))
    }

  constructor(
    aggregation_bits: SSZBitlist,
    data: AttestationData,
    inclusion_delay: Slot,
    proposer_index: ValidatorIndex,
    crosslink_success: boolean
  ) : super(
    TYPE,
    (aggregation_bits as SSZAbstractCollection<*, *>).view,
    data,
    UInt64View(inclusion_delay.toUnsignedLong()),
    UInt64View(proposer_index.toUnsignedLong()),
    BitView(crosslink_success)
  )

  constructor(type: ContainerViewType<PendingAttestation>, backingNode: TreeNode) : super(
    type,
    backingNode
  )

  constructor() : super(TYPE)

  companion object {
    val TYPE = ContainerViewType(
      listOf(
        ListViewType<BitView>(
          BasicViewTypes.BIT_TYPE,
          Constants.MAX_VALIDATORS_PER_COMMITTEE.toLong()
        ),
        AttestationData.TYPE,
        BasicViewTypes.UINT64_TYPE,
        BasicViewTypes.UINT64_TYPE,
        BasicViewTypes.BIT_TYPE
      ), ::PendingAttestation
    )
  }
}

class HistoricalBatch : AbstractImmutableContainer {
  val block_roots: SSZVector<Root>
    get() = SSZVectorImpl(getAny(0), Bytes32View::get)
  val state_roots: SSZVector<Root>
    get() = SSZVectorImpl(getAny(1), Bytes32View::get)

  constructor(type: ContainerViewType<HistoricalBatch>, backingNode: TreeNode) :
      super(type, backingNode)

  constructor(block_roots: SSZVector<Root>, state_roots: SSZVector<Root>) :
      super(
        TYPE,
        (block_roots as SSZAbstractCollection<*, *>).view,
        (state_roots as SSZAbstractCollection<*, *>).view
      )

  constructor() : super(TYPE)

  companion object {
    val TYPE = ContainerViewType(
      listOf(
        VectorViewType<Root>(
          BasicViewTypes.BYTES32_TYPE,
          HISTORICAL_ROOTS_LIMIT.toLong()
        ),
        VectorViewType<Root>(
          BasicViewTypes.BYTES32_TYPE,
          HISTORICAL_ROOTS_LIMIT.toLong()
        )
      ), ::HistoricalBatch
    )
  }
}

class SigningRoot : AbstractImmutableContainer {
  val object_root: Root
    get() = (get(0) as Bytes32View).get()
  val domain: Domain
    get() = (get(1) as Bytes32View).get()

  constructor(object_root: Root, domain: Domain) : super(
    TYPE,
    Bytes32View(object_root),
    Bytes32View(domain)
  )

  constructor(
    type: ContainerViewType<out AbstractImmutableContainer>,
    backingNode: TreeNode
  ) : super(type, backingNode)

  constructor() : super(TYPE)

  companion object {
    val TYPE = ContainerViewType(
      listOf(BasicViewTypes.BYTES32_TYPE, BasicViewTypes.BYTES32_TYPE),
      ::SigningRoot
    )
  }
}

class CompactCommittee : AbstractImmutableContainer {
  val pubkeys: SSZList<BLSPubkey>
    get() = SSZListImpl<BLSPubkey, VectorViewRead<ByteView>>(getAny(0)) { v ->
      BLSPubkey(
        Bytes48.wrap(
          ViewUtils.getAllBytes(v)
        )
      )
    }
  val compact_validators: SSZList<uint64>
    get() = SSZListImpl<uint64, UInt64View>(getAny(1)) { v -> v.get().toUInt64() }

  constructor(
    type: ContainerViewType<out AbstractImmutableContainer>,
    backingNode: TreeNode
  ) : super(type, backingNode)

  constructor(
    pubkeys: SSZList<BLSPubkey>,
    compact_validators: SSZList<uint64>
  ) : super(
    TYPE,
    (pubkeys as SSZAbstractCollection<*, *>).view,
    (compact_validators as SSZAbstractCollection<*, *>).view
  )

  constructor(
    pubkeys: List<BLSPubkey>,
    compact_validators: List<uint64>
  ) : this(
    SSZListImpl<BLSPubkey, VectorViewRead<ByteView>>(
      Bytes48Type,
      MAX_VALIDATORS_PER_COMMITTEE,
      pubkeys,
      { v -> getBasicValue(v) },
      { u -> wrapBasicValue(u) }
    ),
    SSZListImpl<uint64, UInt64View>(
      BasicViewTypes.UINT64_TYPE,
      MAX_VALIDATORS_PER_COMMITTEE,
      compact_validators,
      { v -> getBasicValue(v) },
      { u -> wrapBasicValue(u) }
    )
  )

  constructor() : super(TYPE)

  companion object {
    val TYPE = ContainerViewType<CompactCommittee>(
      listOf(
        ListViewType<VectorViewRead<ByteView>>(Bytes48Type, MAX_VALIDATORS_PER_COMMITTEE.toLong()),
        ListViewType<UInt64View>(BasicViewTypes.UINT64_TYPE, MAX_VALIDATORS_PER_COMMITTEE.toLong())
      ),
      ::CompactCommittee
    )
  }
}

class CustodyChunkChallengeRecord : AbstractImmutableContainer {
  val challenge_index: uint64
    get() = getBasicValue(get(0))
  val challenger_index: ValidatorIndex
    get() = getBasicValue(get(1))
  val responder_index: ValidatorIndex
    get() = getBasicValue(get(2))
  val inclusion_epoch: Epoch
    get() = getBasicValue(get(3))
  val data_root: Root
    get() = getBasicValue(get(4))
  val chunk_index: uint64
    get() = getBasicValue(get(5))

  constructor(
    challenge_index: uint64,
    challenger_index: ValidatorIndex,
    responder_index: ValidatorIndex,
    inclusion_epoch: Epoch,
    data_root: Root,
    chunk_index: uint64
  ) : super(
    TYPE,
    *wrapValues(
      challenge_index,
      challenger_index,
      responder_index,
      inclusion_epoch,
      data_root,
      chunk_index
    )
  )

  constructor(
    type: ContainerViewType<out AbstractImmutableContainer>?,
    backingNode: TreeNode?
  ) : super(type, backingNode)

  constructor() : super(TYPE)

  companion object {
    val TYPE = ContainerViewType<CustodyChunkChallengeRecord>(
      listOf(
        BasicViewTypes.UINT64_TYPE,
        BasicViewTypes.UINT64_TYPE,
        BasicViewTypes.UINT64_TYPE,
        BasicViewTypes.UINT64_TYPE,
        BasicViewTypes.BYTES32_TYPE,
        BasicViewTypes.UINT64_TYPE
      ),
      ::CustodyChunkChallengeRecord
    )
  }
}

class BeaconState : AbstractMutableContainer {
  var genesis_time: uint64
    get() = getBasicValue(get(0))
    set(value) = set(0,
      wrapBasicValue(value)
    )
  var genesis_validators_root: Root
    get() = getBasicValue(get(1))
    set(value) = set(1,
      wrapBasicValue(value)
    )
  var slot: Slot
    get() = getBasicValue(get(2))
    set(value) {
      set(2, wrapBasicValue(value))
    }
  var fork: Fork
    get() = getAny(3)
    set(value) {
      set(3, value)
    }
  var latest_block_header: BeaconBlockHeader
    get() = getAny(4)
    set(value) {
      set(4, value)
    }
  val block_roots: SSZMutableVector<Root>
    get() = SSZMutableVectorImpl(getAnyByRef(5), Bytes32View::get, ::Bytes32View)
  val state_roots: SSZMutableVector<Root>
    get() = SSZMutableVectorImpl(getAnyByRef(6), Bytes32View::get, ::Bytes32View)
  val historical_roots: SSZMutableList<Root>
    get() = SSZMutableListImpl(getAnyByRef(7), Bytes32View::get, ::Bytes32View)
  var eth1_data: Eth1Data
    get() = getAnyByRef(8)
    set(value) {
      set(8, value)
    }
  var eth1_data_votes: SSZMutableList<Eth1Data>
    get() = SSZMutableListImpl<Eth1Data, Eth1Data>(getAnyByRef(9), { v -> v }, { v -> v })
    set(value) {
      set(9, (value as SSZAbstractCollection<*, *>).view)
    }
  var eth1_deposit_index: uint64
    get() = getBasicValue(get(10))
    set(value) {
      set(10, wrapBasicValue(value))
    }
  val validators: SSZMutableList<Validator>
    get() = SSZMutableListImpl<Validator, Validator>(getAnyByRef(11), { v -> v }, { v -> v })
  val balances: SSZMutableList<Gwei>
    get() = SSZMutableListImpl<Gwei, UInt64View>(
      getAnyByRef(12),
      { v -> getBasicValue(v) },
      { v -> wrapBasicValue(v) })
  var randao_mixes: SSZMutableVector<Root>
    get() = SSZMutableVectorImpl(getAnyByRef(13), Bytes32View::get, ::Bytes32View)
    set(value) = set(13, (value as SSZAbstractCollection<*, *>).view)
  val slashings: SSZMutableVector<Gwei>
    get() = SSZMutableVectorImpl<Gwei, UInt64View>(
      getAnyByRef(14),
      { v -> getBasicValue(v) },
      { v -> wrapBasicValue(v) })
  var previous_epoch_attestations: SSZMutableList<PendingAttestation>
    get() = SSZMutableListImpl<PendingAttestation, PendingAttestation>(
      getAnyByRef(15),
      { v -> v },
      { v -> v })
    set(value) {
      set(15, (value as SSZAbstractCollection<*, *>).view)
    }
  var current_epoch_attestations: SSZMutableList<PendingAttestation>
    get() = SSZMutableListImpl<PendingAttestation, PendingAttestation>(
      getAnyByRef(16),
      { v -> v },
      { v -> v })
    set(value) {
      set(16, (value as SSZAbstractCollection<*, *>).view)
    }
  val justification_bits: SSZMutableBitvector
    get() = SSZMutableBitvectorImpl(getAnyByRef<VectorViewWrite<BitView>>(17))
  var previous_justified_checkpoint: Checkpoint
    get() = getAny(18)
    set(value) {
      set(18, value)
    }
  var current_justified_checkpoint: Checkpoint
    get() = getAny(19)
    set(value) {
      set(19, value)
    }
  var finalized_checkpoint: Checkpoint
    get() = getAny(20)
    set(value) = set(20, value)
  var current_epoch_start_shard: Shard
    get() = getBasicValue(get(21))
    set(value) = set(21,
      wrapBasicValue(value)
    )
  val shard_states: SSZMutableList<ShardState>
    get() = SSZMutableListImpl<ShardState, ShardState>(
      getAnyByRef(22),
      { v -> v },
      { v -> v })
  val online_countdown: SSZMutableList<OnlineEpochs>
    get() = SSZMutableListImpl<OnlineEpochs, ByteView>(
      getAnyByRef(23),
      { v -> getBasicValue(v) },
      { v -> wrapBasicValue(v) })
  var current_light_committee: CompactCommittee
    get() = getAny(24)
    set(value) {
      set(24, value)
    }
  var next_light_committee: CompactCommittee
    get() = getAny(25)
    set(value) {
      set(25, value)
    }
  val exposed_derived_secrets: SSZMutableVector<SSZMutableList<ValidatorIndex>>
    get() = SSZMutableVectorImpl<SSZMutableList<ValidatorIndex>, ListViewWrite<UInt64View>>(
      getAnyByRef(26),
      { v ->
        SSZMutableListImpl(
          v,
          { u ->
            getBasicValue<ValidatorIndex>(
              u
            )
          },
          { u -> wrapBasicValue(u) })
      },
      { v -> (v as SSZMutableListImpl<ValidatorIndex, UInt64View>).view }
    )
  var custody_chunk_challenge_records: SSZMutableList<CustodyChunkChallengeRecord>
    get() = SSZMutableListImpl<CustodyChunkChallengeRecord, CustodyChunkChallengeRecord>(
      getAnyByRef(27), { v -> v }, { v -> v })
    set(value) {
      set(27, (value as SSZAbstractCollection<*, *>).view)
    }
  var custody_chunk_challenge_index: uint64
    get() = getBasicValue(get(28))
    set(value) {
      set(28, wrapBasicValue(value))
    }

  constructor(
    genesis_time: uint64,
    genesis_validators_root: Root = Root(),
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
    justification_bits: SSZBitvector,
    previous_justified_checkpoint: Checkpoint,
    current_justified_checkpoint: Checkpoint,
    finalized_checkpoint: Checkpoint,
    current_epoch_start_shard: Shard,
    shard_states: SSZList<ShardState>,
    online_countdown: SSZList<OnlineEpochs>,
    current_light_committee: CompactCommittee,
    next_light_committee: CompactCommittee,
    exposed_derived_secrets: SSZVector<SSZList<ValidatorIndex>>,
    custody_chunk_challenge_records: SSZList<CustodyChunkChallengeRecord> = SSZListImpl(
      ListViewType<CustodyChunkChallengeRecord>(
        CustodyChunkChallengeRecord.TYPE,
        MAX_CUSTODY_CHUNK_CHALLENGE_RECORDS.toLong()
      ).default
    ) { v -> v },
    custody_chunk_challenge_index: uint64 = 0uL
  ) : super(
    TYPE,
    *wrapValues(
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
      current_epoch_start_shard,
      shard_states,
      online_countdown,
      current_light_committee,
      next_light_committee,
      exposed_derived_secrets,
      custody_chunk_challenge_records,
      custody_chunk_challenge_index
    )
  )

  constructor(
    genesis_time: uint64,
    fork: Fork,
    latest_block_header: BeaconBlockHeader,
    eth1_data: Eth1Data,
    randao_mixes: SSZMutableVector<Root>
  ) : this() {
    this.genesis_time = genesis_time
    this.fork = fork
    this.latest_block_header = latest_block_header
    this.eth1_data = eth1_data
    this.randao_mixes = randao_mixes
  }

  constructor(type: ContainerViewType<out ContainerViewRead>?, backingNode: TreeNode?) : super(
    type,
    backingNode
  )

  constructor() : super(TYPE)

  companion object {
    val TYPE = ContainerViewType<BeaconState>(
      listOf(
        BasicViewTypes.UINT64_TYPE,
        BasicViewTypes.BYTES32_TYPE,
        BasicViewTypes.UINT64_TYPE,
        Fork.TYPE,
        BeaconBlockHeader.TYPE,
        VectorViewType<Bytes32View>(
          BasicViewTypes.BYTES32_TYPE,
          SLOTS_PER_HISTORICAL_ROOT.toLong()
        ),
        VectorViewType<Bytes32View>(
          BasicViewTypes.BYTES32_TYPE,
          SLOTS_PER_HISTORICAL_ROOT.toLong()
        ),
        ListViewType<Bytes32View>(BasicViewTypes.BYTES32_TYPE, HISTORICAL_ROOTS_LIMIT.toLong()),
        Eth1Data.TYPE,
        ListViewType<Eth1Data>(
          Eth1Data.TYPE,
          (EPOCHS_PER_ETH1_VOTING_PERIOD * SLOTS_PER_EPOCH).toLong()
        ),
        BasicViewTypes.UINT64_TYPE,
        ListViewType<Validator>(Validator.TYPE, VALIDATOR_REGISTRY_LIMIT.toLong()),
        ListViewType<UInt64View>(BasicViewTypes.UINT64_TYPE, VALIDATOR_REGISTRY_LIMIT.toLong()),
        VectorViewType<Bytes32View>(
          BasicViewTypes.BYTES32_TYPE,
          EPOCHS_PER_HISTORICAL_VECTOR.toLong()
        ),
        VectorViewType<UInt64View>(
          BasicViewTypes.UINT64_TYPE,
          EPOCHS_PER_SLASHINGS_VECTOR.toLong()
        ),
        ListViewType<PendingAttestation>(
          PendingAttestation.TYPE,
          (MAX_ATTESTATIONS * SLOTS_PER_EPOCH).toLong()
        ),
        ListViewType<PendingAttestation>(
          PendingAttestation.TYPE,
          (MAX_ATTESTATIONS * SLOTS_PER_EPOCH).toLong()
        ),
        VectorViewType<BitView>(BasicViewTypes.BIT_TYPE, JUSTIFICATION_BITS_LENGTH.toLong()),
        Checkpoint.TYPE,
        Checkpoint.TYPE,
        Checkpoint.TYPE,
        BasicViewTypes.UINT64_TYPE,
        ListViewType<ShardState>(ShardState.TYPE, MAX_SHARDS.toLong()),
        ListViewType<UInt64View>(BasicViewTypes.UINT64_TYPE, VALIDATOR_REGISTRY_LIMIT.toLong()),
        CompactCommittee.TYPE,
        CompactCommittee.TYPE,
        VectorViewType<ListViewRead<UInt64View>>(
          ListViewType<UInt64View>(
            BasicViewTypes.UINT64_TYPE,
            (MAX_EARLY_DERIVED_SECRET_REVEALS * SLOTS_PER_EPOCH).toLong()
          ), EARLY_DERIVED_SECRET_PENALTY_MAX_FUTURE_EPOCHS.toLong()
        ),
        ListViewType<CustodyChunkChallengeRecord>(
          CustodyChunkChallengeRecord.TYPE,
          MAX_CUSTODY_CHUNK_CHALLENGE_RECORDS.toLong()
        ),
        BasicViewTypes.UINT64_TYPE
      ),
      ::BeaconState
    )
  }

  fun copy(): BeaconState = BeaconState(TYPE, this.backingNode)
}
