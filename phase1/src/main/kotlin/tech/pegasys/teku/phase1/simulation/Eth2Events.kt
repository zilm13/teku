package tech.pegasys.teku.phase1.simulation

import tech.pegasys.teku.phase1.integration.datastructures.FullAttestation
import tech.pegasys.teku.phase1.integration.datastructures.SignedBeaconBlock
import tech.pegasys.teku.phase1.integration.datastructures.SignedShardBlock
import tech.pegasys.teku.phase1.onotole.phase1.GENESIS_SLOT
import tech.pegasys.teku.phase1.onotole.phase1.Root
import tech.pegasys.teku.phase1.onotole.phase1.Slot
import tech.pegasys.teku.phase1.util.printRoot

sealed class Eth2Event

open class NewSlot(val slot: Slot) : Eth2Event() {
  override fun toString(): String {
    return "NewSlot(slot=$slot)"
  }
}

class SlotTerminal(val slot: Slot) : Eth2Event() {
  override fun toString(): String {
    return "SlotTerminal(slot=$slot)"
  }
}

class PrevSlotAttestationsPublished(val attestations: List<FullAttestation>) : Eth2Event() {
  override fun toString(): String {
    return "PrevSlotAttestationsPublished(attestations=${attestations.size})"
  }
}

abstract class NewHead(val head: BeaconHead) : Eth2Event()
class HeadAfterAttestationsApplied(head: BeaconHead) : NewHead(head) {
  override fun toString(): String {
    return "HeadAfterAttestationsApplied(root=${printRoot(head.root)})"
  }
}

class HeadAfterNewBeaconBlock(head: BeaconHead) : NewHead(head) {
  override fun toString(): String {
    return "HeadAfterNewBeaconBlock(root=${printRoot(head.root)})"
  }
}

class NewBeaconBlock(val block: SignedBeaconBlock) : Eth2Event() {
  override fun toString(): String {
    return "NewBeaconBlock(root=${printRoot(block.hashTreeRoot())})"
  }
}

class NewShardBlocks(val blocks: List<SignedShardBlock>) : Eth2Event() {
  override fun toString(): String {
    return "NewShardBlocks(blocks=${blocks.size})"
  }
}

class NotCrosslinkedBlocksPublished(val blocks: List<SignedShardBlock>) : Eth2Event() {
  override fun toString(): String {
    return "NotCrosslinkedBlocksPublished(blocks=${blocks.size})"
  }
}

class NewAttestations(val attestations: List<FullAttestation>) : Eth2Event() {
  override fun toString(): String {
    return "NewAttestations(attestations=${attestations.size})"
  }
}

class NewShardHeads(val shardHeadRoots: List<Root>) : Eth2Event() {
  override fun toString(): String {
    return "NewShardHeads(shardHeadRoots=${shardHeadRoots.size})"
  }
}

object GenesisSlotEvent : NewSlot(GENESIS_SLOT)
