package tech.pegasys.teku.simulation.util

import org.awaitility.Awaitility
import org.awaitility.pollinterval.IterativePollInterval
import java.time.Duration
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

/**
 * A simpler wrapper around Awaitility that directs people towards best practices for waiting. The
 * native Awaitility wrapper has a number of "gotchas" that can lead to intermittency which this
 * wrapper aims to prevent.
 */
public class Waiter {
    @JvmOverloads
    @Throws(InterruptedException::class)
    fun ensureConditionRemainsMet(
            condition: Condition, waitTimeInMilliseconds: Int = 2000) {
        val mustBeTrueUntil = System.currentTimeMillis() + waitTimeInMilliseconds
        while (System.currentTimeMillis() < mustBeTrueUntil) {
            try {
                condition.run()
            } catch (t: Throwable) {
                throw RuntimeException("Condition did not remain met", t)
            }
            Thread.sleep(500)
        }
    }

    fun interface Condition {
        @Throws(Throwable::class)
        fun run()
    }

    companion object {
        private const val DEFAULT_TIMEOUT_SECONDS = 30
        private val INITIAL_POLL_INTERVAL = Duration.ofMillis(200)
        private val MAX_POLL_INTERVAL = Duration.ofSeconds(5)

        fun waitFor(
                assertion: Condition, timeoutValue: Int = DEFAULT_TIMEOUT_SECONDS, timeUnit: TimeUnit? = TimeUnit.SECONDS) {
            Awaitility.waitAtMost(timeoutValue.toLong(), timeUnit)
                    .ignoreExceptions()
                    .pollInterval(
                            IterativePollInterval.iterative(this::nextPollInterval, INITIAL_POLL_INTERVAL))
                    .untilAsserted { assertion.run() }
        }

        private fun nextPollInterval(duration: Duration): Duration {
            val nextInterval = duration.multipliedBy(2)
            return if (nextInterval.compareTo(MAX_POLL_INTERVAL) <= 0) nextInterval else MAX_POLL_INTERVAL
        }

        fun <T> waitFor(future: Future<T>, timeoutValue: Int = DEFAULT_TIMEOUT_SECONDS, timeUnit: TimeUnit = TimeUnit.SECONDS): T {
            return future[timeoutValue.toLong(), timeUnit]
        }

        fun waitFor(assertion: Condition) {
            waitFor(assertion, DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        }
    }
}