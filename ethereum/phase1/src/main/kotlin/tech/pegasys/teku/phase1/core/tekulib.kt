package tech.pegasys.teku.phase1.core

import com.google.common.primitives.UnsignedLong
import org.apache.tuweni.bytes.Bytes
import org.apache.tuweni.bytes.Bytes48
import org.apache.tuweni.ssz.SSZ
import tech.pegasys.teku.bls.BLSPublicKey
import tech.pegasys.teku.phase1.ssz.Bytes32
import tech.pegasys.teku.phase1.ssz.CBitlist
import tech.pegasys.teku.phase1.ssz.CBitvector
import tech.pegasys.teku.phase1.ssz.CList
import tech.pegasys.teku.phase1.ssz.CVector
import tech.pegasys.teku.phase1.ssz.SSZBitlist
import tech.pegasys.teku.phase1.ssz.SSZList
import tech.pegasys.teku.phase1.ssz.uint64
import tech.pegasys.teku.ssz.SSZTypes.Bitlist
import tech.pegasys.teku.ssz.SSZTypes.Bitvector
import tech.pegasys.teku.ssz.SSZTypes.SSZMutableList
import tech.pegasys.teku.ssz.SSZTypes.SSZMutableVector
import tech.pegasys.teku.util.hashtree.HashTreeUtil
import java.util.stream.Collectors
import kotlin.reflect.KClass

fun UnsignedLong(v: uint64) = UnsignedLong.valueOf(v.toLong())
fun uint64(v: UnsignedLong): uint64 = v.toLong().toULong()
fun BLSPubkey(x: Bytes): BLSPubkey = BLSPubkey(Bytes48.wrap(x))

fun <T : Any> CVector<T>.copyTo(to: SSZMutableVector<T>) {
  this.forEachIndexed { i, e -> to.set(i, e) }
}

fun <U : Any, V : Any> CVector<U>.copyTo(to: SSZMutableVector<V>, convert: (U) -> V) {
  this.forEachIndexed { i, e -> to.set(i, convert(e)) }
}

fun <T : Any> CList<T>.copyTo(to: SSZMutableList<T>) {
  this.forEachIndexed { i, e -> to.set(i, e) }
}

fun <U : Any, V : Any> CList<U>.copyTo(to: SSZMutableList<V>, convert: (U) -> V) {
  this.forEachIndexed { i, e -> to.set(i, convert(e)) }
}

fun CBitlist.asTeku(): Bitlist {
  val bitlist = Bitlist(items.size, this.max_size.toLong())
  items.forEachIndexed { idx, bit -> if (bit) bitlist.setBit(idx) }
  return bitlist
}

fun CBitvector.asTeku(): Bitvector {
  val bitvector = Bitvector(items.size)
  items.forEachIndexed { idx, bit -> if (bit) bitvector.setBit(idx) }
  return bitvector
}

fun BLSPubkey.asTeku() = BLSPublicKey.fromBytes(this)

fun Fork.asTeku() = tech.pegasys.teku.datastructures.state.Fork(
    this.previous_version,
    this.current_version,
    UnsignedLong(this.epoch)
)

fun tech.pegasys.teku.datastructures.state.Fork.asOnotole() = Fork(
    this.previous_version,
    this.current_version,
    uint64(this.epoch)
)

fun BeaconBlockHeader.asTeku() = tech.pegasys.teku.datastructures.blocks.BeaconBlockHeader(
    UnsignedLong(this.slot),
    UnsignedLong(this.proposer_index),
    this.parent_root,
    this.state_root,
    this.body_root
)

fun tech.pegasys.teku.datastructures.blocks.BeaconBlockHeader.asOnotole() = BeaconBlockHeader(
    uint64(this.slot),
    uint64(this.proposer_index)
)

fun Eth1Data.asTeku() = tech.pegasys.teku.datastructures.blocks.Eth1Data(
    this.deposit_root,
    UnsignedLong(this.deposit_count),
    this.block_hash
)

fun tech.pegasys.teku.datastructures.blocks.Eth1Data.asOnotole() = Eth1Data(
    this.deposit_root,
    uint64(this.deposit_count),
    this.block_hash
)

fun Validator.asTeku() = tech.pegasys.teku.datastructures.state.Validator(
    this.pubkey.asTeku(),
    this.withdrawal_credentials,
    UnsignedLong(this.effective_balance),
    this.slashed,
    UnsignedLong(this.activation_eligibility_epoch),
    UnsignedLong(this.activation_epoch),
    UnsignedLong(this.exit_epoch),
    UnsignedLong(this.withdrawable_epoch)
)

fun Checkpoint.asTeku() = tech.pegasys.teku.datastructures.state.Checkpoint(
    UnsignedLong(this.epoch),
    this.root
)

