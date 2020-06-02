package tech.pegasys.teku.phase1.deps

import org.apache.tuweni.crypto.Hash
import org.apache.tuweni.ssz.SSZ
import tech.pegasys.teku.phase1.pylib.pyint
import tech.pegasys.teku.phase1.ssz.Bytes
import tech.pegasys.teku.phase1.ssz.Bytes32
import tech.pegasys.teku.phase1.ssz.Bytes48
import tech.pegasys.teku.phase1.ssz.Bytes96
import tech.pegasys.teku.phase1.ssz.SSZComposite
import tech.pegasys.teku.phase1.ssz.boolean
import tech.pegasys.teku.phase1.ssz.uint64
import tech.pegasys.teku.phase1.ssz.uint8
import tech.pegasys.teku.util.hashtree.HashTreeUtil
import java.lang.IllegalArgumentException

fun hash_tree_root(a: Any): Bytes32 {
  return when(a) {
    is SSZComposite -> a.hash_tree_root()
    is boolean -> HashTreeUtil.hash_tree_root(HashTreeUtil.SSZTypes.BASIC, SSZ.encodeBoolean(a))
    is uint8 -> HashTreeUtil.hash_tree_root(HashTreeUtil.SSZTypes.BASIC, SSZ.encodeUInt8(a.toInt()))
    is uint64 -> HashTreeUtil.hash_tree_root(HashTreeUtil.SSZTypes.BASIC, SSZ.encodeUInt64(a.toLong()))
    is Bytes -> HashTreeUtil.hash_tree_root(HashTreeUtil.SSZTypes.BASIC, a)
    else -> throw IllegalArgumentException("Unsupported SSZ type: " + a::class.qualifiedName)
  }
}

fun hash(a: Bytes): Bytes32 = Hash.sha2_256(a)

data class FQ2(val coeffs: Pair<pyint, pyint>)

object bls {
  fun Sign(privkey: pyint, message: Bytes): Bytes96 {
    TODO("Not yet implemented")
  }

  fun Verify(pubkey: Bytes48, message: Bytes, signature: Bytes96): Boolean {
    TODO("Not yet implemented")
  }

  fun Aggregate(signatures: Collection<Bytes96>): Bytes96 {
    TODO("Not yet implemented")
  }

  fun FastAggregateVerify(pubkeys: Collection<Bytes48>, root: Bytes, signature: Bytes96): Boolean {
    TODO("Not yet implemented")
  }

  fun AggregateVerify(pairs: List<Pair<Bytes48, Bytes>>, signature: Bytes96): boolean {
    TODO("Not yet implemented")
  }

  fun signature_to_G2(signature: Bytes96): Triple<FQ2, FQ2, FQ2> {
    TODO("Not yet implemented")
  }
}
