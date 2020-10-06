package tech.pegasys.teku.simulation.sync

import com.google.common.annotations.VisibleForTesting
import org.apache.logging.log4j.LogManager
import tech.pegasys.teku.util.async.AsyncRunner
import tech.pegasys.teku.util.async.SafeFuture
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.TimeUnit
import java.util.function.Supplier


/**
 * An AsyncRunner that uses the common ForkJoinPool so that it is guaranteed not to leak threads
 * even if the test doesn't shut it down.
 */
class DelayedExecutorAsyncRunner private constructor(private val executorFactory: ExecutorFactory) : AsyncRunner {
    override fun <U> runAsync(action: Supplier<SafeFuture<U>>): SafeFuture<U> {
        val executor = asyncExecutor
        return runAsync(action, executor)
    }

    override fun <U> runAfterDelay(
            action: Supplier<SafeFuture<U>>, delayAmount: Long, delayUnit: TimeUnit): SafeFuture<U> {
        val executor = getDelayedExecutor(delayAmount, delayUnit)
        return runAsync(action, executor)
    }

    override fun shutdown() {}

    @VisibleForTesting
    fun <U> runAsync(action: Supplier<SafeFuture<U>>, executor: Executor): SafeFuture<U> {
        val result = SafeFuture<U>()
        try {
            executor.execute { SafeFuture.ofComposed { action.get() }.propagateTo(result) }
        } catch (ex: RejectedExecutionException) {
            LOG.debug("shutting down ", ex)
        } catch (t: Throwable) {
            result.completeExceptionally(t)
        }
        return result
    }

    private val asyncExecutor: Executor
        private get() = getDelayedExecutor(-1, TimeUnit.SECONDS)

    private fun getDelayedExecutor(delayAmount: Long, delayUnit: TimeUnit): Executor {
        return executorFactory.create(delayAmount, delayUnit)
    }

    private interface ExecutorFactory {
        fun create(delayAmount: Long, delayUnit: TimeUnit): Executor
    }

    private class DelayedExecutor : ExecutorFactory {
        override fun create(delayAmount: Long, delayUnit: TimeUnit): Executor {
            return CompletableFuture.delayedExecutor(delayAmount, delayUnit)
        }
    }

    companion object {
        private val LOG = LogManager.getLogger()
        fun create(): DelayedExecutorAsyncRunner {
            return DelayedExecutorAsyncRunner(DelayedExecutor())
        }
    }

}