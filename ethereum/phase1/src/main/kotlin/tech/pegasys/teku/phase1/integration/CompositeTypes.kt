package tech.pegasys.teku.phase1.integration

import tech.pegasys.teku.phase1.integration.datastructures.AttestationCustodyBitWrapperImpl
import tech.pegasys.teku.phase1.integration.datastructures.AttestationDataWrapper
import tech.pegasys.teku.phase1.integration.datastructures.AttestationWrapper
import tech.pegasys.teku.phase1.integration.datastructures.AttesterSlashingWrapper
import tech.pegasys.teku.phase1.integration.datastructures.BeaconBlockBodyWrapper
import tech.pegasys.teku.phase1.integration.datastructures.BeaconBlockHeaderWrapper
import tech.pegasys.teku.phase1.integration.datastructures.BeaconBlockWrapper
import tech.pegasys.teku.phase1.integration.datastructures.BeaconStateWrapper
import tech.pegasys.teku.phase1.integration.datastructures.CheckpointWrapper
import tech.pegasys.teku.phase1.integration.datastructures.CompactCommitteeWrapper
import tech.pegasys.teku.phase1.integration.datastructures.CustodyKeyRevealWrapper
import tech.pegasys.teku.phase1.integration.datastructures.CustodySlashingWrapper
import tech.pegasys.teku.phase1.integration.datastructures.DepositDataWrapper
import tech.pegasys.teku.phase1.integration.datastructures.DepositMessageWrapper
import tech.pegasys.teku.phase1.integration.datastructures.DepositWrapper
import tech.pegasys.teku.phase1.integration.datastructures.EarlyDerivedSecretRevealWrapper
import tech.pegasys.teku.phase1.integration.datastructures.Eth1DataWrapper
import tech.pegasys.teku.phase1.integration.datastructures.ExposedValidatorIndicesWrapper
import tech.pegasys.teku.phase1.integration.datastructures.ForkDataWrapper
import tech.pegasys.teku.phase1.integration.datastructures.ForkWrapper
import tech.pegasys.teku.phase1.integration.datastructures.HistoricalBatchWrapper
import tech.pegasys.teku.phase1.integration.datastructures.IndexedAttestationWrapper
import tech.pegasys.teku.phase1.integration.datastructures.PendingAttestationWrapper
import tech.pegasys.teku.phase1.integration.datastructures.ProposerSlashingWrapper
import tech.pegasys.teku.phase1.integration.datastructures.ShardBlockHeaderWrapper
import tech.pegasys.teku.phase1.integration.datastructures.ShardBlockWrapper
import tech.pegasys.teku.phase1.integration.datastructures.ShardStateWrapper
import tech.pegasys.teku.phase1.integration.datastructures.ShardTransitionWrapper
import tech.pegasys.teku.phase1.integration.datastructures.SignedBeaconBlockHeaderWrapper
import tech.pegasys.teku.phase1.integration.datastructures.SignedBeaconBlockWrapper
import tech.pegasys.teku.phase1.integration.datastructures.SignedCustodySlashingWrapper
import tech.pegasys.teku.phase1.integration.datastructures.SignedShardBlockWrapper
import tech.pegasys.teku.phase1.integration.datastructures.SignedVoluntaryExitWrapper
import tech.pegasys.teku.phase1.integration.datastructures.SigningRootWrapper
import tech.pegasys.teku.phase1.integration.datastructures.ValidatorWrapper
import tech.pegasys.teku.phase1.integration.datastructures.VoluntaryExitWrapper
import tech.pegasys.teku.phase1.onotole.phase1.Attestation
import tech.pegasys.teku.phase1.onotole.phase1.AttestationCustodyBitWrapper
import tech.pegasys.teku.phase1.onotole.phase1.AttestationData
import tech.pegasys.teku.phase1.onotole.phase1.AttesterSlashing
import tech.pegasys.teku.phase1.onotole.phase1.BeaconBlock
import tech.pegasys.teku.phase1.onotole.phase1.BeaconBlockBody
import tech.pegasys.teku.phase1.onotole.phase1.BeaconBlockHeader
import tech.pegasys.teku.phase1.onotole.phase1.BeaconState
import tech.pegasys.teku.phase1.onotole.phase1.Checkpoint
import tech.pegasys.teku.phase1.onotole.phase1.CompactCommittee
import tech.pegasys.teku.phase1.onotole.phase1.CustodyKeyReveal
import tech.pegasys.teku.phase1.onotole.phase1.CustodySlashing
import tech.pegasys.teku.phase1.onotole.phase1.Deposit
import tech.pegasys.teku.phase1.onotole.phase1.DepositData
import tech.pegasys.teku.phase1.onotole.phase1.DepositMessage
import tech.pegasys.teku.phase1.onotole.phase1.EarlyDerivedSecretReveal
import tech.pegasys.teku.phase1.onotole.phase1.Eth1Data
import tech.pegasys.teku.phase1.onotole.phase1.ExposedValidatorIndices
import tech.pegasys.teku.phase1.onotole.phase1.Fork
import tech.pegasys.teku.phase1.onotole.phase1.ForkData
import tech.pegasys.teku.phase1.onotole.phase1.HistoricalBatch
import tech.pegasys.teku.phase1.onotole.phase1.IndexedAttestation
import tech.pegasys.teku.phase1.onotole.phase1.PendingAttestation
import tech.pegasys.teku.phase1.onotole.phase1.ProposerSlashing
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
import tech.pegasys.teku.phase1.onotole.phase1.Validator
import tech.pegasys.teku.phase1.onotole.phase1.VoluntaryExit
import kotlin.reflect.KClass
import tech.pegasys.teku.datastructures.blocks.BeaconBlockHeader as TekuBeaconBlockHeader
import tech.pegasys.teku.datastructures.blocks.Eth1Data as TekuEth1Data
import tech.pegasys.teku.datastructures.blocks.SignedBeaconBlockHeader as TekuSignedBeaconBlockHeader
import tech.pegasys.teku.datastructures.operations.Deposit as TekuDeposit
import tech.pegasys.teku.datastructures.operations.DepositData as TekuDepositData
import tech.pegasys.teku.datastructures.operations.DepositMessage as TekuDepositMessage
import tech.pegasys.teku.datastructures.operations.ProposerSlashing as TekuProposerSlashing
import tech.pegasys.teku.datastructures.operations.SignedVoluntaryExit as TekuSignedVoluntaryExit
import tech.pegasys.teku.datastructures.operations.VoluntaryExit as TekuVoluntaryExit
import tech.pegasys.teku.datastructures.phase1.blocks.BeaconBlockBodyPhase1 as TekuBeaconBlockBody
import tech.pegasys.teku.datastructures.phase1.blocks.BeaconBlockPhase1 as TekuBeaconBlock
import tech.pegasys.teku.datastructures.phase1.blocks.SignedBeaconBlockPhase1 as TekuSignedBeaconBlock
import tech.pegasys.teku.datastructures.phase1.operations.AttestationCustodyBitWrapper as TekuAttestationCustodyBitWrapper
import tech.pegasys.teku.datastructures.phase1.operations.AttestationDataPhase1 as TekuAttestationData
import tech.pegasys.teku.datastructures.phase1.operations.AttestationPhase1 as TekuAttestation
import tech.pegasys.teku.datastructures.phase1.operations.AttesterSlashingPhase1 as TekuAttesterSlashing
import tech.pegasys.teku.datastructures.phase1.operations.CustodyKeyReveal as TekuCustodyKeyReveal
import tech.pegasys.teku.datastructures.phase1.operations.CustodySlashing as TekuCustodySlashing
import tech.pegasys.teku.datastructures.phase1.operations.EarlyDerivedSecretReveal as TekuEarlyDerivedSecretReveal
import tech.pegasys.teku.datastructures.phase1.operations.IndexedAttestationPhase1 as TekuIndexedAttestation
import tech.pegasys.teku.datastructures.phase1.operations.SignedCustodySlashing as TekuSignedCustodySlashing
import tech.pegasys.teku.datastructures.phase1.shard.ShardBlock as TekuShardBlock
import tech.pegasys.teku.datastructures.phase1.shard.ShardBlockHeader as TekuShardBlockHeader
import tech.pegasys.teku.datastructures.phase1.shard.ShardState as TekuShardState
import tech.pegasys.teku.datastructures.phase1.shard.ShardTransition as TekuShardTransition
import tech.pegasys.teku.datastructures.phase1.shard.SignedShardBlock as TekuSignedShardBlock
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

