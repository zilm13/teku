package tech.pegasys.teku.phase1.simulation.util

import tech.pegasys.teku.bls.BLSSecretKey
import tech.pegasys.teku.phase1.onotole.pylib.pyint
import java.math.BigInteger

fun toPyint(secretKey: BLSSecretKey) = pyint(BigInteger(1, secretKey.toBytes().toArray()))
