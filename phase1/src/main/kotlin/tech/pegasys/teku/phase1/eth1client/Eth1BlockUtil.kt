package tech.pegasys.teku.phase1.eth1client

import org.apache.tuweni.crypto.Hash
import tech.pegasys.teku.phase1.onotole.ssz.Bytes
import tech.pegasys.teku.phase1.onotole.ssz.Bytes32
import tech.pegasys.teku.phase1.onotole.ssz.uint64

fun hash(rlp: Bytes): Bytes32 = Hash.sha2_256(rlp)

fun encodeBlockDataWithRLP(
  number: uint64,
  parentHash: Bytes32,
  stateRoot: Bytes32,
  receiptsRoot: Bytes32,
  body: Bytes
): Bytes {
  return Bytes.concatenate(
    Bytes.ofUnsignedLong(number.toLong()),
    parentHash,
    stateRoot,
    receiptsRoot,
    body
  )
}

fun parseBlockNumberFromRLP(rlp: Bytes): uint64 {
  return rlp.slice(0, 8).toLong().toULong()
}

fun parseParentHashFromRLP(rlp: Bytes): Bytes32 {
  return Bytes32.wrap(rlp.slice(8, Bytes32.SIZE))
}
