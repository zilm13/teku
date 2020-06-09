package tech.pegasys.teku.phase1.core

import com.google.common.primitives.UnsignedLong
import org.apache.tuweni.bytes.Bytes
import tech.pegasys.teku.phase1.ssz.uint64
import kotlin.reflect.KClass
import tech.pegasys.teku.bls.BLSPublicKey as TekuBLSPublicKey
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

interface TypeConverter {
  @Suppress("UNCHECKED_CAST")
  companion object {

    fun <T : Any> match(type: KClass<*>): KClass<T> {
      return when (type) {
        UnsignedLong::class -> UnsignedLong::class as KClass<T>
        uint64::class -> UnsignedLong::class as KClass<T>
        UnsignedLong::class -> uint64::class as KClass<T>
        BLSPubkey::class -> TekuBLSPublicKey::class as KClass<T>
        TekuBLSPublicKey::class -> BLSPubkey::class as KClass<T>
        ValidatorIndicesSSZListDelegate::class -> return TekuValidatorIndicesSSZList::class as KClass<T>
        TekuValidatorIndicesSSZList::class -> return ValidatorIndicesSSZListDelegate::class as KClass<T>
        ValidatorDelegate::class -> TekuValidator::class as KClass<T>
        TekuValidator::class -> ValidatorDelegate::class as KClass<T>
        ForkDelegate::class -> TekuFork::class as KClass<T>
        TekuFork::class -> ForkDelegate::class as KClass<T>
        ForkDataDelegate::class -> TekuForkData::class as KClass<T>
        TekuForkData::class -> ForkDataDelegate::class as KClass<T>
        CheckpointDelegate::class -> TekuCheckpoint::class as KClass<T>
        TekuCheckpoint::class -> CheckpointDelegate::class as KClass<T>
        AttestationDataDelegate::class -> TekuAttestationData::class as KClass<T>
        TekuAttestationData::class -> AttestationDataDelegate::class as KClass<T>
        PendingAttestationDelegate::class -> TekuPendingAttestation::class as KClass<T>
        TekuPendingAttestation::class -> PendingAttestationDelegate::class as KClass<T>
        Eth1DataDelegate::class -> TekuEth1Data::class as KClass<T>
        TekuEth1Data::class -> Eth1DataDelegate::class as KClass<T>
        HistoricalBatchDelegate::class -> TekuHistoricalBatch::class as KClass<T>
        TekuHistoricalBatch::class -> HistoricalBatchDelegate::class as KClass<T>
        DepositMessageDelegate::class -> TekuDepositMessage::class as KClass<T>
        TekuDepositMessage::class -> DepositMessageDelegate::class as KClass<T>
        DepositDataDelegate::class -> TekuDepositData::class as KClass<T>
        TekuDepositData::class -> DepositDataDelegate::class as KClass<T>
        BeaconBlockHeaderDelegate::class -> TekuBeaconBlockHeader::class as KClass<T>
        TekuBeaconBlockHeader::class -> BeaconBlockHeaderDelegate::class as KClass<T>
        SigningRootDelegate::class -> TekuSigningRoot::class as KClass<T>
        TekuSigningRoot::class -> SigningRootDelegate::class as KClass<T>
        AttestationDelegate::class -> TekuAttestation::class as KClass<T>
        TekuAttestation::class -> AttestationDelegate::class as KClass<T>
        IndexedAttestationDelegate::class -> TekuIndexedAttestation::class as KClass<T>
        TekuIndexedAttestation::class -> IndexedAttestationDelegate::class as KClass<T>
        AttesterSlashingDelegate::class -> TekuAttesterSlashing::class as KClass<T>
        TekuAttesterSlashing::class -> AttesterSlashingDelegate::class as KClass<T>
        DepositDelegate::class -> TekuDeposit::class as KClass<T>
        TekuDeposit::class -> DepositDelegate::class as KClass<T>
        VoluntaryExitDelegate::class -> TekuVoluntaryExit::class as KClass<T>
        TekuVoluntaryExit::class -> VoluntaryExitDelegate::class as KClass<T>
        SignedVoluntaryExitDelegate::class -> TekuSignedVoluntaryExit::class as KClass<T>
        TekuSignedVoluntaryExit::class -> SignedVoluntaryExitDelegate::class as KClass<T>
        SignedBeaconBlockHeaderDelegate::class -> TekuSignedBeaconBlockHeader::class as KClass<T>
        TekuSignedBeaconBlockHeader::class -> SignedBeaconBlockHeaderDelegate::class as KClass<T>
        ProposerSlashingDelegate::class -> TekuProposerSlashing::class as KClass<T>
        TekuProposerSlashing::class -> ProposerSlashingDelegate::class as KClass<T>
        CustodyKeyReveal::class -> TODO()
        EarlyDerivedSecretReveal::class -> TODO()
        ShardBlock::class -> TODO()
        SignedShardBlock::class -> TODO()
        ShardBlockHeader::class -> TODO()
        ShardState::class -> TODO()
        ShardTransition::class -> TODO()
        CustodySlashing::class -> TODO()
        SignedCustodySlashing::class -> TODO()
        BeaconBlockBodyDelegate::class -> TekuBeaconBlockBody::class as KClass<T>
        TekuBeaconBlockBody::class -> BeaconBlockBodyDelegate::class as KClass<T>
        BeaconBlockDelegate::class -> TekuBeaconBlock::class as KClass<T>
        TekuBeaconBlock::class -> BeaconBlockDelegate::class as KClass<T>
        SignedBeaconBlockDelegate::class -> TekuSignedBeaconBlock::class as KClass<T>
        TekuSignedBeaconBlock::class -> SignedBeaconBlockDelegate::class as KClass<T>
        CompactCommittee::class -> TODO()
        BeaconStateDelegate::class -> TekuBeaconState::class as KClass<T>
        TekuBeaconState::class -> BeaconStateDelegate::class as KClass<T>
        AttestationCustodyBitWrapper::class -> TODO()
        SSZBitListDelegate::class -> TekuBitlist::class as KClass<T>
        TekuBitlist::class -> SSZBitListDelegate::class as KClass<T>
        SSZBitVectorDelegate::class -> TekuBitvector::class as KClass<T>
        TekuBitvector::class -> SSZBitVectorDelegate::class as KClass<T>
        else -> throw IllegalArgumentException("Unknown type ${type::qualifiedName}")
      }
    }

    fun <T : Any> cast(o: Any, to: KClass<T>): T {
      if (o::class == to) {
        return o as T
      }

      when (o) {
        is uint64 -> return UnsignedLong.valueOf(o.toLong()) as T
        is UnsignedLong -> return o.toLong().toULong() as T
        is Bytes -> {
          when (to) {
            TekuBLSSignature::class -> return TekuBLSSignature.fromBytes(o) as T
            TekuBytes4::class -> return TekuBytes4(o) as T
            else -> throw IllegalStateException("Type ${o::class::qualifiedName} can't be casted to ${to::class::qualifiedName}")
          }
        }
        is TekuBLSSignature -> return BLSSignature(o.toBytes()) as T
        is TekuBytes4 -> return o.wrappedBytes as T
        is BLSPubkey -> return TekuBLSPublicKey.fromBytesCompressed(o) as T
        is TekuBLSPublicKey -> return BLSPubkey(org.apache.tuweni.bytes.Bytes48.wrap(o.toBytesCompressed())) as T
        is ValidatorIndicesSSZListDelegate -> return o.delegate as T
        is TekuValidatorIndicesSSZList -> return ValidatorIndicesSSZListDelegate(o, ValidatorIndex::class) as T
        is ValidatorDelegate -> return o.v as T
        is TekuValidator -> return ValidatorDelegate(o) as T
        is ForkDelegate -> return o.v as T
        is TekuFork -> return ForkDelegate(o) as T
        is ForkDataDelegate -> return o.v as T
        is TekuForkData -> return ForkDataDelegate(o) as T
        is CheckpointDelegate -> return o.v as T
        is TekuCheckpoint -> return CheckpointDelegate(o) as T
        is AttestationDataDelegate -> return o.v as T
        is TekuAttestationData -> return AttestationDataDelegate(o) as T
        is PendingAttestationDelegate -> return o.v as T
        is TekuPendingAttestation -> return PendingAttestationDelegate(o) as T
        is Eth1DataDelegate -> return o.v as T
        is TekuEth1Data -> return Eth1DataDelegate(o) as T
        is HistoricalBatchDelegate -> return o.v as T
        is TekuHistoricalBatch -> return HistoricalBatchDelegate(o) as T
        is DepositMessageDelegate -> return o.v as T
        is TekuDepositMessage -> return DepositMessageDelegate(o) as T
        is DepositDataDelegate -> return o.v as T
        is TekuDepositData -> return DepositDataDelegate(o) as T
        is BeaconBlockHeaderDelegate -> return o.v as T
        is TekuBeaconBlockHeader -> return BeaconBlockHeaderDelegate(o) as T
        is SigningRootDelegate -> return o.v as T
        is TekuSigningRoot -> return SigningRootDelegate(o) as T
        is AttestationDelegate -> return o.v as T
        is TekuAttestation -> return AttestationDelegate(o) as T
        is IndexedAttestationDelegate -> return o.v as T
        is TekuIndexedAttestation -> return IndexedAttestationDelegate(o) as T
        is AttesterSlashingDelegate -> return o.v as T
        is TekuAttesterSlashing -> return AttesterSlashingDelegate(o) as T
        is DepositDelegate -> return o.v as T
        is TekuDeposit -> return DepositDelegate(o) as T
        is VoluntaryExitDelegate -> return o.v as T
        is TekuVoluntaryExit -> return VoluntaryExitDelegate(o) as T
        is SignedVoluntaryExitDelegate -> return o.v as T
        is TekuSignedVoluntaryExit -> return SignedVoluntaryExitDelegate(o) as T
        is SignedBeaconBlockHeaderDelegate -> return o.v as T
        is TekuSignedBeaconBlockHeader -> return SignedBeaconBlockHeaderDelegate(o) as T
        is ProposerSlashingDelegate -> return o.v as T
        is TekuProposerSlashing -> return ProposerSlashingDelegate(o) as T
        is CustodyKeyReveal -> TODO()
        is EarlyDerivedSecretReveal -> TODO()
        is ShardBlock -> TODO()
        is SignedShardBlock -> TODO()
        is ShardBlockHeader -> TODO()
        is ShardState -> TODO()
        is ShardTransition -> TODO()
        is CustodySlashing -> TODO()
        is SignedCustodySlashing -> TODO()
        is BeaconBlockBodyDelegate -> return o.v as T
        is TekuBeaconBlockBody -> return BeaconBlockBodyDelegate(o) as T
        is BeaconBlockDelegate -> return o.v as T
        is TekuBeaconBlock -> return BeaconBlockDelegate(o) as T
        is SignedBeaconBlockDelegate -> return o.v as T
        is TekuSignedBeaconBlock -> return SignedBeaconBlockDelegate(o) as T
        is CompactCommittee -> TODO()
        is BeaconStateDelegate -> return o.v as T
        is TekuBeaconState -> return BeaconStateDelegate(o) as T
        is AttestationCustodyBitWrapper -> TODO()
        is SSZBitListDelegate -> return o.delegate as T
        is TekuBitlist -> return SSZBitListDelegate(o) as T
        is SSZBitVectorDelegate -> return o.delegate as T
        is TekuBitvector -> return SSZBitVectorDelegate(o) as T
        else -> throw IllegalArgumentException("Unknown type ${o::class::qualifiedName}")
      }
    }
  }
}
