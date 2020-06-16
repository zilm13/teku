package tech.pegasys.teku.phase1.integration.ssz

import com.google.common.primitives.UnsignedLong
import org.apache.tuweni.bytes.Bytes
import org.apache.tuweni.ssz.SSZ
import tech.pegasys.teku.phase1.integration.TypePair
import tech.pegasys.teku.phase1.integration.datastructures.Mutable
import tech.pegasys.teku.phase1.integration.datastructures.Wrapper
import tech.pegasys.teku.phase1.onotole.ssz.Bytes1
import tech.pegasys.teku.phase1.onotole.ssz.Bytes32
import tech.pegasys.teku.phase1.onotole.ssz.SSZBitList
import tech.pegasys.teku.phase1.onotole.ssz.SSZBitVector
import tech.pegasys.teku.phase1.onotole.ssz.SSZByteList
import tech.pegasys.teku.phase1.onotole.ssz.SSZImmutableCollection
import tech.pegasys.teku.phase1.onotole.ssz.SSZList
import tech.pegasys.teku.phase1.onotole.ssz.SSZMutableCollection
import tech.pegasys.teku.phase1.onotole.ssz.SSZMutableList
import tech.pegasys.teku.phase1.onotole.ssz.SSZMutableVector
import tech.pegasys.teku.phase1.onotole.ssz.SSZVector
import tech.pegasys.teku.ssz.SSZTypes.Bitlist
import tech.pegasys.teku.ssz.SSZTypes.Bitvector
import tech.pegasys.teku.util.hashtree.HashTreeUtil
import java.util.stream.Collectors
import tech.pegasys.teku.ssz.SSZTypes.SSZImmutableCollection as TekuSSZImmutableCollection
import tech.pegasys.teku.ssz.SSZTypes.SSZList as TekuSSZList
import tech.pegasys.teku.ssz.SSZTypes.SSZMutableCollection as TekuSSZMutableCollection
import tech.pegasys.teku.ssz.SSZTypes.SSZMutableList as TekuSSZMutableList
import tech.pegasys.teku.ssz.SSZTypes.SSZMutableVector as TekuSSZMutableVector
import tech.pegasys.teku.ssz.SSZTypes.SSZVector as TekuSSZVector

abstract class SSZImmutableCollectionWrapper<Onotole : Any, Teku : Any>(
  internal val type: TypePair<Onotole, Teku>
) : SSZImmutableCollection<Onotole> {
  internal abstract val collection: TekuSSZImmutableCollection<Teku>

  override val size: Int
    get() = collection.size()

  override fun contains(element: Onotole): Boolean = collection.contains(type.unwrap(element))
  override fun get(index: ULong): Onotole {
    return type.wrap(collection[index.toInt()])
  }

  override fun indexOf(element: Onotole) = collection.indexOf(type.unwrap(element))
  override fun isEmpty() = collection.isEmpty
  override fun iterator(): MutableIterator<Onotole> = object : MutableIterator<Onotole> {
    private val iterator = collection.iterator()
    override fun hasNext() = iterator.hasNext()
    override fun next() = type.wrap(iterator.next())
    override fun remove() = iterator.remove()
  }

  override fun lastIndexOf(element: Onotole) = collection.lastIndexOf(type.unwrap(element))
  override fun subList(fromIndex: Int, toIndex: Int): List<Onotole> {
    return collection.stream()
      .skip(fromIndex.toLong())
      .limit((toIndex - fromIndex + 1).toLong())
      .map { type.wrap(it) }
      .collect(Collectors.toList())
  }

  override fun containsAll(elements: Collection<Onotole>) = TODO("Not yet implemented")
  override fun listIterator(): MutableListIterator<Onotole> = TODO("Not yet implemented")
  override fun listIterator(index: Int): MutableListIterator<Onotole> = TODO("Not yet implemented")
}

abstract class SSZMutableCollectionWrapper<Onotole : Any, Teku : Any>(
  internal val type: TypePair<Onotole, Teku>
) : SSZMutableCollection<Onotole> {
  internal abstract val collection: TekuSSZMutableCollection<Teku>

  override val size: Int
    get() = collection.size()

  override fun contains(element: Onotole): Boolean = collection.contains(type.unwrap(element))
  override fun get(index: ULong): Onotole {
    val item = type.wrap(collection[index.toInt()])
    if (item is Mutable<*>) {
      item.callback = { value -> this[index] = value as Onotole }
    }
    return item
  }

  override fun indexOf(element: Onotole) = collection.indexOf(type.unwrap(element))
  override fun isEmpty() = collection.isEmpty
  override fun iterator(): MutableIterator<Onotole> = object : MutableIterator<Onotole> {
    private val iterator = collection.iterator()
    override fun hasNext() = iterator.hasNext()
    override fun next() = type.wrap(iterator.next())
    override fun remove() = iterator.remove()
  }

  override fun lastIndexOf(element: Onotole) = collection.lastIndexOf(type.unwrap(element))
  override fun subList(fromIndex: Int, toIndex: Int): List<Onotole> {
    return collection.stream()
      .skip(fromIndex.toLong())
      .limit((toIndex - fromIndex + 1).toLong())
      .map { type.wrap(it) }
      .collect(Collectors.toList())
  }

  override operator fun set(index: ULong, item: Onotole): Onotole {
    val oldItem = collection[index.toInt()]
    collection.set(index.toInt(), type.unwrap(item))
    return type.wrap(oldItem)
  }

  override fun containsAll(elements: Collection<Onotole>) = TODO("Not yet implemented")
  override fun listIterator(): MutableListIterator<Onotole> = TODO("Not yet implemented")
  override fun listIterator(index: Int): MutableListIterator<Onotole> = TODO("Not yet implemented")
}

