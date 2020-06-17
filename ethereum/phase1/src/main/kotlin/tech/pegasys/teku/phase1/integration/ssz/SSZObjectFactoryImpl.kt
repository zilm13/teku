package tech.pegasys.teku.phase1.integration.ssz

import tech.pegasys.teku.phase1.integration.types.TypePair
import tech.pegasys.teku.phase1.integration.types.resolveBasicType
import tech.pegasys.teku.phase1.integration.types.resolveCompositeType
import tech.pegasys.teku.phase1.integration.types.resolveSSZType
import tech.pegasys.teku.phase1.onotole.ssz.SSZBitList
import tech.pegasys.teku.phase1.onotole.ssz.SSZBitVector
import tech.pegasys.teku.phase1.onotole.ssz.SSZByteList
import tech.pegasys.teku.phase1.onotole.ssz.SSZMutableList
import tech.pegasys.teku.phase1.onotole.ssz.SSZMutableVector
import tech.pegasys.teku.phase1.onotole.ssz.SSZObjectFactory
import kotlin.reflect.KClass

class SSZObjectFactoryImpl : SSZObjectFactory {
  override fun <T : Any> SSZList(
    type: KClass<T>,
    maxSize: ULong,
    items: MutableList<T>
  ): SSZMutableList<T> = SSZMutableListWrapper(items, maxSize, resolveType<T, Any>(type))

  override fun <T : Any> SSZVector(type: KClass<T>, items: MutableList<T>): SSZMutableVector<T> =
    SSZMutableVectorWrapper(items, resolveType<T, Any>(type))

  override fun SSZByteList(maxSize: ULong, items: MutableList<Byte>): SSZByteList =
    SSZByteListWrapper(items, maxSize)

  override fun SSZBitList(maxSize: ULong, items: MutableList<Boolean>): SSZBitList =
    SSZBitListWrapper(items, maxSize)

  override fun SSZBitVector(items: MutableList<Boolean>): SSZBitVector =
    SSZBitVectorWrapper(items)

  private fun <Onotole : Any, Teku : Any> resolveType(type: KClass<Onotole>): TypePair<Onotole, Teku> {
    return resolveBasicType(type)
      ?: resolveSSZType(type)
      ?: resolveCompositeType(type)
    ?: throw IllegalArgumentException("Can't resolve given type ${type.qualifiedName}")
  }
}
