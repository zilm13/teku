package tech.pegasys.teku.phase1.onotole.ssz

import tech.pegasys.teku.phase1.integration.ssz.SSZBitlistImpl
import tech.pegasys.teku.phase1.integration.ssz.SSZBitvectorImpl
import tech.pegasys.teku.phase1.integration.ssz.SSZByteListImpl
import tech.pegasys.teku.phase1.integration.ssz.SSZMutableListImpl
import tech.pegasys.teku.phase1.integration.ssz.SSZMutableVectorImpl
import tech.pegasys.teku.phase1.integration.toUnsignedLong
import tech.pegasys.teku.phase1.onotole.phase1.Root
import tech.pegasys.teku.phase1.onotole.pylib.pybytes
import tech.pegasys.teku.phase1.onotole.pylib.pyint
import tech.pegasys.teku.ssz.SSZTypes.Bitvector
import tech.pegasys.teku.ssz.backing.ContainerViewRead
import tech.pegasys.teku.ssz.backing.ViewRead
import tech.pegasys.teku.ssz.backing.tree.TreeNode.BranchNode
import tech.pegasys.teku.ssz.backing.tree.TreeNode.LeafNode
import tech.pegasys.teku.ssz.backing.type.ViewType
import tech.pegasys.teku.ssz.backing.view.BasicViews
import kotlin.reflect.KClass
import org.apache.tuweni.bytes.Bytes as TuweniBytes
import org.apache.tuweni.bytes.Bytes32 as TuweniBytes32
import org.apache.tuweni.bytes.Bytes48 as TuweniBytes48
import tech.pegasys.teku.ssz.SSZTypes.Bytes4 as TekuBytes4

typealias SSZObject = Any

typealias bit = Boolean
typealias boolean = Boolean
typealias uint8 = UByte
typealias uint64 = ULong

typealias Bytes = TuweniBytes
typealias Bytes1 = Byte
typealias Bytes4 = TekuBytes4
typealias Bytes32 = TuweniBytes32
typealias Bytes48 = TuweniBytes48

data class Bytes96(val wrappedBytes: Bytes)

typealias SSZDict<K, V> = MutableMap<K, V>

typealias Sequence<T> = List<T>

fun uint64(v: pyint): uint64 = v.value.toLong().toULong()
fun Bytes4(): Bytes4 = Bytes4.fromHexString("0x00000000")
fun Bytes32(): Bytes32 = Bytes32.ZERO
fun Bytes32(x: List<Byte>): Bytes32 = Bytes32.wrap(x.toByteArray())
fun Bytes48(): Bytes48 = Bytes48.ZERO
fun Bytes96(): Bytes96 = Bytes96(TuweniBytes.concatenate(Bytes48.ZERO, Bytes48.ZERO))

fun <K, V> SSZDict() = mutableMapOf<K, V>()

interface SSZContainer {
  fun hashTreeRoot(): Root
}

interface SSZCollection<T : Any> : Sequence<T> {
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
  fun clear()
}

interface SSZBitlist : SSZList<Boolean>
interface SSZMutableBitlist : SSZMutableList<Boolean>, SSZBitlist
interface SSZByteList : SSZList<Byte>
interface SSZVector<T : Any> : SSZCollection<T>
interface SSZByteVector : SSZVector<Byte>
interface SSZMutableVector<T : Any> : SSZVector<T>, SSZMutableCollection<T>
interface SSZBitvector : SSZVector<Boolean>
interface SSZMutableBitvector : SSZMutableVector<Boolean>

fun SSZByteList.toPyBytes(): pybytes = Bytes.wrap(this.toByteArray())

interface TreeNode {
  fun get_left(): TreeNode
  fun get_right(): TreeNode
  fun merkle_root(): Bytes32
}

class LeafNodeDelegate(private val delegate: LeafNode) : TreeNode {
  override fun get_left(): TreeNode = throw UnsupportedOperationException()
  override fun get_right(): TreeNode = throw UnsupportedOperationException()
  override fun merkle_root(): Bytes32 = delegate.root
}