fun tech.pegasys.teku.datastructures.state.Checkpoint.asOnotole() = Checkpoint(
    uint64(this.epoch),
    this.root
)

fun AttestationData.asTeku() = tech.pegasys.teku.datastructures.operations.AttestationData(
    UnsignedLong(this.slot),
    UnsignedLong(this.index),
    this.beacon_block_root,
    this.source.asTeku(),
    this.target.asTeku()
)

fun PendingAttestation.asTeku() = tech.pegasys.teku.datastructures.state.PendingAttestation(
    this.aggregation_bits.asTeku(),
    this.data.asTeku(),
    UnsignedLong(inclusion_delay),
    UnsignedLong(proposer_index)
)

interface TypeCast {
  companion object companion {
    fun <T> asOnotole(o: Any): T {
      return when (o::class) {
        Validator::class -> {
          val v = o as tech.pegasys.teku.datastructures.state.Validator
          Validator(
              BLSPubkey(v.pubkey.toBytesCompressed()),
              v.withdrawal_credentials,
              uint64(v.effective_balance),
              v.isSlashed,
              uint64(v.activation_eligibility_epoch),
              uint64(v.activation_epoch),
              uint64(v.exit_epoch),
              uint64(v.withdrawable_epoch)
          ) as T
        }
        else -> throw IllegalArgumentException("Unknown type: " + o::class.qualifiedName)
      }
    }

    fun <T> asTeku(o: Any): T {
      return when (o::class) {
        Validator::class -> {
          val v = o as Validator
          tech.pegasys.teku.datastructures.state.Validator(
              BLSPublicKey.fromBytesCompressed(v.pubkey),
              v.withdrawal_credentials,
              UnsignedLong(v.effective_balance),
              v.slashed,
              UnsignedLong(v.activation_eligibility_epoch),
              UnsignedLong(v.activation_epoch),
              UnsignedLong(v.exit_epoch),
              UnsignedLong(v.withdrawable_epoch)
          ) as T
        }
        else -> throw IllegalArgumentException("Unknown type: " + o::class.qualifiedName)
      }
    }
  }
}

interface TypeMap {
  companion object companion {
    fun <Onotole : Any, Teku : Any> asTeku(type: KClass<Onotole>): Class<Teku> {
      when (type) {
        Validator::class -> return tech.pegasys.teku.datastructures.state.Validator::class.java as Class<Teku>
        else -> throw IllegalArgumentException("Unknown type: " + type.qualifiedName)
      }
    }
  }
}

open class TekuDelegateSSZList<Onotole : Any, in Teku : Any>(items: MutableList<Onotole>, maxSize: uint64, type: KClass<Onotole>) : SSZList<Onotole> {
  private val type: KClass<Onotole> = type
  private val data: SSZMutableList<Teku> =
      tech.pegasys.teku.ssz.SSZTypes.SSZList.createMutable(
          items.map { TypeCast.asTeku<Teku>(it) }.toMutableList(), maxSize.toLong(), TypeMap.asTeku(type))

  constructor(maxSize: uint64, type: KClass<Onotole>) : this(mutableListOf<Onotole>(), maxSize, type)

  override val size: Int = data.size()
  override fun contains(element: Onotole): Boolean = data.contains(TypeCast.asTeku(element))
  override fun get(index: Int): Onotole = TypeCast.asOnotole(data[index])
  override fun indexOf(element: Onotole) = data.indexOf(TypeCast.asTeku(element))
  override fun isEmpty() = data.isEmpty
  override fun iterator(): MutableIterator<Onotole> = object : MutableIterator<Onotole> {
    private val iterator = data.iterator()
    override fun hasNext() = iterator.hasNext()
    override fun next() = TypeCast.asOnotole<Onotole, >(iterator.next())
    override fun remove() = iterator.remove()
  }

  override fun lastIndexOf(element: Onotole) = data.lastIndexOf(TypeCast.asTeku(element))
  override fun add(element: Onotole): Boolean {
    data.add(TypeCast.asTeku(element))
    return true
  }

  override fun addAll(elements: Collection<Onotole>): Boolean {
    data.addAll(elements.stream().map { TypeCast.asTeku<Teku>(it) }.collect(Collectors.toList()))
    return true
  }

  override fun clear() = data.clear()
  override fun remove(element: Onotole) = data.removeAll { element == TypeCast.asOnotole(it) }
  override fun removeAll(elements: Collection<Onotole>) = data.removeAll { elements.contains(TypeCast.asOnotole(it)) }
  override fun retainAll(elements: Collection<Onotole>) = data.retainAll { elements.contains(TypeCast.asOnotole(it)) }
  override fun set(index: Int, element: Onotole): Onotole {
    val oldItem = data[index]
    data.set(index, TypeCast.asTeku(element))
    return TypeCast.asOnotole(oldItem)
  }