internal val Eth1DataType =
  object : WrappedTypePair<Eth1Data, Eth1DataWrapper, TekuEth1Data>(
    Eth1Data::class,
    TekuEth1Data::class,
    Eth1DataWrapper::class
  ) {}

internal val BeaconBlockHeaderType =
  object : WrappedTypePair<BeaconBlockHeader, BeaconBlockHeaderWrapper, TekuBeaconBlockHeader>
    (BeaconBlockHeader::class, TekuBeaconBlockHeader::class, BeaconBlockHeaderWrapper::class) {}

internal val SignedBeaconBlockHeaderType = object :
  WrappedTypePair<SignedBeaconBlockHeader, SignedBeaconBlockHeaderWrapper, TekuSignedBeaconBlockHeader>(
    SignedBeaconBlockHeader::class,
    TekuSignedBeaconBlockHeader::class,
    SignedBeaconBlockHeaderWrapper::class
  ) {}

internal val BeaconBlockBodyType =
  object : WrappedTypePair<BeaconBlockBody, BeaconBlockBodyWrapper, TekuBeaconBlockBody>
    (BeaconBlockBody::class, TekuBeaconBlockBody::class, BeaconBlockBodyWrapper::class) {}

internal val BeaconBlockType =
  object : WrappedTypePair<BeaconBlock, BeaconBlockWrapper, TekuBeaconBlock>
    (BeaconBlock::class, TekuBeaconBlock::class, BeaconBlockWrapper::class) {}

