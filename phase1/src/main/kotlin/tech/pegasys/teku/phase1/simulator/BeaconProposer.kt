package tech.pegasys.teku.phase1.simulator

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.SendChannel
import tech.pegasys.teku.phase1.integration.datastructures.Attestation
import tech.pegasys.teku.phase1.integration.datastructures.BeaconState
import tech.pegasys.teku.phase1.integration.datastructures.FullAttestation
import tech.pegasys.teku.phase1.onotole.phase1.GENESIS_SLOT
import tech.pegasys.teku.phase1.onotole.phase1.Root
import tech.pegasys.teku.phase1.onotole.phase1.Slot

class BeaconProposer(
  eventBus: SendChannel<Eth2Event>,
  private val secretKeys: SecretKeyRegistry
) : Eth2Actor(eventBus) {

  private var recentSlot = GENESIS_SLOT
  private var recentAttestations = listOf<FullAttestation>()

  override suspend fun dispatchImpl(event: Eth2Event, scope: CoroutineScope) {
    when (event) {
      is NewSlot -> onNewSlot(event.slot)
      is PrevSlotAttestationsPublished -> onPrevSlotAttestationsPublished(event.attestations)
      is HeadAfterAttestationsApplied -> onHeadAfterAttestationsApplied(event.head)
      is SlotTerminal -> onSlotTerminal()
    }
  }

  private fun onNewSlot(slot: Slot) {
    this.recentSlot = slot
  }

  private suspend fun onHeadAfterAttestationsApplied(head: BeaconHead) {
    if (recentSlot > GENESIS_SLOT) {
      proposeBlock(head.root, head.state)
    }
  }

  private suspend fun proposeBlock(headRoot: Root, headState: BeaconState) {
    val attestations = recentAttestations.map { Attestation(it) }
    val shardTransitions = recentAttestations.map { it.data.shard_transition }
    val newBlock =
      produceBlock(headState, recentSlot, headRoot, attestations, shardTransitions, secretKeys)
    publish(NewBeaconBlock(newBlock))
  }

  private fun onSlotTerminal() {
    this.recentAttestations = listOf()
  }

  private fun onPrevSlotAttestationsPublished(attestations: List<FullAttestation>) {
    this.recentAttestations = attestations
  }
}
