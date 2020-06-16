package tech.pegasys.teku.phase1.integration

import tech.pegasys.teku.phase1.integration.ssz.SSZBitListWrapper
import tech.pegasys.teku.phase1.integration.ssz.SSZBitVectorWrapper
import tech.pegasys.teku.phase1.onotole.ssz.SSZBitList
import tech.pegasys.teku.phase1.onotole.ssz.SSZBitVector
import tech.pegasys.teku.ssz.SSZTypes.Bitlist
import tech.pegasys.teku.ssz.SSZTypes.Bitvector
import kotlin.reflect.KClass

internal val SSZBitListType = object : WrappedTypePair<SSZBitList, SSZBitListWrapper, Bitlist>(
  SSZBitList::class, Bitlist::class
) {}

internal val SSZBitVectorType =
  object : WrappedTypePair<SSZBitVector, SSZBitVectorWrapper, Bitvector>
    (SSZBitVector::class, Bitvector::class) {}


internal fun <Onotole : Any, Teku : Any> resolveSSZType(type: KClass<Onotole>): TypePair<Onotole, Teku>? {
  return when (type) {
    SSZBitList::class -> SSZBitListType
    SSZBitVector::class -> SSZBitVectorType
    else -> null
  } as TypePair<Onotole, Teku>?
}