internal val SignedBeaconBlockType =
  object : WrappedTypePair<SignedBeaconBlock, SignedBeaconBlockWrapper, TekuSignedBeaconBlock>
    (SignedBeaconBlock::class, TekuSignedBeaconBlock::class, SignedBeaconBlockWrapper::class) {}

internal val DepositType = object :
  WrappedTypePair<Deposit, DepositWrapper, TekuDeposit>(
    Deposit::class,
    TekuDeposit::class,
    DepositWrapper::class
  ) {}

internal val DepositDataType =
  object : WrappedTypePair<DepositData, DepositDataWrapper, TekuDepositData>(
    DepositData::class,
    TekuDepositData::class,
    DepositDataWrapper::class
  ) {}

internal val DepositMessageType =
  object : WrappedTypePair<DepositMessage, DepositMessageWrapper, TekuDepositMessage>(
    DepositMessage::class,
    TekuDepositMessage::class,
    DepositMessageWrapper::class
  ) {}

internal val SignedVoluntaryExitType = object :
  WrappedTypePair<SignedVoluntaryExit, SignedVoluntaryExitWrapper, TekuSignedVoluntaryExit>(
    SignedVoluntaryExit::class,
    TekuSignedVoluntaryExit::class,
    SignedVoluntaryExitWrapper::class
  ) {}

internal val VoluntaryExitType =
  object : WrappedTypePair<VoluntaryExit, VoluntaryExitWrapper, TekuVoluntaryExit>(
    VoluntaryExit::class,
    TekuVoluntaryExit::class, VoluntaryExitWrapper::class
  ) {}

internal val AttestationCustodyBitWrapperType =
  object : TypePair<AttestationCustodyBitWrapper, TekuAttestationCustodyBitWrapper> {
    override val teku = TekuAttestationCustodyBitWrapper::class
    override val onotole = AttestationCustodyBitWrapper::class
    override fun wrap(v: TekuAttestationCustodyBitWrapper) = AttestationCustodyBitWrapperImpl(
      v.attestation_data_root,
      UInt64Type.wrap(v.block_index),
      v.bit
    )

    override fun unwrap(v: AttestationCustodyBitWrapper) = TekuAttestationCustodyBitWrapper(
      v.attestation_data_root,
      UInt64Type.unwrap(v.block_index),
      v.bit
    )
  }

internal val AttestationDataType =
  object : WrappedTypePair<AttestationData, AttestationDataWrapper, TekuAttestationData>(
    AttestationData::class,
    TekuAttestationData::class,
    AttestationDataWrapper::class
  ) {}

