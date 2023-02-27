package com.hubtwork.examples.testcases.exceptionhandler

import com.hubtwork.examples.testcases.CoroutineTestSuite
import kotlinx.coroutines.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail

class BaseExceptionHandlerTest: CoroutineTestSuite() {

    @Nested
    inner class WithBaseScope {
        @Test
        fun `Exception in child will cancel parent by propagate CancellationException`() {
            var handledException: Throwable? = null
            val handler = CoroutineExceptionHandler { _, throwable -> handledException = throwable }
            scope.launch(handler) {
                launch(CoroutineName("child 1")) {
                    delay(1000L)
                    fail { "It must be canceled by child2's interrupt" }
                }
                launch(CoroutineName("child 2")) {
                    delay(10L)
                    throw ArithmeticException()
                }
            }
            simulate {
                assertThat(handledException).isInstanceOf(ArithmeticException::class.java)
            }
        }
        @Test
        fun `ExceptionHandler will be executed after all child cancelled`() {
            var handledException: Throwable? = null
            val handler = CoroutineExceptionHandler { _, _ -> handledException = null }
            scope.launch(handler) {
                launch(CoroutineName("child 1")) {
                    try {
                        delay(1000L)
                        fail { "It must be canceled by child2's interrupt" }
                    } finally {
                        handledException = ArithmeticException()
                    }
                }
                launch(CoroutineName("child 2")) {
                    delay(10L)
                    throw ArithmeticException()
                }
            }
            simulate {
                assertThat(handledException).isNull()
            }
        }
        @Test
        fun `If multiple exceptions occurred, rest will be attached to first`() {
            var handledException: Throwable? = null
            val handler = CoroutineExceptionHandler { _, throwable -> handledException = throwable }
            scope.launch(handler) {
                launch(CoroutineName("child 1")) {
                    try {
                        delay(1000L)
                        fail { "It must be canceled by child2's interrupt" }
                    } finally {
                        throw IllegalArgumentException()
                    }
                }
                launch(CoroutineName("child 2")) {
                    delay(10L)
                    throw ArithmeticException()
                }
                launch(CoroutineName("child 3")) {
                    try {
                        delay(3000L)
                    } catch(e: CancellationException) {
                        throw IllegalStateException()
                    }

                }
            }
            simulate {
                assertThat(handledException).isInstanceOf(ArithmeticException::class.java)
                assertThat(handledException?.suppressed?.size).isEqualTo(2)
            }
        }
    }


}
