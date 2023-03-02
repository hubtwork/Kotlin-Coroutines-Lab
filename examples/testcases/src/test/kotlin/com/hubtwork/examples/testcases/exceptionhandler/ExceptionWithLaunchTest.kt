package com.hubtwork.examples.testcases.exceptionhandler

import com.hubtwork.examples.testcases.CoroutineTestSuite
import com.hubtwork.examples.testcases.testScope
import kotlinx.coroutines.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.IOException

class ExceptionWithLaunchTest: CoroutineTestSuite() {
    @DisplayName("Launch basic test")
    @Nested
    inner class LaunchTest {
        @Test
        fun `Exception in launch block can caught by compound context with handler`() {
            var exceptionHandled = false
            var handledException: Throwable? = null
            val handler = CoroutineExceptionHandler { _, throwable ->
                exceptionHandled = true
                handledException = throwable
            }
            testScope(handler).launch { throw ArithmeticException("Please catch me") }
            simulate {
                assertThat(exceptionHandled).isTrue
                assertThat(handledException).isInstanceOf(ArithmeticException::class.java)
            }
        }
        @Test
        fun `Exception wrapped with try-catch in launch block can prevent hoisting`() {
            var exceptionCaught = false
            val handler = CoroutineExceptionHandler { _, _ -> failInCoroutine { "Exception must not be hoisted." } }
            testScope(handler).launch {
                try {
                    throw IOException()
                } catch(e: Throwable) {
                    exceptionCaught = true
                }
            }
            simulate {
                assertThat(exceptionCaught).isTrue
            }
        }
        @Test
        fun `If exception is not caught in launch, will hoist to current scope's parent`() {
            var exceptionHandled = false
            val handler = CoroutineExceptionHandler { _, _ -> exceptionHandled = true }
            val testScope = testScope(handler)
            try {
                testScope.launch {
                    throw IOException()
                }
            } catch (e: Throwable) { failInCoroutine { "It must not be caught by catch block" } }
            simulate {
                assertThat(exceptionHandled).isTrue
            }
        }
    }

    @DisplayName("Launch in coroutineScope")
    @Nested
    inner class LaunchInCoroutineScopeTest {
        @Test
        fun `coroutineScope will hoist exception to parent from child`() {
            var exceptionHandled = false
            var exceptionInLaunchHandled = false
            val handler = CoroutineExceptionHandler { _, _ -> exceptionHandled = true }
            val launchHandler = CoroutineExceptionHandler { _, _ -> exceptionInLaunchHandled = true }
            // coroutineScope case
            testScope(handler).launch {
                coroutineScope {
                    throw Exception()
                }
            }
            // launch in coroutineScope case
            testScope(launchHandler).launch {
                coroutineScope {
                    launch {
                        throw Exception()
                    }
                }
            }
            simulate {
                assertThat(exceptionHandled).isTrue
                assertThat(exceptionInLaunchHandled).isTrue
            }
        }
        @Test
        fun `If exception throw in launch block, it will not be caught by wrapped try-catch and hoist to parent scope`() {
            var exceptionHandled = false
            var launchExceptionCaught = false
            val handler = CoroutineExceptionHandler { _, _ -> exceptionHandled = true }
            val launchHandler = CoroutineExceptionHandler { _, _ -> failInCoroutine { "Exception must not be hoisted." } }
            // try-catch in coroutineScope wrapping launch block.
            testScope(handler).launch {
                coroutineScope {
                    try {
                        launch {
                            throw Exception()
                        }
                    } catch (e: Throwable) {
                        failInCoroutine { "It must not be caught by catch block" }
                    }
                }
            }
            // try-catch wrapping coroutineScope to catch before hoisting to parent scope.
            testScope(launchHandler).launch {
                try {
                    coroutineScope {
                        launch {
                            throw Exception()
                        }
                    }
                } catch(e: Throwable) {
                    launchExceptionCaught = true
                }
            }
            simulate {
                assertThat(exceptionHandled).isTrue
                assertThat(launchExceptionCaught).isTrue
            }
        }
    }

    @DisplayName("Launch in supervisorScope")
    @Nested
    inner class LaunchInSupervisorScopeTest {
        @Test
        fun `supervisorScope will hoist exception from child to parent scope`() {
            var exceptionHandled = false
            var exceptionInLaunchHandled = false
            val handler = CoroutineExceptionHandler { _, _ -> exceptionHandled = true }
            val launchHandler = CoroutineExceptionHandler { _, _ -> exceptionInLaunchHandled = true }
            testScope(handler).launch {
                supervisorScope { throw Exception() }
            }
            testScope(launchHandler).launch {
                supervisorScope {
                    launch { throw Exception() }
                }
            }
            simulate {
                assertThat(exceptionHandled).isTrue
                assertThat(exceptionInLaunchHandled).isTrue
            }
        }
        @Test
        fun `If exception throw in launch block, it will not be caught by wrapped try-catch and hoist to parent scope`() {
            var exceptionHandled = false
            var exceptionInLaunchHandled = false
            val handler = CoroutineExceptionHandler { _, _ -> exceptionHandled = true }
            val launchHandler = CoroutineExceptionHandler { _, _ -> exceptionInLaunchHandled = true }
            // try-catch in supervisorScope wrapping launch block.
            testScope(handler).launch {
                supervisorScope {
                    try {
                        launch {
                            throw Exception()
                        }
                    } catch (e: Throwable) {
                        failInCoroutine { "It must not be caught by catch block" }
                    }
                }
            }
            // try-catch wrapping supervisorScope will not hoist exception to parent.
            testScope(launchHandler).launch {
                try {
                    supervisorScope {
                        launch {
                            throw Exception()
                        }
                    }
                } catch(e: Throwable) {
                    failInCoroutine { "It must not be caught by catch block" }
                }
            }
            simulate {
                assertThat(exceptionHandled).isTrue
                assertThat(exceptionInLaunchHandled).isTrue
            }
        }
    }
}
