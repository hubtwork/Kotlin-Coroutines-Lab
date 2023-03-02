package com.hubtwork.examples.testcases.exceptionhandler

import com.hubtwork.examples.testcases.CoroutineTestSuite
import com.hubtwork.examples.testcases.testScope
import kotlinx.coroutines.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ExceptionWithAsyncTest: CoroutineTestSuite() {
    @DisplayName("Async basic test")
    @Nested
    inner class AsyncTest {
        @Test
        fun `test`() {
            var exceptionHandled = false
            val handler = CoroutineExceptionHandler { _, _ -> exceptionHandled = true }
            testScope(handler).launch {
                async { throw Exception() }
            }
            simulate {
                assertThat(exceptionHandled).isTrue
            }
        }
    }
}
