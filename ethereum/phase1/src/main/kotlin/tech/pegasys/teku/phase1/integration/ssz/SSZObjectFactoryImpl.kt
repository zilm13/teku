package tech.pegasys.teku.phase1.integration.ssz

import tech.pegasys.teku.phase1.integration.TypePair
import tech.pegasys.teku.phase1.integration.resolveBasicType
import tech.pegasys.teku.phase1.integration.resolveCompositeType
import tech.pegasys.teku.phase1.integration.resolveSSZType
import tech.pegasys.teku.phase1.onotole.ssz.SSZMutableList
import tech.pegasys.teku.phase1.onotole.ssz.SSZObjectFactory
import kotlin.reflect.KClass

class SSZObjectFactoryImpl : SSZObjectFactory {
  override fun <T : Any> SSZList(
    type: KClass<T>,
    maxSize: ULong,
    items: MutableList<T>
  ): SSZMutableList<T> = SSZMutableListWrapper(items, maxSize, resolveType<T, Any>(type))

  override fun <T : Any> SSZVector(type: KClass<T>, items: MutableList<T>) =
    SSZMutableVectorWrapper(items, resolveType<T, Any>(type))

  override fun SSZByteList(maxSize: ULong, items: MutableList<Byte>) =
    SSZByteListWrapper(items, maxSize)

  override fun SSZBitList(maxSize: ULong, items: MutableList<Boolean>) =
    SSZBitListWrapper(items, maxSize)

  override fun SSZBitVector(items: MutableList<Boolean>) =
    SSZBitVectorWrapper(items)

  private fun <Onotole : Any, Teku : Any> resolveType(type: KClass<Onotole>): TypePair<Onotole, Teku> {
    return resolveBasicType(type) ?: resolveSSZType(type) ?: resolveCompositeType(type)
    ?: throw IllegalArgumentException("Can't resolve given type ${type.qualifiedName}")
  }
}
