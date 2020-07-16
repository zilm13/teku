package tech.pegasys.teku.phase1.simulation.util

import tech.pegasys.teku.bls.BLSKeyPair
import tech.pegasys.teku.phase1.onotole.phase1.ValidatorIndex
import tech.pegasys.teku.phase1.onotole.pylib.pyint

class SecretKeyRegistry(private val blsKeyPairs: List<BLSKeyPair>) {
  operator fun get(i: ValidatorIndex): pyint {
    return toPyint(blsKeyPairs[i.toInt()].secretKey)
  }
}
