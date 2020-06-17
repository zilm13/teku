package tech.pegasys.teku.phase1.onotole.deps

import org.apache.tuweni.crypto.Hash
import tech.pegasys.teku.phase1.integration.HashTreeRoot
import tech.pegasys.teku.phase1.onotole.phase1.BLSSignature
import tech.pegasys.teku.phase1.onotole.pylib.pyint
import tech.pegasys.teku.phase1.onotole.ssz.Bytes
import tech.pegasys.teku.phase1.onotole.ssz.Bytes32
import tech.pegasys.teku.phase1.onotole.ssz.Bytes48
import tech.pegasys.teku.phase1.onotole.ssz.Bytes96
import tech.pegasys.teku.phase1.onotole.ssz.boolean
import tech.pegasys.teku.bls.BLS as TekuBLS
import tech.pegasys.teku.bls.BLSPublicKey as TekuBLSPublicKey
import tech.pegasys.teku.bls.BLSSecretKey as TekuBLSSecretKey
import tech.pegasys.teku.bls.BLSSignature as TekuBLSSignature

fun hash_tree_root(a: Any): Bytes32 {
  return HashTreeRoot.compute(a)
}

fun hash(a: Bytes): Bytes32 = Hash.sha2_256(a)
fun hash(a: Bytes96): Bytes32 = hash(a.wrappedBytes)

data class FQ2(val coeffs: Pair<pyint, pyint>)

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

  fun AggregateVerify(pairs: List<Pair<Bytes48, Bytes>>, signature: Bytes96): boolean {
    return TekuBLS.aggregateVerify(
      pairs.map { p -> TekuBLSPublicKey.fromBytes(p.first) }.toList(),
      pairs.map { p -> p.second }.toList(),
      TekuBLSSignature.fromBytes(signature.wrappedBytes)
    )
  }

  fun signature_to_G2(signature: Bytes96): Triple<FQ2, FQ2, FQ2> {
    TODO("Not yet implemented")
  }
}
