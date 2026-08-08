package com.mac.boilerplate.utils.handler;

import static org.assertj.core.api.Assertions.assertThatNoException;

import java.util.Map;
import org.junit.jupiter.api.Test;

class AsyncExceptionHandlerTest {

    @Test
    void logsWithProvidedOrGeneratedTraceAndNullableFields() {
        AsyncExceptionHandler handler = new AsyncExceptionHandler();
        assertThatNoException().isThrownBy(() -> handler.handle(
                "trace", "test", "virtual-thread", "run", Map.of("task.id", "1"),
                new IllegalStateException("failure")));
        assertThatNoException().isThrownBy(() -> handler.handle(
                null, "test", "scheduler", "run", null, new IllegalStateException("failure")));
    }
}