  override fun hash_tree_root(): Bytes32 {
    return when (type) {
      uint64::class ->
        HashTreeUtil.hash_tree_root_list_ul(
            (data as tech.pegasys.teku.ssz.SSZTypes.SSZList<UnsignedLong>)
                .map(Bytes::class.java) { SSZ.encodeUInt64(it.toLong()) }
        )
      Bytes32::class ->
        HashTreeUtil.hash_tree_root_list_bytes(
            data as tech.pegasys.teku.ssz.SSZTypes.SSZList<org.apache.tuweni.bytes.Bytes32>
        )
      else ->
        HashTreeUtil.hash_tree_root(HashTreeUtil.SSZTypes.LIST_OF_COMPOSITE, data)
    }
  }

  override fun containsAll(elements: Collection<Onotole>) = TODO("Not yet implemented")
  override fun add(index: Int, element: Onotole) = TODO("Not yet implemented")
  override fun addAll(index: Int, elements: Collection<Onotole>) = TODO("Not yet implemented")
  override fun listIterator(): MutableListIterator<Onotole> = TODO("Not yet implemented")
  override fun listIterator(index: Int): MutableListIterator<Onotole> = TODO("Not yet implemented")
  override fun removeAt(index: Int) = TODO("Not yet implemented")
  override fun subList(fromIndex: Int, toIndex: Int) = TODO("Not yet implemented")
}

open class TekuDelegateSSZBitlist(items: MutableList<Boolean>, maxSize: uint64) : SSZBitlist {
  private val data: Bitlist
  init {
    data = Bitlist(items.size, maxSize.toLong())
    items.forEachIndexed() { i, bit -> if (bit) data.setBit(i) }
  }

  constructor(maxSize: uint64) : this(mutableListOf(), maxSize)

  override val size: Int = data.currentSize
  override fun contains(element: Onotole): Boolean = data.contains(TypeCast.asTeku(element))
  override fun get(index: Int): Onotole = TypeCast.asOnotole(data[index])
  override fun indexOf(element: Onotole) = data.indexOf(TypeCast.asTeku(element))
  override fun isEmpty() = data.isEmpty
  override fun iterator(): MutableIterator<Onotole> = object : MutableIterator<Onotole> {
    private val iterator = data.iterator()
    override fun hasNext() = iterator.hasNext()
    override fun next() = TypeCast.asOnotole<Onotole, >(iterator.next())
    override fun remove() = iterator.remove()
  }

  override fun lastIndexOf(element: Onotole) = data.lastIndexOf(TypeCast.asTeku(element))
  override fun add(element: Onotole): Boolean {
    data.add(TypeCast.asTeku(element))
    return true
  }

  override fun addAll(elements: Collection<Onotole>): Boolean {
    data.addAll(elements.stream().map { TypeCast.asTeku<Teku>(it) }.collect(Collectors.toList()))
    return true
  }

  override fun clear() = data.clear()
  override fun remove(element: Onotole) = data.removeAll { element == TypeCast.asOnotole(it) }
  override fun removeAll(elements: Collection<Onotole>) = data.removeAll { elements.contains(TypeCast.asOnotole(it)) }
  override fun retainAll(elements: Collection<Onotole>) = data.retainAll { elements.contains(TypeCast.asOnotole(it)) }
  override fun set(index: Int, element: Onotole): Onotole {
    val oldItem = data[index]
    data.set(index, TypeCast.asTeku(element))
    return TypeCast.asOnotole(oldItem)
  }

  override fun hash_tree_root(): Bytes32 {
    return when (type) {
      uint64::class ->
        HashTreeUtil.hash_tree_root_list_ul(
            (data as tech.pegasys.teku.ssz.SSZTypes.SSZList<UnsignedLong>)
                .map(Bytes::class.java) { SSZ.encodeUInt64(it.toLong()) }
        )
      Bytes32::class ->
        HashTreeUtil.hash_tree_root_list_bytes(
            data as tech.pegasys.teku.ssz.SSZTypes.SSZList<org.apache.tuweni.bytes.Bytes32>
        )
      else ->
        HashTreeUtil.hash_tree_root(HashTreeUtil.SSZTypes.LIST_OF_COMPOSITE, data)
    }
  }

  override fun containsAll(elements: Collection<Onotole>) = TODO("Not yet implemented")
  override fun add(index: Int, element: Onotole) = TODO("Not yet implemented")
  override fun addAll(index: Int, elements: Collection<Onotole>) = TODO("Not yet implemented")
  override fun listIterator(): MutableListIterator<Onotole> = TODO("Not yet implemented")
  override fun listIterator(index: Int): MutableListIterator<Onotole> = TODO("Not yet implemented")
  override fun removeAt(index: Int) = TODO("Not yet implemented")
  override fun subList(fromIndex: Int, toIndex: Int) = TODO("Not yet implemented")
}