open class SSZListWrapper<Onotole : Any, Teku : Any>(
  override val collection: TekuSSZList<Teku>,
  type: TypePair<Onotole, Teku>
) : SSZList<Onotole>, SSZImmutableCollectionWrapper<Onotole, Teku>(type) {

  constructor(items: MutableList<Onotole>, maxSize: ULong, type: TypePair<Onotole, Teku>)
      : this(
    TekuSSZList.createMutable(
      items.map { type.unwrap(it) }.toList(),
      maxSize.toLong(),
      type.teku.java
    ), type
  )

  override val maxSize: ULong
    get() = collection.maxSize.toULong()

  override fun hash_tree_root(): Bytes32 {
    return when (type.teku) {
      UnsignedLong::class ->
        HashTreeUtil.hash_tree_root_list_ul(
          (collection as TekuSSZList<UnsignedLong>)
            .map(Bytes::class.java) { SSZ.encodeUInt64(it.toLong()) }
        )
      org.apache.tuweni.bytes.Bytes32::class ->
        HashTreeUtil.hash_tree_root_list_bytes(
          collection as TekuSSZList<org.apache.tuweni.bytes.Bytes32>
        )
      else ->
        HashTreeUtil.hash_tree_root(HashTreeUtil.SSZTypes.LIST_OF_COMPOSITE, collection)
    }
  }

  override fun equals(other: Any?): Boolean {
    if (other is SSZMutableListWrapper<*, *>) {
      return other.collection == collection
    }
    return false
  }

  override fun hashCode(): Int {
    return collection.hashCode()
  }

  override fun toString(): String {
    return collection.toString()
  }
}

open class SSZMutableListWrapper<Onotole : Any, Teku : Any>(
  override val collection: TekuSSZMutableList<Teku>,
  type: TypePair<Onotole, Teku>
) : SSZMutableList<Onotole>, SSZMutableCollectionWrapper<Onotole, Teku>(type) {

  constructor(
    items: MutableList<Onotole>,
    maxSize: ULong,
    type: TypePair<Onotole, Teku>
  ) : this(
    TekuSSZList.createMutable(
      items.map { type.unwrap(it) }.toList(),
      maxSize.toLong(),
      type.teku.java
    ), type
  )

  override val maxSize: ULong
    get() = collection.maxSize.toULong()

  override fun append(item: Onotole) {
    collection.add(type.unwrap(item))
  }

  override fun hash_tree_root(): Bytes32 {
    return when (collection.elementType) {
      UnsignedLong::class.java ->
        HashTreeUtil.hash_tree_root_list_ul(
          (collection as TekuSSZList<UnsignedLong>)
            .map(Bytes::class.java) { SSZ.encodeUInt64(it.toLong()) }
        )
      org.apache.tuweni.bytes.Bytes32::class.java ->
        HashTreeUtil.hash_tree_root_list_bytes(
          collection as TekuSSZList<org.apache.tuweni.bytes.Bytes32>
        )
      else ->
        HashTreeUtil.hash_tree_root(HashTreeUtil.SSZTypes.LIST_OF_COMPOSITE, collection)
    }
  }

  override fun equals(other: Any?): Boolean {
    if (other is SSZMutableListWrapper<*, *>) {
      return other.collection == collection
    }
    return false
  }

  override fun hashCode(): Int {
    return collection.hashCode()
  }

  override fun toString(): String {
    return collection.toString()
  }
}

class SSZBitListWrapper(override val v: Bitlist) : Wrapper<Bitlist>, SSZBitList {

  constructor(items: MutableList<Boolean>, maxSize: ULong) : this(
    MutableList(1) { items }.map<MutableList<Boolean>, Bitlist> { list ->
      val data = Bitlist(items.size, maxSize.toLong())
      list.forEachIndexed { i, bit -> if (bit) data.setBit(i) }
      data
    }.first()
  )

