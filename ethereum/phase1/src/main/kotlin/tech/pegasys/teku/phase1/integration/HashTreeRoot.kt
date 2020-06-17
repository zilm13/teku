package tech.pegasys.teku.phase1.integration

import com.google.common.primitives.UnsignedLong
import org.apache.tuweni.ssz.SSZ
import tech.pegasys.teku.phase1.integration.ssz.SSZListWrapper
import tech.pegasys.teku.phase1.integration.ssz.SSZVectorWrapper
import tech.pegasys.teku.phase1.onotole.phase1.BLSPubkey
import tech.pegasys.teku.phase1.onotole.phase1.BLSSignature
import tech.pegasys.teku.phase1.onotole.phase1.Domain
import tech.pegasys.teku.phase1.onotole.phase1.Root
import tech.pegasys.teku.phase1.onotole.ssz.Bytes
import tech.pegasys.teku.phase1.onotole.ssz.Bytes32
import tech.pegasys.teku.phase1.onotole.ssz.Bytes4
import tech.pegasys.teku.phase1.onotole.ssz.SSZCollection
import tech.pegasys.teku.phase1.onotole.ssz.SSZComposite
import tech.pegasys.teku.phase1.onotole.ssz.boolean
import tech.pegasys.teku.phase1.onotole.ssz.uint64
import tech.pegasys.teku.phase1.onotole.ssz.uint8
import tech.pegasys.teku.util.hashtree.HashTreeUtil
import tech.pegasys.teku.bls.BLSPublicKey as TekuBLSPublicKey
import tech.pegasys.teku.bls.BLSSignature as TekuBLSSignature
import tech.pegasys.teku.ssz.SSZTypes.Bytes4 as TekuBytes4

class HashTreeRoot {
  companion object {
    fun basicTypeValue(v: Any): Root {
      return when (v) {
        is Byte -> HashTreeUtil.hash_tree_root(
          HashTreeUtil.SSZTypes.BASIC,
          SSZ.encodeUInt8(v.toInt() and 0xFF)
        )
        is UnsignedLong -> HashTreeUtil.hash_tree_root(
          HashTreeUtil.SSZTypes.BASIC,
          SSZ.encodeInt64(v.toLong())
        )
        is TekuBLSSignature -> HashTreeUtil.hash_tree_root(
          HashTreeUtil.SSZTypes.VECTOR_OF_BASIC,
          v.toBytes()
        )
        is TekuBLSPublicKey -> HashTreeUtil.hash_tree_root(
          HashTreeUtil.SSZTypes.VECTOR_OF_BASIC,
          v.toBytes()
        )
        is TekuBytes4 -> HashTreeUtil.hash_tree_root(
          HashTreeUtil.SSZTypes.VECTOR_OF_BASIC,
          v.wrappedBytes
        )
        is uint8 -> HashTreeUtil.hash_tree_root(
          HashTreeUtil.SSZTypes.BASIC,
          SSZ.encodeUInt8(v.toInt())
        )
        is uint64 -> HashTreeUtil.hash_tree_root(
          HashTreeUtil.SSZTypes.BASIC,
          SSZ.encodeInt64(v.toLong())
        )
        is boolean -> HashTreeUtil.hash_tree_root(HashTreeUtil.SSZTypes.BASIC, SSZ.encodeBoolean(v))
        is Bytes -> HashTreeUtil.hash_tree_root(HashTreeUtil.SSZTypes.VECTOR_OF_BASIC, v)
        is BLSSignature -> HashTreeUtil.hash_tree_root(HashTreeUtil.SSZTypes.VECTOR_OF_BASIC, v)
        is Bytes4 -> HashTreeUtil.hash_tree_root(HashTreeUtil.SSZTypes.VECTOR_OF_BASIC, v)
        is Domain -> HashTreeUtil.hash_tree_root(HashTreeUtil.SSZTypes.VECTOR_OF_BASIC, v)
        is BLSPubkey -> HashTreeUtil.hash_tree_root(HashTreeUtil.SSZTypes.VECTOR_OF_BASIC, v)
        is Bytes32 -> HashTreeUtil.hash_tree_root(HashTreeUtil.SSZTypes.VECTOR_OF_BASIC, v)
        else -> throw UnsupportedOperationException("Unsupported type ${v::class.qualifiedName}")
      }
    }

    fun sszCompositeTypeValue(v: SSZComposite): Root {
      return v.hash_tree_root()
    }

    fun sszCollection(v: SSZCollection<*>): Root {
      return when (v) {
        is SSZVectorWrapper<*, *> ->
          HashTreeUtil.merkleize(v.collection.map { compute(it) }.toList())
        is SSZListWrapper<*, *> ->
          HashTreeUtil.hash_tree_root_list_bytes(v.collection.map(Bytes32::class.java) { compute(it) })
        else -> throw UnsupportedOperationException("Unsupported type ${v::class.qualifiedName}")
      }
    }

    fun compute(v: Any): Root {
      return when (v) {
        isBasicTypeValue(v) -> basicTypeValue(v)
        is SSZCollection<*> -> sszCollection(v)
        is SSZComposite -> sszCompositeTypeValue(v)
        else -> throw UnsupportedOperationException("Unsupported type ${v::class.qualifiedName}")
      }
    }
  }
}
