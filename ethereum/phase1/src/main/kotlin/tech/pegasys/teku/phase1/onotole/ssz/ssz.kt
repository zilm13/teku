package tech.pegasys.teku.phase1.onotole.ssz

import tech.pegasys.teku.phase1.onotole.phase1.Root
import kotlin.reflect.KClass
import org.apache.tuweni.bytes.Bytes as TuweniBytes
import org.apache.tuweni.bytes.Bytes32 as TuweniBytes32
import org.apache.tuweni.bytes.Bytes48 as TuweniBytes48
import tech.pegasys.teku.ssz.SSZTypes.Bytes4 as TekuBytes4

typealias SSZObject = Any

typealias boolean = Boolean
typealias uint8 = UByte
typealias uint64 = ULong

typealias Bytes = TuweniBytes
typealias Bytes1 = Byte
typealias Bytes4 = TekuBytes4
typealias Bytes32 = TuweniBytes32
typealias Bytes48 = TuweniBytes48
data class Bytes96(val wrappedBytes: Bytes)

typealias Bitlist = BooleanArray
typealias CBitlist = List<Boolean>
typealias CBitvector = MutableList<Boolean>
typealias CByteList = List<Byte>
typealias CList<T> = MutableList<T>
typealias CVector<T> = MutableList<T>
typealias CSequence<T> = List<T>
typealias CDict<K, V> = MutableMap<K, V>

typealias Sequence<T> = List<T>
typealias Vector<T> = MutableList<T>

fun Bytes4(): Bytes4 = Bytes4.fromHexString("0x00000000")
fun Bytes32(): Bytes32 = Bytes32.ZERO
fun Bytes32(x: List<Byte>): Bytes32 = Bytes32.wrap(x.toByteArray())
fun Bytes48(): Bytes48 = Bytes48.ZERO
fun Bytes96(): Bytes96 = Bytes96(TuweniBytes.concatenate(Bytes48.ZERO, Bytes48.ZERO))

fun CBitlist() = listOf<Boolean>()
fun CBitvector(): CBitvector = mutableListOf<Boolean>()
fun CByteList() = listOf<Byte>()
fun <T> CList() = mutableListOf<T>()
fun <T> CVector() = mutableListOf<T>()
fun <K, V> CDict() = mutableMapOf<K, V>()

interface SSZComposite {
  fun hash_tree_root(): Root
}

interface SSZCollection<T: Any> : Sequence<T> {
  operator fun get(index: ULong): T
  override operator fun get(index: Int): T = get(index.toULong())
}

interface SSZMutableCollection<T : Any> : SSZCollection<T> {
  operator fun set(index: ULong, item: T): T
  operator fun set(index: Int, item: T): T = set(index.toULong(), item)
}

interface SSZList<T : Any> : SSZCollection<T> {
  val maxSize: ULong
}

interface SSZMutableList<T : Any> : SSZList<T>, SSZMutableCollection<T> {
  fun append(item: T)
}

interface SSZBitList : SSZMutableList<Boolean>, SSZComposite
interface SSZByteList : SSZList<Byte>
interface SSZVector<T : Any> : SSZCollection<T>
interface SSZMutableVector<T : Any> : SSZVector<T>, SSZMutableCollection<T>
interface SSZBitVector : SSZMutableVector<Boolean>, SSZComposite

interface SSZObjectFactory {
  fun <T : Any> SSZList(type: KClass<T>, maxSize: ULong, items: MutableList<T> = mutableListOf()): SSZMutableList<T>
  fun <T : Any> SSZList(immutable: SSZList<T>): SSZMutableList<T>
  fun SSZByteList(maxSize: ULong, items: MutableList<Byte> = mutableListOf()): SSZByteList
  fun SSZBitList(maxSize: ULong, items: MutableList<Boolean> = mutableListOf()): SSZBitList
  fun <T : Any> SSZVector(type: KClass<T>, items: MutableList<T>): SSZMutableVector<T>
  fun <T : Any> SSZVector(immutable: SSZVector<T>): SSZMutableVector<T>
  fun SSZBitVector(items: MutableList<Boolean>): SSZBitVector
}
