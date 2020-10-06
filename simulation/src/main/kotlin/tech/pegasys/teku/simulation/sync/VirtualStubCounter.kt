package tech.pegasys.teku.simulation.sync

import org.hyperledger.besu.plugin.services.metrics.Counter
import org.hyperledger.besu.plugin.services.metrics.LabelledMetric
import java.util.Optional
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

class VirtualStubCounter : LabelledMetric<Counter> {
    private val values: MutableMap<List<String>, UnlabelledCounter> = ConcurrentHashMap()
    override fun labels(vararg labels: String): Counter {
        return values.computeIfAbsent(listOf(*labels)) { UnlabelledCounter() }
    }

    fun getValue(vararg labels: String?): Long {
        return Optional.ofNullable(values[listOf(*labels)])
                .map { obj: UnlabelledCounter -> obj.getValue() }
                .orElse(0L)
    }

    private class UnlabelledCounter : Counter {
        private val value = AtomicLong()
        override fun inc() {
            value.incrementAndGet()
        }

        override fun inc(amount: Long) {
            value.updateAndGet { value: Long -> value + amount }
        }

        fun getValue(): Long {
            return value.get()
        }
    }
}
