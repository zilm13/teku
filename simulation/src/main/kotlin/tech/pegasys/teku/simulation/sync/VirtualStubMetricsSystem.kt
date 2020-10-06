package tech.pegasys.teku.simulation.sync

import org.hyperledger.besu.plugin.services.MetricsSystem
import org.hyperledger.besu.plugin.services.metrics.Counter
import org.hyperledger.besu.plugin.services.metrics.LabelledMetric
import org.hyperledger.besu.plugin.services.metrics.MetricCategory
import org.hyperledger.besu.plugin.services.metrics.OperationTimer
import java.util.Optional
import java.util.concurrent.ConcurrentHashMap
import java.util.function.DoubleSupplier
import java.util.function.Function
import java.util.function.Supplier


class VirtualStubMetricsSystem : MetricsSystem {
    private val counters: MutableMap<MetricCategory, MutableMap<String, VirtualStubCounter>> = ConcurrentHashMap<MetricCategory, MutableMap<String, VirtualStubCounter>>()
    private val gauges: MutableMap<MetricCategory, MutableMap<String, VirtualStubGauge>> = ConcurrentHashMap<MetricCategory, MutableMap<String, VirtualStubGauge>>()
    override fun createLabelledCounter(
            category: MetricCategory,
            name: String,
            help: String,
            vararg labelNames: String): LabelledMetric<Counter> {
        return counters
                .computeIfAbsent(category) { ConcurrentHashMap<String, VirtualStubCounter>() }
                .computeIfAbsent(name) { VirtualStubCounter() }
    }

    override fun createGauge(
            category: MetricCategory,
            name: String,
            help: String,
            valueSupplier: DoubleSupplier) {
        val guage = VirtualStubGauge(category, name, help, valueSupplier)
        val gaugesInCategory: MutableMap<String, VirtualStubGauge> = gauges.computeIfAbsent(category, Function<MetricCategory, MutableMap<String, VirtualStubGauge>> { key: MetricCategory -> ConcurrentHashMap<String, VirtualStubGauge>() })
        require(gaugesInCategory.putIfAbsent(name, guage) == null) { "Attempting to create two gauges with the same name" }
    }

    override fun createLabelledTimer(
            category: MetricCategory,
            name: String,
            help: String,
            vararg labelNames: String): LabelledMetric<OperationTimer> {
        throw UnsupportedOperationException("Timers not supported")
    }

    fun getGauge(category: MetricCategory, name: String): VirtualStubGauge {
        return Optional.ofNullable(gauges[category])
                .map { categoryGauges: Map<String, VirtualStubGauge> -> categoryGauges[name] }
                .orElseThrow { IllegalArgumentException("Unknown guage: $category $name") }!!
    }

    fun getCounter(category: MetricCategory, name: String): VirtualStubCounter {
        return Optional.ofNullable(counters[category])
                .map(Function<Map<String, VirtualStubCounter>, VirtualStubCounter> { categoryCounters: Map<String, VirtualStubCounter> -> categoryCounters[name] })
                .orElseThrow<IllegalArgumentException>(
                        Supplier { IllegalArgumentException("Unknown counter: $category $name") })
    }
}