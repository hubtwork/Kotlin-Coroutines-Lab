package com.hubtwork.examples.testcases.exceptionhandler

import com.hubtwork.examples.testcases.CoroutineTestSuite
import com.hubtwork.examples.testcases.testScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import java.io.IOException

class ExceptionInLaunchTest: CoroutineTestSuite() {

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
        var exceptionHandled = false
        val handler = CoroutineExceptionHandler { _, _ -> exceptionHandled = true }
        testScope(handler).launch {
            try {
                throw IOException()
            } catch(e: Throwable) {
                exceptionCaught = true
            }
        }
        simulate {
            assertThat(exceptionCaught).isTrue
            assertThat(exceptionHandled).isFalse
        }
    }
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
        val handler = CoroutineExceptionHandler { _, e -> exceptionHandled = true }
        testScope(handler).launch {
            coroutineScope {
                try {
                    launch {
                        throw Exception()
                    }
                } catch (e: Throwable) {
                    fail { "It must not be reached." }
                }
            }
        }
        simulate {
            assertThat(exceptionHandled).isTrue
        }
    }
}
