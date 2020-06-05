package tech.pegasys.teku.phase1.core

import com.google.common.primitives.UnsignedLong
import org.apache.tuweni.bytes.Bytes
import org.apache.tuweni.ssz.SSZ
import tech.pegasys.teku.phase1.core.TypeConverter.Companion.cast
import tech.pegasys.teku.phase1.ssz.Bytes1
import tech.pegasys.teku.phase1.ssz.Bytes32
import tech.pegasys.teku.phase1.ssz.SSZBitList
import tech.pegasys.teku.phase1.ssz.SSZBitVector
import tech.pegasys.teku.phase1.ssz.SSZByteList
import tech.pegasys.teku.phase1.ssz.SSZList
import tech.pegasys.teku.phase1.ssz.SSZMutableCollection
import tech.pegasys.teku.phase1.ssz.SSZObjectFactory
import tech.pegasys.teku.phase1.ssz.SSZVector
import tech.pegasys.teku.ssz.SSZTypes.Bitlist
import tech.pegasys.teku.ssz.SSZTypes.Bitvector
import tech.pegasys.teku.util.hashtree.HashTreeUtil
import java.util.stream.Collectors
import kotlin.reflect.KClass
import tech.pegasys.teku.ssz.SSZTypes.SSZList as TekuSSZList
import tech.pegasys.teku.ssz.SSZTypes.SSZMutableCollection as TekuSSZMutableCollection
import tech.pegasys.teku.ssz.SSZTypes.SSZMutableList as TekuSSZMutableList
import tech.pegasys.teku.ssz.SSZTypes.SSZMutableVector as TekuSSZMutableVector
import tech.pegasys.teku.ssz.SSZTypes.SSZVector as TekuSSZVector

abstract class SSZMutableCollectionDelegate<Onotole : Any, Teku : Any>(
    override final val type: KClass<Onotole>
) : SSZMutableCollection<Onotole> {
  internal val tekuType: KClass<Teku> = TypeConverter.match(type)
  internal abstract val data: TekuSSZMutableCollection<Teku>

  override val size: Int = data.size()
  override fun contains(element: Onotole): Boolean = data.contains(cast(element, tekuType))
  override fun get(index: ULong): Onotole {
    val item = cast(data[index.toInt()], type)
    if (item is Mutable<*>) {
      item.callback = { value -> this[index] = value as Onotole }
    }
    return item
  }

  override fun indexOf(element: Onotole) = data.indexOf(cast(element, tekuType))
  override fun isEmpty() = data.isEmpty
  override fun iterator(): MutableIterator<Onotole> = object : MutableIterator<Onotole> {
    private val iterator = data.iterator()
    override fun hasNext() = iterator.hasNext()
    override fun next() = cast(iterator.next(), type)
    override fun remove() = iterator.remove()
  }

  override fun lastIndexOf(element: Onotole) = data.lastIndexOf(cast(element, tekuType))
  override fun subList(fromIndex: Int, toIndex: Int): List<Onotole> {
    return data.stream()
        .skip(fromIndex.toLong())
        .limit((toIndex - fromIndex + 1).toLong())
        .map { cast(it, type) }
        .collect(Collectors.toList())
  }

  override operator fun set(index: ULong, item: Onotole): Onotole {
    val oldItem = data[index.toInt()]
    data.set(index.toInt(), cast(item, tekuType))
    return cast(oldItem, type)
  }

  override fun containsAll(elements: Collection<Onotole>) = TODO("Not yet implemented")
  override fun listIterator(): MutableListIterator<Onotole> = TODO("Not yet implemented")
  override fun listIterator(index: Int): MutableListIterator<Onotole> = TODO("Not yet implemented")
}

open class SSZListDelegate<Onotole : Any, Teku : Any>(
    override val data: TekuSSZMutableList<Teku>,
    type: KClass<Onotole>
) : SSZList<Onotole>, SSZMutableCollectionDelegate<Onotole, Teku>(type) {

  constructor(items: MutableList<Onotole>, maxSize: ULong, type: KClass<Onotole>)
      : this(
      TekuSSZList.createMutable(
          items.map { cast(it, TypeConverter.match<Teku>(type)) }.toList(),
          maxSize.toLong(),
          TypeConverter.match<Teku>(type).java),
      type)

  override val maxSize: ULong
    get() = data.maxSize.toULong()

  override fun append(item: Onotole) {
    data.add(cast(item, tekuType))
  }

  override fun hash_tree_root(): Bytes32 {
    return when (tekuType) {
      UnsignedLong::class ->
        HashTreeUtil.hash_tree_root_list_ul(
            (data as TekuSSZList<UnsignedLong>)
                .map(Bytes::class.java) { SSZ.encodeUInt64(it.toLong()) }
        )
      org.apache.tuweni.bytes.Bytes32::class ->
        HashTreeUtil.hash_tree_root_list_bytes(
            data as TekuSSZList<org.apache.tuweni.bytes.Bytes32>
        )
      else ->
        HashTreeUtil.hash_tree_root(HashTreeUtil.SSZTypes.LIST_OF_COMPOSITE, data)
    }
  }
}

