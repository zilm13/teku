package tech.pegasys.teku.phase1.onotole.phase0

import tech.pegasys.teku.phase1.onotole.phase1.SLOTS_PER_EPOCH

/*
  Return the epoch number at ``slot``.
  */
fun compute_epoch_at_slot(slot: Slot): Epoch {
  return tech.pegasys.teku.phase1.onotole.phase1.Epoch((slot / SLOTS_PER_EPOCH))
}

/*
    Return the current epoch.
    */
fun get_current_epoch(state: BeaconState): Epoch {
  return compute_epoch_at_slot(state.slot)
}

