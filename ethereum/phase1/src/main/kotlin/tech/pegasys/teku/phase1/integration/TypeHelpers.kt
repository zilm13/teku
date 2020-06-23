package tech.pegasys.teku.phase1.integration

import com.google.common.primitives.UnsignedLong
import org.apache.tuweni.bytes.Bytes48
import tech.pegasys.teku.phase1.integration.ssz.SSZAbstractCollection
import tech.pegasys.teku.phase1.onotole.phase1.BLSPubkey
import tech.pegasys.teku.phase1.onotole.phase1.BLSSignature
import tech.pegasys.teku.phase1.onotole.ssz.Bytes
import tech.pegasys.teku.phase1.onotole.ssz.Bytes32
import tech.pegasys.teku.phase1.onotole.ssz.Bytes4
import tech.pegasys.teku.phase1.onotole.ssz.Bytes96
import tech.pegasys.teku.phase1.onotole.ssz.boolean
import tech.pegasys.teku.phase1.onotole.ssz.uint64
import tech.pegasys.teku.phase1.onotole.ssz.uint8
import tech.pegasys.teku.ssz.backing.VectorViewRead
import tech.pegasys.teku.ssz.backing.ViewRead
import tech.pegasys.teku.ssz.backing.type.BasicViewTypes
import tech.pegasys.teku.ssz.backing.type.VectorViewType
import tech.pegasys.teku.ssz.backing.view.BasicViews
import tech.pegasys.teku.ssz.backing.view.ViewUtils

val Bytes48Type = object : VectorViewType<BasicViews.ByteView>(BasicViewTypes.BYTE_TYPE, 48) {}
val Bytes96Type = object : VectorViewType<BasicViews.ByteView>(BasicViewTypes.BYTE_TYPE, 96) {}

fun UnsignedLong.toUInt64() = this.toLong().toULong()
fun uint64.toUnsignedLong() = UnsignedLong.valueOf(this.toLong())

fun <T : Any> wrapValues(vararg values: T): Array<ViewRead> {
  return values.map { wrapValue(it) }.toTypedArray()
}

fun wrapValue(value: Any): ViewRead {
  return when (value) {
    is SSZAbstractCollection<*, *> -> value.view
    is ViewRead -> value
    else -> wrapBasicValue(value)
  }
}

inline fun <reified T> wrapBasicValue(value: Any): T {
  return when (value) {
    is uint8 -> BasicViews.ByteView(value.toByte())
    is uint64 -> BasicViews.UInt64View(value.toUnsignedLong())
    is boolean -> BasicViews.BitView(value)
    is BLSSignature -> ViewUtils.createVectorFromBytes(value.wrappedBytes)
    is BLSPubkey -> ViewUtils.createVectorFromBytes(value)
    is Bytes32 -> BasicViews.Bytes32View(value)
    is Bytes4 -> BasicViews.Bytes4View(value)
    is Bytes -> ViewUtils.createVectorFromBytes(value)
    else -> throw IllegalArgumentException("Unsupported type ${value::class.qualifiedName}")
  } as T
}

@Suppress("IMPLICIT_CAST_TO_ANY", "UNCHECKED_CAST")
inline fun <reified T> getBasicValue(view: ViewRead): T {
  return when (T::class) {
    uint8::class -> (view as BasicViews.ByteView).get().toUByte()
    uint64::class -> (view as BasicViews.UInt64View).get().toUInt64()
    boolean::class -> (view as BasicViews.BitView).get()
    BLSSignature::class -> Bytes96(ViewUtils.getAllBytes(view as VectorViewRead<BasicViews.ByteView>))
    BLSPubkey::class -> Bytes48.wrap(ViewUtils.getAllBytes(view as VectorViewRead<BasicViews.ByteView>))
    Bytes32::class -> (view as BasicViews.Bytes32View).get()
    Bytes4::class -> (view as BasicViews.Bytes4View).get()
    Bytes::class -> ViewUtils.getAllBytes(view as VectorViewRead<BasicViews.ByteView>)
    else -> throw IllegalArgumentException("Unsupported type ${T::class.qualifiedName}")
  } as T
}
