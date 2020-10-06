package tech.pegasys.teku.simulation.util

import tech.pegasys.teku.bls.BLSKeyPair
import java.security.SecureRandom
import java.util.stream.Collectors
import java.util.stream.Stream

object BLSKeyGenerator {
    fun generateKeyPairs(count: Int, rnd: SecureRandom): List<BLSKeyPair> {
        return Stream.generate { BLSKeyPair.random(rnd) }.limit(count.toLong()).collect(Collectors.toList())
    }
}