  override val maxSize: ULong
    get() = v.maxSize.toULong()
  override val size: Int = v.currentSize
  override fun get(index: ULong): Boolean = v.getBit(index.toInt())
  override fun isEmpty() = size == 0
  override fun set(index: ULong, element: Boolean): Boolean {
    val oldItem = this[index.toInt()]
    v.setBit(index.toInt())
    return oldItem
  }

  override fun hash_tree_root() = HashTreeUtil.hash_tree_root_bitlist(v)

  override fun equals(other: Any?): Boolean {
    if (other is SSZBitListWrapper) {
      return other.v == v
    }
    return false
  }

  override fun hashCode(): Int {
    return v.hashCode()
  }

  override fun toString(): String {
    return v.toString()
  }

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

class SSZByteListWrapper(list: TekuSSZList<Byte>) :
  SSZListWrapper<Bytes1, Byte>(list, object : TypePair<Byte, Byte> {
    override val teku = Byte::class
    override val onotole = Byte::class
    inline override fun wrap(v: Byte) = v
    inline override fun unwrap(v: Byte) = v
  }), SSZByteList {

  constructor(items: MutableList<Byte>, maxSize: ULong) : this(
    TekuSSZList.createMutable(items, maxSize.toLong(), Byte::class.java)
  )
}

class SSZVectorWrapper<Onotole : Any, Teku : Any>(
  override val collection: TekuSSZVector<Teku>,
  type: TypePair<Onotole, Teku>
) : SSZVector<Onotole>, SSZImmutableCollectionWrapper<Onotole, Teku>(type) {

  constructor(items: MutableList<Onotole>, type: TypePair<Onotole, Teku>)
      : this(
    TekuSSZVector.createMutable(items.map { type.unwrap(it) }.toList(), type.teku.java),
    type
  )

  override fun hash_tree_root(): Bytes32 {
    return when (type.teku) {
      UnsignedLong::class -> {
        val tekuList = collection as TekuSSZVector<UnsignedLong>
        HashTreeUtil.hash_tree_root_vector_unsigned_long(tekuList)
      }
      else ->
        HashTreeUtil.hash_tree_root(HashTreeUtil.SSZTypes.VECTOR_OF_COMPOSITE, collection)
    }
  }

  override fun equals(other: Any?): Boolean {
    if (other is SSZMutableVectorWrapper<*, *>) {
      return other.collection == collection
    }
    return false
  }

  override fun hashCode(): Int {
    return collection.hashCode()
  }

  override fun toString(): String {
    return collection.toString()
  }
}

class SSZMutableVectorWrapper<Onotole : Any, Teku : Any>(
  override val collection: TekuSSZMutableVector<Teku>,
  type: TypePair<Onotole, Teku>
) : SSZMutableVector<Onotole>, SSZMutableCollectionWrapper<Onotole, Teku>(type) {

  constructor(items: MutableList<Onotole>, type: TypePair<Onotole, Teku>)
      : this(
    TekuSSZVector.createMutable(items.map { type.unwrap(it) }.toList(), type.teku.java),
    type
  )

  override fun hash_tree_root(): Bytes32 {
    return when (type.teku) {
      UnsignedLong::class -> {
        val tekuList = collection as TekuSSZVector<UnsignedLong>
        HashTreeUtil.hash_tree_root_vector_unsigned_long(tekuList)
      }
      else ->
        HashTreeUtil.hash_tree_root(HashTreeUtil.SSZTypes.VECTOR_OF_COMPOSITE, collection)
    }
  }

  override fun equals(other: Any?): Boolean {
    if (other is SSZMutableVectorWrapper<*, *>) {
      return other.collection == collection
    }
    return false
  }

  override fun hashCode(): Int {
    return collection.hashCode()
  }

  override fun toString(): String {
    return collection.toString()
  }
}

class SSZBitVectorWrapper(override val v: Bitvector) : Wrapper<Bitvector>, SSZBitVector {
  constructor(items: MutableList<Boolean>) : this(
    Bitvector(
      items.mapIndexed { i, v -> Pair(i, v) }.filter { p -> p.second }
        .map { p -> p.first },
      items.size
    )
  )

  override val size: Int = v.size
  override fun get(index: ULong): Boolean = v.getBit(index.toInt())
  override fun isEmpty() = size == 0
  override fun set(index: ULong, element: Boolean): Boolean {
    val oldItem = this[index.toInt()]
    v.setBit(index.toInt())
    return oldItem
  }

  override fun hash_tree_root() = HashTreeUtil.hash_tree_root_bitvector(v)

  override fun equals(other: Any?): Boolean {
    if (other is SSZBitVectorWrapper) {
      return other.v == v
    }
    return false
  }

  override fun hashCode(): Int {
    return v.hashCode()
  }

  override fun toString(): String {
    return v.toString()
  }

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
  (this as SSZMutableCollectionWrapper<Onotole, Teku>).forEachIndexed { i, item ->
    to.set(
      i,
      this.type.unwrap(item)
    )
  }
}
