package tech.pegasys.teku.simulation.sync

import org.assertj.core.api.Assertions
import tech.pegasys.teku.events.ChannelExceptionHandler
import java.lang.reflect.Method

enum class TestExceptionHandler : ChannelExceptionHandler {
    TEST_EXCEPTION_HANDLER;

    override fun handleException(
            error: Throwable,
            subscriber: Any,
            invokedMethod: Method,
            args: Array<Any>) {
        Assertions.fail<Void>("Unhandled error in subscriber ...", error)
    }
}