internal val AttestationType =
  object : WrappedTypePair<Attestation, AttestationWrapper, TekuAttestation>(
    Attestation::class,
    TekuAttestation::class,
    AttestationWrapper::class
  ) {}

internal val AttesterSlashingType =
  object : WrappedTypePair<AttesterSlashing, AttesterSlashingWrapper, TekuAttesterSlashing>(
    AttesterSlashing::class,
    TekuAttesterSlashing::class,
    AttesterSlashingWrapper::class
  ) {}

internal val ProposerSlashingType =
  object : WrappedTypePair<ProposerSlashing, ProposerSlashingWrapper, TekuProposerSlashing>(
    ProposerSlashing::class, TekuProposerSlashing::class, ProposerSlashingWrapper::class
  ) {}

internal val CustodyKeyRevealType =
  object : WrappedTypePair<CustodyKeyReveal, CustodyKeyRevealWrapper, TekuCustodyKeyReveal>(
    CustodyKeyReveal::class,
    TekuCustodyKeyReveal::class,
    CustodyKeyRevealWrapper::class
  ) {}

internal val CustodySlashingType =
  object : WrappedTypePair<CustodySlashing, CustodySlashingWrapper, TekuCustodySlashing>(
    CustodySlashing::class,
    TekuCustodySlashing::class,
    CustodySlashingWrapper::class
  ) {}

internal val EarlyDerivedSecretRevealType = object :
  WrappedTypePair<EarlyDerivedSecretReveal, EarlyDerivedSecretRevealWrapper, TekuEarlyDerivedSecretReveal>(
    EarlyDerivedSecretReveal::class,
    TekuEarlyDerivedSecretReveal::class,
    EarlyDerivedSecretRevealWrapper::class
  ) {}

internal val IndexedAttestationType = object :
  WrappedTypePair<IndexedAttestation, IndexedAttestationWrapper, TekuIndexedAttestation>(
    IndexedAttestation::class,
    TekuIndexedAttestation::class,
    IndexedAttestationWrapper::class
  ) {}

internal val SignedCustodySlashingType = object :
  WrappedTypePair<SignedCustodySlashing, SignedCustodySlashingWrapper, TekuSignedCustodySlashing>(
    SignedCustodySlashing::class,
    TekuSignedCustodySlashing::class,
    SignedCustodySlashingWrapper::class
  ) {}

internal val ShardBlockType =
  object : WrappedTypePair<ShardBlock, ShardBlockWrapper, TekuShardBlock>(
    ShardBlock::class,
    TekuShardBlock::class,
    ShardBlockWrapper::class
  ) {}

internal val ShardBlockHeaderType =
  object : WrappedTypePair<ShardBlockHeader, ShardBlockHeaderWrapper, TekuShardBlockHeader>(
    ShardBlockHeader::class,
    TekuShardBlockHeader::class,
    ShardBlockHeaderWrapper::class
  ) {}

internal val ShardStateType =
  object : WrappedTypePair<ShardState, ShardStateWrapper, TekuShardState>(
    ShardState::class,
    TekuShardState::class,
    ShardStateWrapper::class
  ) {}

internal val ShardTransitionType =
  object : WrappedTypePair<ShardTransition, ShardTransitionWrapper, TekuShardTransition>(
    ShardTransition::class,
    TekuShardTransition::class,
    ShardTransitionWrapper::class
  ) {}

internal val SignedShardBlockType =
  object : WrappedTypePair<SignedShardBlock, SignedShardBlockWrapper, TekuSignedShardBlock>(
    SignedShardBlock::class,
    TekuSignedShardBlock::class,
    SignedShardBlockWrapper::class
  ) {}

internal val ExposedValidatorIndicesType = object :
  WrappedTypePair<ExposedValidatorIndices, ExposedValidatorIndicesWrapper, TekuExposedValidatorIndices>(
    ExposedValidatorIndices::class,
    TekuExposedValidatorIndices::class,
    ExposedValidatorIndicesWrapper::class
  ) {}

internal val BeaconStateType =
  object : WrappedTypePair<BeaconState, BeaconStateWrapper, TekuBeaconState>(
    BeaconState::class,
    TekuBeaconState::class,
    BeaconStateWrapper::class
  ) {}

