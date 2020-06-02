package tech.pegasys.teku.phase1.ssz

import com.google.common.primitives.UnsignedLong
import org.apache.tuweni.ssz.SSZ
import tech.pegasys.teku.phase1.pylib.get
import tech.pegasys.teku.ssz.SSZTypes.Bitlist
import tech.pegasys.teku.ssz.SSZTypes.Bitvector
import tech.pegasys.teku.ssz.SSZTypes.SSZList
import tech.pegasys.teku.ssz.SSZTypes.SSZVector
import tech.pegasys.teku.util.hashtree.HashTreeUtil
import kotlin.reflect.KClass
import org.apache.tuweni.bytes.Bytes as TuweniBytes
import org.apache.tuweni.bytes.Bytes32 as TuweniBytes32
import org.apache.tuweni.bytes.Bytes48 as TuweniBytes48
import tech.pegasys.teku.ssz.SSZTypes.Bytes4 as SSZBytes4

typealias SSZObject = Any

typealias boolean = Boolean
typealias uint8 = UByte
typealias uint64 = ULong

typealias Bytes = TuweniBytes
typealias Bytes1 = Byte
//typealias Bytes4 = TuweniBytes
typealias Bytes4 = SSZBytes4
typealias Bytes32 = TuweniBytes32
typealias Bytes48 = TuweniBytes48
typealias Bytes96 = TuweniBytes

//typealias Bitlist = BooleanArray
//typealias CBitlist = List<Boolean>
//typealias CBitlist = CList<Boolean>
//typealias CBitvector = MutableList<Boolean>
//typealias CBitvector = CVector<Boolean>
//typealias CByteList = List<Byte>
//typealias CByteList = CList<Byte>
//typealias CList<T> = MutableList<T>
//typealias CVector<T> = MutableList<T>
//typealias CSequence<T> = List<T>
typealias CDict<K, V> = MutableMap<K, V>

typealias Sequence<T> = List<T>
//typealias Vector<T> = MutableList<T>

//fun Bytes4(): Bytes4 = TuweniBytes.fromHexString("0x00000000")
fun Bytes4(): Bytes4 = SSZBytes4.fromHexString("0x00000000")
fun Bytes32(): Bytes32 = Bytes32.ZERO
fun Bytes32(x: List<Byte>): Bytes32 = Bytes32.wrap(x.toByteArray())
fun Bytes48(): Bytes48 = Bytes48.ZERO
fun Bytes96(): Bytes96 = TuweniBytes.concatenate(Bytes48.ZERO, Bytes48.ZERO)

//fun CBitlist() = listOf<Boolean>()
//fun CBitvector(): CBitvector = mutableListOf<Boolean>()
//fun CByteList() = listOf<Byte>()
//fun <T> CList() = mutableListOf<T>()
//fun <T> CVector() = mutableListOf<T>()
fun <K, V> CDict() = mutableMapOf<K, V>()

// ADDITIONS

@ExperimentalUnsignedTypes
interface SSZList<T : Any> : MutableList<T>, SSZComposite
interface SSZBitlist : tech.pegasys.teku.phase1.ssz.SSZList<Boolean>

