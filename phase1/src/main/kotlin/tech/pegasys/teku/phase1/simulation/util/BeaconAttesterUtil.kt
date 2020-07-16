package tech.pegasys.teku.phase1.simulation.util

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import tech.pegasys.teku.phase1.integration.datastructures.BeaconState
import tech.pegasys.teku.phase1.integration.datastructures.Checkpoint
import tech.pegasys.teku.phase1.integration.datastructures.FullAttestation
import tech.pegasys.teku.phase1.integration.datastructures.FullAttestationData
import tech.pegasys.teku.phase1.integration.datastructures.SignedShardBlock
import tech.pegasys.teku.phase1.integration.ssz.SSZBitlistImpl
import tech.pegasys.teku.phase1.onotole.deps.bls
import tech.pegasys.teku.phase1.onotole.phase1.BLSSignature
import tech.pegasys.teku.phase1.onotole.phase1.CommitteeIndex
import tech.pegasys.teku.phase1.onotole.phase1.MAX_VALIDATORS_PER_COMMITTEE
import tech.pegasys.teku.phase1.onotole.phase1.Root
import tech.pegasys.teku.phase1.onotole.phase1.ValidatorIndex
import tech.pegasys.teku.phase1.onotole.phase1.compute_shard_from_committee_index
import tech.pegasys.teku.phase1.onotole.phase1.compute_start_slot_at_epoch
import tech.pegasys.teku.phase1.onotole.phase1.get_attestation_signature
import tech.pegasys.teku.phase1.onotole.phase1.get_block_root
import tech.pegasys.teku.phase1.onotole.phase1.get_current_epoch
import tech.pegasys.teku.phase1.onotole.phase1.get_shard_transition

suspend fun computeAggregateAttestationByCommittee(
  committeeIndex: CommitteeIndex,
  committee: List<ValidatorIndex>,
  headRoot: Root,
  headState: BeaconState,
  shardHeadRoot: Root,
  shardBlocksToCrosslink: List<SignedShardBlock>,
  secretKeys: SecretKeyRegistry
): FullAttestation {
  val attestationData =
    computeAttestationData(
      committeeIndex,
      headRoot,
      headState,
      shardHeadRoot,
      shardBlocksToCrosslink
    )

  val res: Pair<SSZBitlistImpl, List<BLSSignature>>
  coroutineScope {
    res = committee.map {
      async { it to get_attestation_signature(headState, attestationData, secretKeys[it]) }
    }.awaitAll().fold(
      SSZBitlistImpl(MAX_VALIDATORS_PER_COMMITTEE) to mutableListOf<BLSSignature>()
    ) { acc, pair ->
      acc.first.set(pair.first)
      acc.second.add(pair.second)
      acc.first to acc.second
    }
  }

  val (aggregationBits, signatureAggregate) = res.first to bls.Aggregate(res.second)
  return FullAttestation(aggregationBits, attestationData, signatureAggregate)
}

fun computeAttestationData(
  committeeIndex: CommitteeIndex,
  headRoot: Root,
  headState: BeaconState,
  shardHeadRoot: Root,
  shardBlocksToCrosslink: List<SignedShardBlock>
): FullAttestationData {
  val startSlot = compute_start_slot_at_epoch(get_current_epoch(headState))
  val shard = compute_shard_from_committee_index(headState, committeeIndex, headState.slot)
  val epochBoundaryBlockRoot =
    if (startSlot == headState.slot) headRoot else get_block_root(
      headState,
      get_current_epoch(headState)
    )
  val shardTransition = get_shard_transition(headState, shard, shardBlocksToCrosslink)
  return FullAttestationData(
    headState.slot,
    committeeIndex,
    headRoot,
    headState.current_justified_checkpoint,
    Checkpoint(epoch = get_current_epoch(headState), root = epochBoundaryBlockRoot),
    shard,
    shardHeadRoot,
    shardTransition
  )
}
