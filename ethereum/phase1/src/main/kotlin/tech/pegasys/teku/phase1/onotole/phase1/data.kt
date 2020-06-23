package tech.pegasys.teku.phase1.onotole.phase1

import tech.pegasys.teku.phase1.onotole.ssz.Bytes32
import tech.pegasys.teku.phase1.onotole.ssz.Bytes4
import tech.pegasys.teku.phase1.onotole.ssz.Bytes48
import tech.pegasys.teku.phase1.onotole.ssz.Bytes96
import tech.pegasys.teku.phase1.onotole.ssz.uint64
import tech.pegasys.teku.phase1.onotole.ssz.uint8

typealias Slot = uint64

fun Slot(x: uint64): Slot = x
fun Slot() = Slot(0uL)

typealias Epoch = uint64

fun Epoch(x: uint64): Epoch = x
fun Epoch() = Epoch(0uL)

typealias CommitteeIndex = uint64

fun CommitteeIndex(x: uint64): CommitteeIndex = x
fun CommitteeIndex() = CommitteeIndex(0uL)

typealias ValidatorIndex = uint64

fun ValidatorIndex(x: uint64): ValidatorIndex = x
fun ValidatorIndex() = ValidatorIndex(0uL)

typealias Gwei = uint64

fun Gwei(x: uint64): Gwei = x
fun Gwei() = Gwei(0uL)

typealias Root = Bytes32

fun Root(x: Bytes32): Root = x
fun Root() = Root(Bytes32())

typealias Version = Bytes4

fun Version(x: Bytes4): Version = x
fun Version() = Version(Bytes4())

typealias DomainType = Bytes4

fun DomainType(x: Bytes4): DomainType = x
fun DomainType() = DomainType(Bytes4())

typealias ForkDigest = Bytes4

fun ForkDigest(x: Bytes4): ForkDigest = x
fun ForkDigest() = ForkDigest(Bytes4())

typealias Domain = Bytes32

fun Domain(x: Bytes32): Domain = x
fun Domain() = Domain(Bytes32())

typealias BLSPubkey = Bytes48

fun BLSPubkey(x: Bytes48): BLSPubkey = x
fun BLSPubkey() = BLSPubkey(Bytes48())

typealias BLSSignature = Bytes96

fun BLSSignature(x: Bytes96): BLSSignature = x
fun BLSSignature() = BLSSignature(Bytes96())

typealias Shard = uint64

fun Shard(x: uint64): Shard = x
fun Shard() = Shard(0uL)

typealias OnlineEpochs = uint8

fun OnlineEpochs(x: uint8): OnlineEpochs = x
fun OnlineEpochs() = OnlineEpochs(0u.toUByte())