@ExperimentalUnsignedTypes
open class CList<T : Any> constructor(
    protected val class_info: KClass<T>,
    val max_size: ULong,
    val items: MutableList<T>
) : SSZComposite, Sequence<T> {

  constructor(class_info: KClass<T>, max_size: ULong) : this(class_info, max_size, mutableListOf())
  constructor(class_info: KClass<T>) : this(class_info, ULong.MAX_VALUE)

  override fun hash_tree_root(): org.apache.tuweni.bytes.Bytes32 {
    return when (class_info) {
      uint64::class -> {
        val tekuList = SSZList.createMutable(
            items.map { SSZ.encodeUInt64((it as uint64).toLong()) },
            max_size.toLong(),
            Bytes::class.java)
        HashTreeUtil.hash_tree_root_list_ul(tekuList)
      }
      Bytes32::class -> {
        val tekuList = SSZList.createMutable(
            items.map { it as Bytes32 }, max_size.toLong(), Bytes32::class.java)
        HashTreeUtil.hash_tree_root_list_bytes(tekuList)
      }
      else ->
        HashTreeUtil.hash_tree_root(HashTreeUtil.SSZTypes.LIST_OF_COMPOSITE,
            SSZList.createMutable(items, max_size.toLong(), class_info.java))
    }
  }

  fun updateSlice(f: uint64, t: uint64, x: List<T>) {
    for (i in f until t) {
      this[i] = x[i]
    }
  }

  operator fun get(i: ULong) = get(i.toInt())
  operator fun set(i: ULong, item: T) = items.set(i.toInt(), item)

  override val size: Int get() = items.size
  override fun iterator() = items.iterator()
  override fun contains(element: T) = items.contains(element)
  override fun containsAll(elements: Collection<T>) = items.containsAll(elements)
  override fun indexOf(element: T) = items.indexOf(element)
  override fun isEmpty() = items.isEmpty()
  override fun lastIndexOf(element: T) = items.lastIndexOf(element)
  override fun listIterator() = items.listIterator()
  override fun listIterator(index: Int) = items.listIterator(index)
  override fun subList(fromIndex: Int, toIndex: Int) = items.subList(fromIndex, toIndex)
  override fun get(index: Int) = items[index]
}

@ExperimentalUnsignedTypes
class CBitlist(max_size: ULong, items: MutableList<Boolean>)
  : CList<Boolean>(Boolean::class, max_size, items) {
  constructor(max_size: ULong) : this(max_size, mutableListOf())
  constructor() : this(ULong.MAX_VALUE, mutableListOf())

  override fun hash_tree_root(): org.apache.tuweni.bytes.Bytes32 {
    val bitlist = Bitlist(items.size, max_size.toLong())
    items.forEachIndexed { idx, bit -> if (bit) bitlist.setBit(idx) }
    return HashTreeUtil.hash_tree_root_bitlist(bitlist)
  }
}

@ExperimentalUnsignedTypes
class CByteList(max_size: ULong, items: MutableList<Byte>)
  : CList<Byte>(Byte::class, max_size, items) {
  constructor(max_size: ULong) : this(max_size, mutableListOf())
  constructor() : this(ULong.MAX_VALUE, mutableListOf())
}

@ExperimentalUnsignedTypes
// TODO explicitly check for the size of the vector
open class CVector<T : Any>(class_info: KClass<T>, items: MutableList<T>) :
    CList<T>(class_info, 0uL, items) {

  constructor(class_info: KClass<T>) : this(class_info, mutableListOf())

  override fun hash_tree_root(): org.apache.tuweni.bytes.Bytes32 {
    return when (class_info) {
      uint64::class -> {
        val tekuList = SSZVector.createMutable(
            items.map { UnsignedLong.valueOf((it as uint64).toLong()) },
            UnsignedLong::class.java)
        HashTreeUtil.hash_tree_root_vector_unsigned_long(tekuList)
      }
      else ->
        HashTreeUtil.hash_tree_root(
            HashTreeUtil.SSZTypes.VECTOR_OF_COMPOSITE,
            SSZVector.createMutable(items, class_info.java))

    }
  }
}

@ExperimentalUnsignedTypes
class CBitvector(items: MutableList<Boolean>) : CVector<Boolean>(Boolean::class, items) {
  constructor() : this(mutableListOf())
  operator fun set(i: uint64, v: uint64) = this.set(i, v != 0uL)

  override fun hash_tree_root(): org.apache.tuweni.bytes.Bytes32 {
    val bitvector = Bitvector(items.size)
    items.forEachIndexed { idx, bit -> if (bit) bitvector.setBit(idx) }
    return HashTreeUtil.hash_tree_root_bitvector(bitvector)
  }
}

interface SSZComposite {
  fun hash_tree_root() : Bytes32
}