internal val PendingAttestationType = object :
  WrappedTypePair<PendingAttestation, PendingAttestationWrapper, TekuPendingAttestation>(
    PendingAttestation::class,
    TekuPendingAttestation::class,
    PendingAttestationWrapper::class
  ) {}

internal val ValidatorType =
  object : WrappedTypePair<Validator, ValidatorWrapper, TekuValidator>(
    Validator::class,
    TekuValidator::class,
    ValidatorWrapper::class
  ) {}

internal val CheckpointType =
  object : WrappedTypePair<Checkpoint, CheckpointWrapper, TekuCheckpoint>(
    Checkpoint::class,
    TekuCheckpoint::class,
    CheckpointWrapper::class
  ) {}

internal val ForkType =
  object : WrappedTypePair<Fork, ForkWrapper, TekuFork>(
    Fork::class,
    TekuFork::class,
    ForkWrapper::class
  ) {}

internal val ForkDataType = object :
  WrappedTypePair<ForkData, ForkDataWrapper, TekuForkData>(
    ForkData::class,
    TekuForkData::class,
    ForkDataWrapper::class
  ) {}

internal val HistoricalBatchType =
  object : WrappedTypePair<HistoricalBatch, HistoricalBatchWrapper, TekuHistoricalBatch>(
    HistoricalBatch::class,
    TekuHistoricalBatch::class,
    HistoricalBatchWrapper::class
  ) {}

internal val SigningRootType =
  object : WrappedTypePair<SigningRoot, SigningRootWrapper, TekuSigningRoot>(
    SigningRoot::class,
    TekuSigningRoot::class,
    SigningRootWrapper::class
  ) {}

internal val CompactCommitteeType = object :
  WrappedTypePair<CompactCommittee, CompactCommitteeWrapper, TekuCompactCommittee>(
    CompactCommittee::class, TekuCompactCommittee::class, CompactCommitteeWrapper::class
  ) {}

internal fun <Onotole : Any, Teku : Any> resolveCompositeType(type: KClass<Onotole>): TypePair<Onotole, Teku>? {
  return when (type) {
    Eth1Data::class -> Eth1DataType
    BeaconBlockHeader::class -> BeaconBlockHeaderType
    SignedBeaconBlockHeader::class -> SignedBeaconBlockHeaderType
    BeaconBlockBody::class -> BeaconBlockBodyType
    BeaconBlock::class -> BeaconBlockType
    SignedBeaconBlock::class -> SignedBeaconBlockType
    Deposit::class -> DepositType
    DepositData::class -> DepositDataType
    DepositMessage::class -> DepositMessageType
    SignedVoluntaryExit::class -> SignedVoluntaryExitType
    VoluntaryExit::class -> VoluntaryExitType
    AttestationCustodyBitWrapper::class -> AttestationCustodyBitWrapperType
    AttestationData::class -> AttestationDataType
    Attestation::class -> AttestationType
    AttesterSlashing::class -> AttesterSlashingType
    ProposerSlashing::class -> ProposerSlashingType
    CustodyKeyReveal::class -> CustodyKeyRevealType
    CustodySlashing::class -> CustodySlashingType
    EarlyDerivedSecretReveal::class -> EarlyDerivedSecretRevealType
    IndexedAttestation::class -> IndexedAttestationType
    SignedCustodySlashing::class -> SignedCustodySlashingType
    ShardBlock::class -> ShardBlockType
    ShardBlockHeader::class -> ShardBlockHeaderType
    ShardState::class -> ShardStateType
    ShardTransition::class -> ShardTransitionType
    SignedShardBlock::class -> SignedShardBlockType
    ExposedValidatorIndices::class -> ExposedValidatorIndicesType
    BeaconState::class -> BeaconStateType
    PendingAttestation::class -> PendingAttestationType
    Validator::class -> ValidatorType
    Checkpoint::class -> CheckpointType
    Fork::class -> ForkType
    ForkData::class -> ForkDataType
    HistoricalBatch::class -> HistoricalBatchType
    SigningRoot::class -> SigningRootType
    CompactCommittee::class -> CompactCommitteeType
    else -> null
  } as TypePair<Onotole, Teku>?
}
