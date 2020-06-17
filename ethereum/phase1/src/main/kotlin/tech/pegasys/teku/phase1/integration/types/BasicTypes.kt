package tech.pegasys.teku.phase1.integration.types

import com.google.common.primitives.UnsignedLong
import org.apache.tuweni.bytes.Bytes48
import tech.pegasys.teku.phase1.onotole.phase1.BLSPubkey
import tech.pegasys.teku.phase1.onotole.phase1.BLSSignature
import tech.pegasys.teku.phase1.onotole.phase1.Domain
import tech.pegasys.teku.phase1.onotole.ssz.Bytes
import tech.pegasys.teku.phase1.onotole.ssz.Bytes32
import tech.pegasys.teku.phase1.onotole.ssz.Bytes4
import tech.pegasys.teku.phase1.onotole.ssz.boolean
import tech.pegasys.teku.phase1.onotole.ssz.uint64
import tech.pegasys.teku.phase1.onotole.ssz.uint8
import kotlin.reflect.KClass
import org.apache.tuweni.bytes.Bytes as TekuBytes
import org.apache.tuweni.bytes.Bytes32 as TekuBytes32
import tech.pegasys.teku.bls.BLSPublicKey as TekuBLSPublicKey
import tech.pegasys.teku.bls.BLSSignature as TekuBLSSignature
import tech.pegasys.teku.ssz.SSZTypes.Bytes4 as TekuBytes4

internal interface BasicType<Onotole : Any, Teku : Any> :
  TypePair<Onotole, Teku>

internal val BooleanType = object :
  BasicType<boolean, Boolean> {
  override val teku = Boolean::class
  override val onotole = boolean::class
  inline override fun wrap(v: Boolean) = v
  inline override fun unwrap(v: boolean) = v
}

internal val UInt8Type = object :
  BasicType<uint8, Byte> {
  override val teku = Byte::class
  override val onotole = uint8::class
  inline override fun wrap(v: Byte) = v.toUByte()
  inline override fun unwrap(v: uint8) = v.toByte()
}

internal val UInt64Type = object :
  BasicType<uint64, UnsignedLong> {
  override val teku = UnsignedLong::class
  override val onotole = uint64::class
  inline override fun wrap(v: UnsignedLong) = v.toLong().toULong()
  inline override fun unwrap(v: uint64) = UnsignedLong.valueOf(v.toLong())
}

internal val ByteType = object :
  BasicType<Byte, Byte> {
  override val teku = Byte::class
  override val onotole = Byte::class
  inline override fun wrap(v: Byte) = v
  inline override fun unwrap(v: Byte) = v
}

internal val Bytes4Type = object :
  BasicType<Bytes4, TekuBytes4> {
  override val teku = TekuBytes4::class
  override val onotole = Bytes4::class
  inline override fun wrap(v: TekuBytes4) = v
  inline override fun unwrap(v: Bytes4) = v
}

internal val Bytes32Type = object :
  BasicType<Bytes32, TekuBytes32> {
  override val teku = TekuBytes32::class
  override val onotole = Bytes32::class
  inline override fun wrap(v: TekuBytes32) = v
  inline override fun unwrap(v: Bytes32) = v
}

internal val BytesType = object :
  BasicType<Bytes, TekuBytes> {
  override val teku = TekuBytes::class
  override val onotole = Bytes::class
  inline override fun wrap(v: TekuBytes) = v
  inline override fun unwrap(v: Bytes) = v
}

internal val BLSPublicKeyType = object :
  BasicType<BLSPubkey, TekuBLSPublicKey> {
  override val teku = TekuBLSPublicKey::class
  override val onotole = BLSPubkey::class
  override fun wrap(v: TekuBLSPublicKey) = BLSPubkey(Bytes48.wrap(v.toBytesCompressed()))
  override fun unwrap(v: BLSPubkey) = TekuBLSPublicKey.fromBytesCompressed(v)
}

internal val BLSSignatureType = object :
  BasicType<BLSSignature, TekuBLSSignature> {
  override val teku = TekuBLSSignature::class
  override val onotole = BLSSignature::class
  inline override fun wrap(v: TekuBLSSignature) = BLSSignature(v.toBytes())
  inline override fun unwrap(v: BLSSignature) = TekuBLSSignature.fromBytes(v.wrappedBytes)
}

internal val DomainTypePair = object :
  BasicType<Domain, Bytes> {
  override val teku = TekuBytes::class
  override val onotole = Domain::class
  inline override fun wrap(v: Bytes) = Domain(Bytes32.wrap(v))
  inline override fun unwrap(v: Domain) = v
}

internal fun <Onotole : Any, Teku : Any> resolveBasicType(type: KClass<Onotole>): TypePair<Onotole, Teku>? {
  return when (type) {
    uint8::class -> UInt8Type
    uint64::class -> UInt64Type
    boolean::class -> BooleanType
    BLSSignature::class -> BLSSignatureType
    BLSPubkey::class -> BLSPublicKeyType
    Bytes32::class -> Bytes32Type
    Bytes4::class -> Bytes4Type
    Bytes::class -> BytesType
    Domain::class -> DomainTypePair
    else -> null
  } as TypePair<Onotole, Teku>?
}

internal fun isBasicTypeValue(v: Any): Boolean {
  return when (v) {
    is Byte -> true
    is UnsignedLong -> true
    is TekuBLSSignature -> true
    is TekuBLSPublicKey -> true
    is uint8 -> true
    is uint64 -> true
    is boolean -> true
    is BLSSignature -> true
    is BLSPubkey -> true
    is Bytes32 -> true
    is Bytes4 -> true
    is Bytes -> true
    else -> false
  }
}
