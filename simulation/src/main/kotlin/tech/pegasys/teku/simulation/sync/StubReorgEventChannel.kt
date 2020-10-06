package tech.pegasys.teku.simulation.sync

import com.google.common.primitives.UnsignedLong
import org.apache.tuweni.bytes.Bytes32
import tech.pegasys.teku.storage.api.ReorgEventChannel

class StubReorgEventChannel : ReorgEventChannel {
    override fun reorgOccurred(bestBlockRoot: Bytes32, bestSlot: UnsignedLong) {}
}
