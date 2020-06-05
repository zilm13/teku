package tech.pegasys.teku.phase1.core

import com.google.common.primitives.UnsignedLong
import org.apache.tuweni.bytes.Bytes
import tech.pegasys.teku.phase1.ssz.uint64
import kotlin.reflect.KClass
import tech.pegasys.teku.bls.BLSPublicKey as TekuBLSPublicKey
import tech.pegasys.teku.bls.BLSSignature as TekuBLSSignature
import tech.pegasys.teku.datastructures.state.Fork as TekuFork
import tech.pegasys.teku.datastructures.state.ForkData as TekuForkData
import tech.pegasys.teku.datastructures.state.Validator as TekuValidator
import tech.pegasys.teku.ssz.SSZTypes.Bytes4 as TekuBytes4
import tech.pegasys.teku.ssz.SSZTypes.SSZMutableList as TekuSSZMutableList

interface TypeConverter {
  @Suppress("UNCHECKED_CAST")
  companion object {
    private val TYPE_MAP: Map<KClass<*>, KClass<*>> = mapOf()

    fun <T : Any> match(from: KClass<*>): KClass<T> {
      if (TYPE_MAP.contains(from)) {
        return TYPE_MAP[from] as KClass<T>
      }

      throw IllegalArgumentException("Unknown type ${from::class.qualifiedName}")
    }

    fun <T : Any> cast(o: Any, to: KClass<T>): T {
      if (o::class == to) {
        return o as T
      }

//      typealias boolean = Boolean
//      typealias uint8 = UByte
//      typealias uint64 = ULong
//
//      typealias Bytes = org.apache.tuweni.bytes.Bytes
//      typealias Bytes1 = Byte
//      typealias Bytes4 = org.apache.tuweni.bytes.Bytes
//      typealias Bytes32 = org.apache.tuweni.bytes.Bytes32
//      typealias Bytes48 = org.apache.tuweni.bytes.Bytes48
//      typealias Bytes96 = org.apache.tuweni.bytes.Bytes

//      typealias Slot = uint64
//      typealias Epoch = uint64
//      typealias CommitteeIndex = uint64
//      typealias ValidatorIndex = uint64
//      typealias Gwei = uint64
//      typealias Root = Bytes32
//      typealias Version = Bytes4
//      typealias DomainType = Bytes4
//      typealias ForkDigest = Bytes4
//      typealias Domain = Bytes32
//      typealias BLSPubkey = Bytes48
//      typealias BLSSignature = Bytes96
//      typealias Shard = uint64
//      typealias OnlineEpochs = uint8

//      interface ValidatorIndicesSSZList : SSZList<ValidatorIndex>
//      Fork::class,
//      ForkData::class,
//      Checkpoint::class,
//      Validator::class,
//      AttestationData::class,
//      PendingAttestation::class,
//      Eth1Data::class,
//      HistoricalBatch::class,
//      DepositMessage::class,
//      DepositData::class,
//      BeaconBlockHeader::class,
//      SigningRoot::class,
//      Attestation::class,
//      IndexedAttestation::class,
//      AttesterSlashing::class,
//      Deposit::class,
//      VoluntaryExit::class,
//      SignedVoluntaryExit::class,
//      SignedBeaconBlockHeader::class,
//      ProposerSlashing::class,
//      CustodyKeyReveal::class,
//      EarlyDerivedSecretReveal::class,
//      ShardBlock::class,
//      SignedShardBlock::class,
//      ShardBlockHeader::class,
//      ShardState::class,
//      ShardTransition::class,
//      CustodySlashing::class,
//      SignedCustodySlashing::class,
//      BeaconBlockBody::class,
//      BeaconBlock::class,
//      SignedBeaconBlock::class,
//      CompactCommittee::class,
//      BeaconState::class,
//      AttestationCustodyBitWrapper::class,
//      LatestMessage::class,
//      Store::class


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
        is TekuValidator -> return ValidatorDelegate(o) as T
        is ValidatorIndicesSSZListDelegate -> return o.data as T
        is TekuSSZMutableList<*> -> {
          when (to) {
            ValidatorIndicesSSZList::class -> return ValidatorIndicesSSZListDelegate(o as TekuSSZMutableList<UnsignedLong>, ValidatorIndex::class) as T
            else -> throw IllegalStateException("Type ${o::class::qualifiedName} can't be casted to ${to::class::qualifiedName}")
          }
        }
        is ValidatorDelegate -> return o.v as T
        is TekuFork -> TODO()
        is TekuForkData -> TODO()
        is Checkpoint -> TODO()
        is Validator -> TODO()
        is AttestationData -> TODO()
        is PendingAttestation -> TODO()
        is Eth1Data -> TODO()
        is HistoricalBatch -> TODO()
        is DepositMessage -> TODO()
        is DepositData -> TODO()
        is BeaconBlockHeader -> TODO()
        is SigningRoot -> TODO()
        is Attestation -> TODO()
        is IndexedAttestation -> TODO()
        is AttesterSlashing -> TODO()
        is Deposit -> TODO()
        is VoluntaryExit -> TODO()
        is SignedVoluntaryExit -> TODO()
        is SignedBeaconBlockHeader -> TODO()
        is ProposerSlashing -> TODO()
        is CustodyKeyReveal -> TODO()
        is EarlyDerivedSecretReveal -> TODO()
        is ShardBlock -> TODO()
        is SignedShardBlock -> TODO()
        is ShardBlockHeader -> TODO()
        is ShardState -> TODO()
        is ShardTransition -> TODO()
        is CustodySlashing -> TODO()
        is SignedCustodySlashing -> TODO()
        is BeaconBlockBody -> TODO()
        is BeaconBlock -> TODO()
        is SignedBeaconBlock -> TODO()
        is CompactCommittee -> TODO()
        is BeaconState -> TODO()
        is AttestationCustodyBitWrapper -> TODO()
        is LatestMessage -> TODO()
        is Store -> TODO()
        else -> throw IllegalArgumentException("Unknown type ${o::class::qualifiedName}")
      }
    }
  }
}
