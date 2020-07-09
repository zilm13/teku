package tech.pegasys.teku.phase1.onotole.deps

import com.google.common.primitives.UnsignedLong
import org.apache.milagro.amcl.BLS381.BIG
import org.apache.milagro.amcl.BLS381.FP2
import org.apache.tuweni.crypto.Hash
import tech.pegasys.teku.phase1.integration.bls.G2Points
import tech.pegasys.teku.phase1.integration.ssz.SSZAbstractCollection
import tech.pegasys.teku.phase1.onotole.phase1.BLSPubkey
import tech.pegasys.teku.phase1.onotole.phase1.BLSSignature
import tech.pegasys.teku.phase1.onotole.pylib.pyint
import tech.pegasys.teku.phase1.onotole.ssz.Bytes
import tech.pegasys.teku.phase1.onotole.ssz.Bytes32
import tech.pegasys.teku.phase1.onotole.ssz.Bytes48
import tech.pegasys.teku.phase1.onotole.ssz.Bytes96
import tech.pegasys.teku.phase1.onotole.ssz.SSZCollection
import tech.pegasys.teku.phase1.onotole.ssz.boolean
import tech.pegasys.teku.phase1.onotole.ssz.uint64
import tech.pegasys.teku.phase1.onotole.ssz.uint8
import tech.pegasys.teku.ssz.SSZTypes.Bytes4
import tech.pegasys.teku.ssz.backing.ViewRead
import tech.pegasys.teku.ssz.backing.view.BasicViews
import tech.pegasys.teku.ssz.backing.view.ViewUtils
import java.math.BigInteger
import tech.pegasys.teku.bls.BLS as TekuBLS
import tech.pegasys.teku.bls.BLSPublicKey as TekuBLSPublicKey
import tech.pegasys.teku.bls.BLSSecretKey as TekuBLSSecretKey
import tech.pegasys.teku.bls.BLSSignature as TekuBLSSignature

fun hash_tree_root(v: Any): Bytes32 {
  return when (v) {
    is ViewRead -> v.hashTreeRoot()
    is Byte -> BasicViews.ByteView(v).hashTreeRoot()
    is Bytes4 -> BasicViews.Bytes4View(v).hashTreeRoot()
    is uint8 -> BasicViews.ByteView(v.toByte()).hashTreeRoot()
    is uint64 -> BasicViews.UInt64View(UnsignedLong.valueOf(v.toLong())).hashTreeRoot()
    is boolean -> BasicViews.BitView(v).hashTreeRoot()
    is Bytes -> ViewUtils.createVectorFromBytes(v).hashTreeRoot()
    is BLSSignature -> ViewUtils.createVectorFromBytes(v.wrappedBytes).hashTreeRoot()
    is BLSPubkey -> ViewUtils.createVectorFromBytes(v).hashTreeRoot()
    is Bytes32 -> BasicViews.Bytes32View(v).hashTreeRoot()
    is SSZCollection<*> -> v.hashTreeRoot()
    else -> throw UnsupportedOperationException("Unsupported type ${v::class.qualifiedName}")
  }
}

fun hash(a: Bytes): Bytes32 = Hash.sha2_256(a)
fun hash(a: Bytes96): Bytes32 = hash(a.wrappedBytes)

data class FQ2(val coeffs: Pair<pyint, pyint>)

fun BIG.toPyInt(): pyint {
  val bytes = ByteArray(BIG.MODBYTES)
  this.toBytes(bytes)
  return pyint(BigInteger(1, bytes))
}

fun FP2.toFQ2(): FQ2 = FQ2(Pair(this.a.toPyInt(), this.b.toPyInt()))

object bls {
  fun Sign(privkey: pyint, message: Bytes): BLSSignature {
    return BLSSignature(
      TekuBLS.sign(TekuBLSSecretKey.fromBytes(Bytes.wrap(privkey.value.toByteArray())), message)
        .toBytes()
    )
  }

  fun Verify(pubkey: Bytes48, message: Bytes, signature: Bytes96): Boolean {
    return TekuBLS.verify(
      TekuBLSPublicKey.fromBytes(pubkey),
      message,
      TekuBLSSignature.fromBytes(signature.wrappedBytes)
    )
  }

  fun Aggregate(signatures: Collection<Bytes96>): Bytes96 {
    return BLSSignature(
      TekuBLS.aggregate(signatures.map { s -> TekuBLSSignature.fromBytes(s.wrappedBytes) })
        .toBytes()
    )
  }

  fun FastAggregateVerify(pubkeys: Collection<Bytes48>, root: Bytes, signature: Bytes96): Boolean {
    return TekuBLS.fastAggregateVerify(
      pubkeys.map { k -> TekuBLSPublicKey.fromBytes(k) }.toList(),
      root,
      TekuBLSSignature.fromBytes(signature.wrappedBytes)
    )
  }

  fun AggregateVerify(pubkyes: List<Bytes48>, messages: List<Bytes>, signature: Bytes96): boolean {
    return TekuBLS.aggregateVerify(
      pubkyes.map { p -> TekuBLSPublicKey.fromBytes(p) }.toList(),
      messages,
      TekuBLSSignature.fromBytes(signature.wrappedBytes)
    )
  }

  fun signature_to_G2(signature: Bytes96): Triple<FQ2, FQ2, FQ2> {
    val p = G2Points.fromBytesCompressed(signature.wrappedBytes)
    return Triple(p.getx().toFQ2(), p.gety().toFQ2(), p.getz().toFQ2())
  }
}
