package tech.pegasys.teku.phase1.integration

import tech.pegasys.teku.phase1.integration.ssz.SSZBitListWrapper
import tech.pegasys.teku.phase1.integration.ssz.SSZBitVectorWrapper
import tech.pegasys.teku.phase1.integration.ssz.SSZByteListWrapper
import tech.pegasys.teku.phase1.integration.ssz.SSZListWrapper
import tech.pegasys.teku.phase1.integration.ssz.SSZMutableListWrapper
import tech.pegasys.teku.phase1.integration.ssz.SSZMutableVectorWrapper
import tech.pegasys.teku.phase1.integration.ssz.SSZVectorWrapper
import tech.pegasys.teku.phase1.onotole.ssz.SSZBitList
import tech.pegasys.teku.phase1.onotole.ssz.SSZBitVector
import tech.pegasys.teku.phase1.onotole.ssz.SSZList
import tech.pegasys.teku.phase1.onotole.ssz.SSZMutableList
import tech.pegasys.teku.phase1.onotole.ssz.SSZMutableVector
import tech.pegasys.teku.phase1.onotole.ssz.SSZVector
import tech.pegasys.teku.ssz.SSZTypes.Bitlist
import tech.pegasys.teku.ssz.SSZTypes.Bitvector
import kotlin.reflect.KClass
import tech.pegasys.teku.ssz.SSZTypes.SSZList as TekuSSZList
import tech.pegasys.teku.ssz.SSZTypes.SSZMutableList as TekuSSZMutableList
import tech.pegasys.teku.ssz.SSZTypes.SSZMutableVector as TekuSSZMutableVector
import tech.pegasys.teku.ssz.SSZTypes.SSZVector as TekuSSZVector

internal val SSZBitListType = object : WrappedTypePair<SSZBitList, SSZBitListWrapper, Bitlist>(
  SSZBitList::class, Bitlist::class, SSZBitListWrapper::class
) {}

internal val SSZBitVectorType =
  object : WrappedTypePair<SSZBitVector, SSZBitVectorWrapper, Bitvector>
    (SSZBitVector::class, Bitvector::class, SSZBitVectorWrapper::class) {}

internal inline class SSZListType<Onotole : Any, Teku : Any>(val type: TypePair<Onotole, Teku>) {
  fun wrap(v: TekuSSZList<Teku>) = SSZListWrapper(v, type)
  fun unwrap(v: SSZList<Onotole>) = (v as SSZListWrapper<*, Teku>).collection
}

internal class SSZByteListTypeDefinition {
  inline fun wrap(v: TekuSSZList<Byte>) = SSZByteListWrapper(v)
  inline fun unwrap(v: SSZList<Byte>) = (v as SSZByteListWrapper).collection
}

internal val SSZByteListType = SSZByteListTypeDefinition()

internal inline class SSZMutableListType<Onotole : Any, Teku : Any>(val type: TypePair<Onotole, Teku>) {
  fun wrap(v: TekuSSZMutableList<Teku>) = SSZMutableListWrapper(v, type)
  fun unwrap(v: SSZMutableList<Onotole>) = (v as SSZMutableListWrapper<*, Teku>).collection
}

internal inline class SSZVectorType<Onotole : Any, Teku : Any>(val type: TypePair<Onotole, Teku>) {
  fun wrap(v: TekuSSZVector<Teku>) = SSZVectorWrapper(v, type)
  fun unwrap(v: SSZVector<Onotole>) = (v as SSZVectorWrapper<*, Teku>).collection
}

internal inline class SSZMutableVectorType<Onotole : Any, Teku : Any>(val type: TypePair<Onotole, Teku>) {
  fun wrap(v: TekuSSZMutableVector<Teku>) = SSZMutableVectorWrapper(v, type)
  fun unwrap(v: SSZMutableVector<Onotole>) = (v as SSZMutableVectorWrapper<*, Teku>).collection
}

internal fun <Onotole : Any, Teku : Any> resolveSSZType(type: KClass<Onotole>): TypePair<Onotole, Teku>? {
  return when (type) {
    SSZBitList::class -> SSZBitListType
    SSZBitVector::class -> SSZBitVectorType
    else -> null
  } as TypePair<Onotole, Teku>?
}
