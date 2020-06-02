package tech.pegasys.teku.phase1.core

import tech.pegasys.teku.phase1.ssz.Bytes
import tech.pegasys.teku.phase1.ssz.Bytes1
import tech.pegasys.teku.phase1.ssz.Bytes32
import tech.pegasys.teku.phase1.ssz.Bytes4
import tech.pegasys.teku.phase1.ssz.uint64

fun ValidatorIndex(x: Int): ValidatorIndex = x.toULong()
fun Domain(x: Bytes): Domain = Domain(Bytes32.wrap(x))
fun DomainType(x: String): DomainType = Bytes4.fromHexString(x)
fun Version(x: String): Version = Bytes4.fromHexString(x)
fun Bytes1(x: String): Bytes1 = Bytes.fromHexString(x)[0]
fun OnlineEpochs(x: uint64): OnlineEpochs = x.toUByte()
