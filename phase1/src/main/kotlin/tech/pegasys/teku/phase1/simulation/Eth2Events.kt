package tech.pegasys.teku.phase1.simulation

import tech.pegasys.teku.phase1.integration.datastructures.FullAttestation
import tech.pegasys.teku.phase1.integration.datastructures.SignedBeaconBlock
import tech.pegasys.teku.phase1.integration.datastructures.SignedShardBlock
import tech.pegasys.teku.phase1.onotole.phase1.GENESIS_SLOT
import tech.pegasys.teku.phase1.onotole.phase1.Root
import tech.pegasys.teku.phase1.onotole.phase1.Slot

sealed class Eth2Event

open class NewSlot(val slot: Slot) : Eth2Event()
class SlotTerminal(val slot: Slot) : Eth2Event()
class PrevSlotAttestationsPublished(val attestations: List<FullAttestation>) : Eth2Event()
abstract class NewHead(val head: BeaconHead) : Eth2Event()
class HeadAfterAttestationsApplied(head: BeaconHead) : NewHead(head)
class HeadAfterNewBeaconBlock(head: BeaconHead) : NewHead(head)
class NewBeaconBlock(val block: SignedBeaconBlock) : Eth2Event()
class NewShardBlocks(val blocks: List<SignedShardBlock>) : Eth2Event()
class NotCrosslinkedBlocksPublished(val blocks: List<SignedShardBlock>) : Eth2Event()
class NewAttestations(val attestations: List<FullAttestation>) : Eth2Event()
class NewShardHeads(val shardHeadRoots: List<Root>) : Eth2Event()

object GenesisSlotEvent : NewSlot(GENESIS_SLOT)
