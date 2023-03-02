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
        fun `Deferred will be not caught by try-catch for async and hoisted`() {
            var exceptionHandled = false
            val handler = CoroutineExceptionHandler { _, _ -> exceptionHandled = true }
            testScope(handler).launch {
                try {
                    val receiver = async { throw Exception() }
                } catch (e: Throwable) {
                    failInCoroutine { "It must not be caught" }
                }
            }
            simulate {
                assertThat(exceptionHandled).isTrue
            }
        }
        @Test
        fun `Deferred will be caught by try-catch for await and hoisted too`() {
            var exceptionHandled = false
            var exceptionCaught = false
            val receivedHandler = CoroutineExceptionHandler { _, _ -> exceptionHandled = true }
            testScope(receivedHandler).launch {
                val receiver = async { throw Exception() }
                try {
                    receiver.await()
                } catch(e: Throwable) {
                    exceptionCaught = true
                }
            }
            simulate {
                assertThat(exceptionHandled).isTrue
                assertThat(exceptionCaught).isTrue
            }
        }
        @Test
        fun `Deferred with yield between async ~ await will not be caught in try-catch`() {
            var exceptionHandled = false
            val handler = CoroutineExceptionHandler { _, _ -> exceptionHandled = true }
            testScope(handler).launch {
                val receiver = async { throw Exception() }
                yield()
                try {
                    receiver.await()
                } catch(e: Throwable) {
                    failInCoroutine { "It must not be caught because of yield()" }
                }
            }
            simulate {
                assertThat(exceptionHandled).isTrue
            }
        }
    }

    @DisplayName("Async in coroutineScope")
    @Nested
    inner class AsyncInCoroutineScopeTest {
        @Test
        fun `coroutineScope will hoist exception from child to parent scope`() {
            var exceptionHandled = false
            val handler = CoroutineExceptionHandler { _, _ -> exceptionHandled = true }
            testScope(handler).launch {
                coroutineScope {
                    launch { throw Exception() }
                }
            }
            simulate {
                assertThat(exceptionHandled).isTrue
            }
        }
        @Test
        fun `If coroutineScope is wrapped by try-catch, exception will caught by catch block`() {
            var exceptionHandled = false
            var exceptionCaught = false
            val handler = CoroutineExceptionHandler { _, _ -> exceptionHandled = true }
            val asyncHandler = CoroutineExceptionHandler { _, _ -> failInCoroutine { "It must not be hoisted !" } }
            testScope(handler).launch {
                coroutineScope {
                    try {
                        async { throw Exception() }
                    } catch (e: Throwable) {
                        failInCoroutine { "It must not be caught !" }
                    }
                }
            }
            testScope(asyncHandler).launch {
                try {
                    coroutineScope {
                        async { throw Exception() }
                    }
                } catch (e: Throwable) {
                    exceptionCaught = true
                }
            }
            simulate {
                assertThat(exceptionHandled).isTrue
                assertThat(exceptionCaught).isTrue
            }
        }
    }

    @DisplayName("Async in supervisorScope")
    @Nested
    inner class AsyncInSupervisorScopeTest {
        @Test
        fun `supervisorScope will hoist exception from child to parent scope`() {
            var exceptionHandled = false
            val handler = CoroutineExceptionHandler { _, _ -> exceptionHandled = true }
            testScope(handler).launch {
                supervisorScope {
                    launch { throw Exception() }
                }
            }
            simulate {
                assertThat(exceptionHandled).isTrue
            }
        }
        @Test
        fun `If async in supervisorScope is wrapped by try-catch, it won't hoist exception to parent scope`() {
            var exceptionHandled = false
            val handler = CoroutineExceptionHandler { _, _ -> failInCoroutine { "It must not be hoisted !" } }
            testScope(handler).launch {
                supervisorScope {
                    try {
                        async { throw Exception() }
                    } catch (e: Throwable) {
                        failInCoroutine { "It must not be caught !" }
                    }
                }
            }
            simulate {
                assertThat(exceptionHandled).isFalse
            }
        }
        @Test
        fun `If await in supervisorScope is wrapped by try-catch, it won't hoist exception to parent scope`() {
            var exceptionCaught = false
            val handler = CoroutineExceptionHandler { _, _ -> failInCoroutine { "It must not be hoisted !" } }
            testScope(handler).launch {
                supervisorScope {
                    val deferred = async { throw Exception() }
                    try {
                        deferred.await()
                    } catch (e: Throwable) {
                        exceptionCaught = true
                    }
                }
            }
            simulate {
                assertThat(exceptionCaught).isTrue
            }
        }
        @Test
        fun `If throwable supervisorScope is wrapped by try-catch, it won't hoist exception to parent scope`() {
            var exceptionCaught = false
            val handler = CoroutineExceptionHandler { _, _ -> failInCoroutine { "It must not be hoisted !" } }
            testScope(handler).launch {
                try {
                    supervisorScope {
                        val deferred = async { throw Exception() }
                        deferred.await()
                    }
                } catch (e: Throwable) {
                    exceptionCaught = true
                }
            }
            simulate {
                assertThat(exceptionCaught).isTrue
            }
        }
    }
}
