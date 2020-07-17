package tech.pegasys.teku.phase1.onotole.phase1

import tech.pegasys.teku.phase1.integration.datastructures.BeaconState
import tech.pegasys.teku.phase1.integration.datastructures.PendingAttestation
import tech.pegasys.teku.phase1.onotole.ssz.Sequence

fun group_attestations_by_validator_index(
  state: BeaconState,
  attestations: Sequence<PendingAttestation>
): Map<ValidatorIndex, Sequence<PendingAttestation>> {
  return attestations.groupBy { it.data.slot to it.data.index }
    .map { get_beacon_committee(state, it.key.first, it.key.second) to it.value }
    .map {
      it.second.map { a ->
        a.aggregation_bits.bitsSet().map { indexWithinCommittee ->
          it.first[indexWithinCommittee.toInt()]
        } to a
      }
    }
    .flatten().map {
      it.first.map { validatorIndex ->
        validatorIndex to it.second
      }
    }
    .flatten().groupBy({ it.first }, { it.second })
}
