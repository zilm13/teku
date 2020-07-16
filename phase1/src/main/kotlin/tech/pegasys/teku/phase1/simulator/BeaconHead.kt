package tech.pegasys.teku.phase1.simulator

import tech.pegasys.teku.phase1.integration.datastructures.BeaconState
import tech.pegasys.teku.phase1.onotole.phase1.Root

data class BeaconHead(
  val root: Root,
  val state: BeaconState
)
