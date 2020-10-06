package tech.pegasys.teku.simulation.sync

import org.hyperledger.besu.plugin.services.metrics.MetricCategory
import java.util.function.DoubleSupplier
abstract class StubMetric protected constructor(val category: MetricCategory, val name: String, val help: String)

class VirtualStubGauge(
        category: MetricCategory,
        name: String,
        help: String,
        private val supplier: DoubleSupplier) : StubMetric(category, name, help) {
    val value: Double
        get() = supplier.asDouble
}