class SSZBitListDelegate(items: MutableList<Boolean>, override val maxSize: ULong) : SSZBitList {
  override val type: KClass<Boolean> = Boolean::class
  private val data: Bitlist

  init {
    data = Bitlist(items.size, maxSize.toLong())
    items.forEachIndexed { i, bit -> if (bit) data.setBit(i) }
  }

  override val size: Int = data.currentSize
  override fun get(index: ULong): Boolean = data.getBit(index.toInt())
  override fun isEmpty() = size == 0
  override fun set(index: ULong, element: Boolean): Boolean {
    val oldItem = this[index.toInt()]
    data.setBit(index.toInt())
    return oldItem
  }

  override fun hash_tree_root() = HashTreeUtil.hash_tree_root_bitlist(data)

  override fun append(item: Boolean) = TODO("Not yet implemented")
  override fun contains(element: Boolean) = TODO("Not yet implemented")
  override fun indexOf(element: Boolean) = TODO("Not yet implemented")
  override fun lastIndexOf(element: Boolean) = TODO("Not yet implemented")
  override fun iterator() = TODO("Not yet implemented")
  override fun containsAll(elements: Collection<Boolean>) = TODO("Not yet implemented")
  override fun listIterator(): MutableListIterator<Boolean> = TODO("Not yet implemented")
  override fun listIterator(index: Int): MutableListIterator<Boolean> = TODO("Not yet implemented")
  override fun subList(fromIndex: Int, toIndex: Int) = TODO("Not yet implemented")
}

class SSZByteListDelegate(items: MutableList<Byte>, maxSize: ULong)
  : SSZListDelegate<Bytes1, Byte>(items, maxSize, Bytes1::class), SSZByteList

class SSZVectorDelegate<Onotole : Any, Teku : Any>(
    override val data: TekuSSZMutableVector<Teku>,
    type: KClass<Onotole>
) : SSZVector<Onotole>, SSZMutableCollectionDelegate<Onotole, Teku>(type) {

  constructor(items: MutableList<Onotole>, type: KClass<Onotole>)
      : this(
      TekuSSZVector.createMutable(
          items.map { cast(it, TypeConverter.match<Teku>(type)) }.toList(),
          TypeConverter.match<Teku>(type).java
      ),
      type)

  override fun hash_tree_root(): Bytes32 {
    return when (tekuType) {
      UnsignedLong::class -> {
        val tekuList = data as TekuSSZVector<UnsignedLong>
        HashTreeUtil.hash_tree_root_vector_unsigned_long(tekuList)
      }
      else ->
        HashTreeUtil.hash_tree_root(HashTreeUtil.SSZTypes.VECTOR_OF_COMPOSITE, data)
    }
  }
}

class SSZBitVectorDelegate(items: MutableList<Boolean>) : SSZBitVector {
  private val data: Bitvector
  override val type: KClass<Boolean> = Boolean::class

  init {
    data = Bitvector(items.size)
    items.forEachIndexed { i, bit -> if (bit) data.setBit(i) }
  }

  override val size: Int = data.size
  override fun get(index: ULong): Boolean = data.getBit(index.toInt())
  override fun isEmpty() = size == 0
  override fun set(index: ULong, element: Boolean): Boolean {
    val oldItem = this[index.toInt()]
    data.setBit(index.toInt())
    return oldItem
  }

  override fun hash_tree_root() = HashTreeUtil.hash_tree_root_bitvector(data)

  override fun contains(element: Boolean) = TODO("Not yet implemented")
  override fun indexOf(element: Boolean) = TODO("Not yet implemented")
  override fun lastIndexOf(element: Boolean) = TODO("Not yet implemented")
  override fun iterator() = TODO("Not yet implemented")
  override fun containsAll(elements: Collection<Boolean>) = TODO("Not yet implemented")
  override fun listIterator(): MutableListIterator<Boolean> = TODO("Not yet implemented")
  override fun listIterator(index: Int): MutableListIterator<Boolean> = TODO("Not yet implemented")
  override fun subList(fromIndex: Int, toIndex: Int) = TODO("Not yet implemented")
}

fun <Onotole : Any, Teku : Any> SSZMutableCollection<Onotole>.copyTo(
    to: TekuSSZMutableCollection<Teku>
) {
  val tekuType = TypeConverter.match<Teku>(this.type)
  this.forEachIndexed { i, item -> to.set(i, cast(item, tekuType)) }
}

class TekuSSZFactory : SSZObjectFactory {
  override fun <T : Any> SSZList(type: KClass<T>, maxSize: ULong, items: MutableList<T>) = SSZListDelegate<T, Any>(items, maxSize, type)
  override fun <T : Any> SSZVector(type: KClass<T>, items: MutableList<T>) = SSZVectorDelegate<T, Any>(items, type)
  override fun SSZByteList(maxSize: ULong, items: MutableList<Byte>) = SSZByteListDelegate(items, maxSize)
  override fun SSZBitList(maxSize: ULong, items: MutableList<Boolean>) = SSZBitListDelegate(items, maxSize)
  override fun SSZBitVector(items: MutableList<Boolean>) = SSZBitVectorDelegate(items)
}
