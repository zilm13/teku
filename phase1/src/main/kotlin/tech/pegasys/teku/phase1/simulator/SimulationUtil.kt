package tech.pegasys.teku.phase1.simulator

import tech.pegasys.teku.phase1.onotole.phase1.GENESIS_SLOT
import tech.pegasys.teku.phase1.onotole.phase1.Slot

fun runsOutOfSlots(currentSlot: Slot, slotsToRun: Slot): Boolean =
  currentSlot + 1uL >= GENESIS_SLOT + slotsToRun
