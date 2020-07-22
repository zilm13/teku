package tech.pegasys.teku.phase1.eth1client

import org.apache.tuweni.crypto.Hash
import org.apache.tuweni.rlp.RLP
import org.bouncycastle.jce.provider.BouncyCastleProvider
import tech.pegasys.teku.phase1.onotole.ssz.Bytes
import tech.pegasys.teku.phase1.onotole.ssz.Bytes32
import java.security.Security

val EMPTY_TRIE_HASH = Hash.keccak256(RLP.encodeValue(Bytes.EMPTY))

fun computeEth1BlockHash(rlp: Bytes): Bytes32 = Hash.keccak256(rlp)

fun parseEth1BlockDataFromRLP(rlp: Bytes): Eth1BlockData {
  val blockHeader = RLP.decodeList(rlp) { it.readValue() }
  val blockHash = computeEth1BlockHash(blockHeader)

  return RLP.decodeList(blockHeader) {
    val parentHash = Bytes32.wrap(it.readValue())   // parentHash       = rlpList[0]
    it.readValue()                                  // uncleHash        = rlpList[1]
    it.readValue()                                  // coinbase         = rlpList[2]
    val stateRoot = Bytes32.wrap(it.readValue())    // stateRoot        = rlpList[3]
    it.readValue()                                  // txTrieRoot       = rlpList[4]
    val receiptsRootValue = it.readValue()          // receiptTrieRoot  = rlpList[5]
    it.readValue()                                  // logsBloom        = rlpList[6]
    it.readValue()                                  // difficulty       = rlpList[7]
    val number = it.readLong().toULong()            // number           = rlpList[8]

    val receiptsRoot = if (receiptsRootValue.isEmpty) {
      EMPTY_TRIE_HASH
    } else {
      Bytes32.wrap(receiptsRootValue)
    }

    Eth1BlockData(blockHash, parentHash, stateRoot, receiptsRoot, number, rlp)
  }
}
