package tech.pegasys.teku.phase1.integration.ssz

import tech.pegasys.teku.phase1.integration.datastructures.Mutable
import tech.pegasys.teku.phase1.integration.datastructures.Wrapper
import tech.pegasys.teku.phase1.integration.types.ByteType
import tech.pegasys.teku.phase1.integration.types.TypePair
import tech.pegasys.teku.phase1.onotole.ssz.Bytes1
import tech.pegasys.teku.phase1.onotole.ssz.SSZBitList
import tech.pegasys.teku.phase1.onotole.ssz.SSZBitVector
import tech.pegasys.teku.phase1.onotole.ssz.SSZByteList
import tech.pegasys.teku.phase1.onotole.ssz.SSZCollection
import tech.pegasys.teku.phase1.onotole.ssz.SSZList
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

internal abstract class SSZCollectionWrapper<Onotole : Any, Teku : Any>(
  internal val type: TypePair<Onotole, Teku>
) : SSZCollection<Onotole> {
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


  override fun equals(other: Any?): Boolean {
    if (other is SSZCollectionWrapper<*, *>) {
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

  override fun containsAll(elements: Collection<Onotole>) = throw UnsupportedOperationException()
  override fun listIterator(): MutableListIterator<Onotole> = throw UnsupportedOperationException()
  override fun listIterator(index: Int): MutableListIterator<Onotole> =
    throw UnsupportedOperationException()
}

internal open class SSZListWrapper<Onotole : Any, Teku : Any>(
  override val collection: TekuSSZList<Teku>,
  type: TypePair<Onotole, Teku>
) : SSZList<Onotole>, SSZCollectionWrapper<Onotole, Teku>(type) {

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
}

internal open class SSZMutableListWrapper<Onotole : Any, Teku : Any>(
  override val collection: TekuSSZMutableList<Teku>,
  type: TypePair<Onotole, Teku>
) : SSZMutableList<Onotole>, SSZListWrapper<Onotole, Teku>(collection, type) {

  constructor(immutable: SSZList<Onotole>) : this(
    TekuSSZList.createMutable(
      (immutable as SSZListWrapper<Onotole, Teku>).collection
    ), immutable.type
  )

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

  override fun append(item: Onotole) {
    collection.add(type.unwrap(item))
  }

  override fun get(index: ULong): Onotole {
    val item = type.wrap(collection[index.toInt()])
    if (item is Mutable<*>) {
      item.callback = { value -> this[index] = value as Onotole }
    }
    return item
  }

  override operator fun set(index: ULong, item: Onotole): Onotole {
    val oldItem = collection[index.toInt()]
    collection.set(index.toInt(), type.unwrap(item))
    return type.wrap(oldItem)
  }
}

internal class SSZBitListWrapper(override val v: Bitlist) : Wrapper<Bitlist>, SSZBitList {

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

  override fun append(item: Boolean) = throw UnsupportedOperationException()
  override fun contains(element: Boolean) = throw UnsupportedOperationException()
  override fun indexOf(element: Boolean) = throw UnsupportedOperationException()
  override fun lastIndexOf(element: Boolean) = throw UnsupportedOperationException()
  override fun iterator() = throw UnsupportedOperationException()
  override fun containsAll(elements: Collection<Boolean>) = throw UnsupportedOperationException()
  override fun listIterator(): MutableListIterator<Boolean> = throw UnsupportedOperationException()
  override fun listIterator(index: Int): MutableListIterator<Boolean> =
    throw UnsupportedOperationException()

  override fun subList(fromIndex: Int, toIndex: Int) = throw UnsupportedOperationException()
}

internal class SSZByteListWrapper(list: TekuSSZList<Byte>) :
  SSZListWrapper<Bytes1, Byte>(
    list,
    ByteType
  ), SSZByteList {

  constructor(items: MutableList<Byte>, maxSize: ULong) : this(
    TekuSSZList.createMutable(items, maxSize.toLong(), Byte::class.java)
  )
}

internal open class SSZVectorWrapper<Onotole : Any, Teku : Any>(
  override val collection: TekuSSZVector<Teku>,
  type: TypePair<Onotole, Teku>
) : SSZVector<Onotole>, SSZCollectionWrapper<Onotole, Teku>(type) {

  constructor(items: MutableList<Onotole>, type: TypePair<Onotole, Teku>)
      : this(
    TekuSSZVector.createMutable(items.map { type.unwrap(it) }.toList(), type.teku.java),
    type
  )
}

internal class SSZMutableVectorWrapper<Onotole : Any, Teku : Any>(
  override val collection: TekuSSZMutableVector<Teku>,
  type: TypePair<Onotole, Teku>
) : SSZMutableVector<Onotole>, SSZVectorWrapper<Onotole, Teku>(collection, type) {

  constructor(immutable: SSZVector<Onotole>) : this(
    TekuSSZVector.createMutable(
      (immutable as SSZVectorWrapper<Onotole, Teku>).collection.asList(),
      immutable.type.teku.java
    ),
    immutable.type
  )

  constructor(items: MutableList<Onotole>, type: TypePair<Onotole, Teku>)
      : this(
    TekuSSZVector.createMutable(items.map { type.unwrap(it) }.toList(), type.teku.java),
    type
  )

  override fun get(index: ULong): Onotole {
    val item = type.wrap(collection[index.toInt()])
    if (item is Mutable<*>) {
      item.callback = { value -> this[index] = value as Onotole }
    }
    return item
  }

  override operator fun set(index: ULong, item: Onotole): Onotole {
    val oldItem = collection[index.toInt()]
    collection.set(index.toInt(), type.unwrap(item))
    return type.wrap(oldItem)
  }
}

internal class SSZBitVectorWrapper(override val v: Bitvector) : Wrapper<Bitvector>, SSZBitVector {
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

  override fun contains(element: Boolean) = throw UnsupportedOperationException()
  override fun indexOf(element: Boolean) = throw UnsupportedOperationException()
  override fun lastIndexOf(element: Boolean) = throw UnsupportedOperationException()
  override fun iterator() = throw UnsupportedOperationException()
  override fun containsAll(elements: Collection<Boolean>) = throw UnsupportedOperationException()
  override fun listIterator(): MutableListIterator<Boolean> = throw UnsupportedOperationException()
  override fun listIterator(index: Int): MutableListIterator<Boolean> =
    throw UnsupportedOperationException()

  override fun subList(fromIndex: Int, toIndex: Int) = throw UnsupportedOperationException()
}

internal fun <Onotole : Any, Teku : Any> SSZMutableVector<Onotole>.copyTo(
  to: TekuSSZMutableCollection<Teku>
) {
  (this as SSZMutableVectorWrapper<Onotole, Teku>).forEachIndexed { i, item ->
    to.set(
      i,
      this.type.unwrap(item)
    )
  }
}

internal fun <Onotole : Any, Teku : Any> SSZMutableList<Onotole>.copyTo(
  to: TekuSSZMutableCollection<Teku>
) {
  (this as SSZMutableListWrapper<Onotole, Teku>).forEachIndexed { i, item ->
    to.set(
      i,
      this.type.unwrap(item)
    )
  }
}
