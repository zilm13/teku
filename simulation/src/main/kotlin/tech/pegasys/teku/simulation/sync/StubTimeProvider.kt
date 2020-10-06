package tech.pegasys.teku.simulation.sync

import com.google.common.primitives.UnsignedLong
import tech.pegasys.teku.util.time.TimeProvider
import java.util.concurrent.TimeUnit

class StubTimeProvider private constructor(private var timeInMillis: UnsignedLong) : TimeProvider {
    fun advanceTimeBySeconds(seconds: Long) {
        advanceTimeByMillis(TimeUnit.SECONDS.toMillis(seconds))
    }

    fun advanceTimeByMillis(millis: Long) {
        timeInMillis = timeInMillis.plus(UnsignedLong.valueOf(millis))
    }

    override fun getTimeInMillis(): UnsignedLong {
        return timeInMillis
    }

    companion object {
        fun withTimeInSeconds(timeInSeconds: Long): StubTimeProvider {
            return withTimeInSeconds(UnsignedLong.valueOf(timeInSeconds))
        }

        fun withTimeInSeconds(timeInSeconds: UnsignedLong): StubTimeProvider {
            return withTimeInMillis(timeInSeconds.times(TimeProvider.MILLIS_PER_SECOND))
        }

        fun withTimeInMillis(timeInMillis: Long): StubTimeProvider {
            return withTimeInMillis(UnsignedLong.valueOf(timeInMillis))
        }

        fun withTimeInMillis(timeInMillis: UnsignedLong): StubTimeProvider {
            return StubTimeProvider(timeInMillis)
        }
    }

}