class BranchNodeDelegate(private val delegate: BranchNode) : TreeNode {
  override fun get_left(): TreeNode = buildNodeDelegate(delegate.left())
  override fun get_right(): TreeNode = buildNodeDelegate(delegate.right())
  override fun merkle_root(): Bytes32 = delegate.hashTreeRoot()
}

private fun buildNodeDelegate(node: tech.pegasys.teku.ssz.backing.tree.TreeNode): TreeNode {
  return if (node is LeafNode) LeafNodeDelegate(node) else BranchNodeDelegate(node as BranchNode)
}

fun SSZByteList.get_backing(): TreeNode = buildNodeDelegate((this as SSZByteListImpl).backingNode)

fun <T : Any> SSZVector(
  elementType: ViewType,
  size: ULong,
  unwrapper: (ViewRead) -> T = getUnwrapper(elementType.default::class),
  wrapper: (T) -> ViewRead = getWrapper(elementType.default::class)
): SSZMutableVector<T> =
  SSZMutableVectorImpl(
    elementType,
    size,
    unwrapper,
    wrapper
  )

fun <T : Any> SSZVector(
  elementType: ViewType,
  elements: List<T>,
  unwrapper: (ViewRead) -> T = getUnwrapper(elementType.default::class),
  wrapper: (T) -> ViewRead = getWrapper(elementType.default::class)
): SSZMutableVector<T> =
  SSZMutableVectorImpl(
    elementType,
    elements,
    unwrapper,
    wrapper
  )

fun <T : Any> SSZList(
  elementType: ViewType,
  maxSize: ULong,
  elements: List<T> = listOf(),
  unwrapper: (ViewRead) -> T = getUnwrapper(elementType.default::class),
  wrapper: (T) -> ViewRead = getWrapper(elementType.default::class)
): SSZMutableList<T> =
  SSZMutableListImpl(
    elementType,
    maxSize,
    elements,
    unwrapper,
    wrapper
  )

fun SSZBitlist(maxSize: ULong, elements: List<Boolean> = listOf()): SSZBitlist =
  SSZBitlistImpl(maxSize, elements)

fun SSZBitvector(bitvector: Bitvector): SSZBitvector {
  return SSZBitvectorImpl((0 until bitvector.size).map { bitvector.getBit(it) }.toList())
}

fun <U : Any, V : ViewRead> getWrapper(viewType: KClass<out V>): (U) -> V {
  return when (viewType) {
    ContainerViewRead::class -> { e -> e as V }
    BasicViews.BitView::class -> { e -> BasicViews.BitView(e as boolean) as V }
    BasicViews.ByteView::class -> { e -> BasicViews.ByteView(e as Byte) as V }
    BasicViews.UInt64View::class -> { e -> BasicViews.UInt64View((e as uint64).toUnsignedLong()) as V }
    BasicViews.Bytes4View::class -> { e -> BasicViews.Bytes4View(e as Bytes4) as V }
    BasicViews.Bytes32View::class -> { e -> BasicViews.Bytes32View(e as Bytes32) as V }
    else -> throw IllegalArgumentException("Unsupported type ${viewType.qualifiedName}")
  }
}

fun <U : Any, V : ViewRead> getUnwrapper(viewType: KClass<out V>): (V) -> U {
  return when (viewType) {
    ContainerViewRead::class -> { v -> v as U }
    BasicViews.BitView::class -> { v -> (v as BasicViews.BitView).get() as U }
    BasicViews.ByteView::class -> { v -> (v as BasicViews.ByteView).get() as U }
    BasicViews.UInt64View::class -> { v -> (v as BasicViews.UInt64View).get() as U }
    BasicViews.Bytes4View::class -> { v -> (v as BasicViews.Bytes4View).get() as U }
    BasicViews.Bytes32View::class -> { v -> (v as BasicViews.Bytes32View).get() as U }
    else -> throw IllegalArgumentException("Unsupported type ${viewType.qualifiedName}")
  }
}
