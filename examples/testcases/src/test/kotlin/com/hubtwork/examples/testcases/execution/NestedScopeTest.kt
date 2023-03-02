@file:OptIn(ExperimentalStdlibApi::class)

package com.hubtwork.examples.testcases.execution

import com.hubtwork.examples.testcases.CoroutineTestSuite
import com.hubtwork.examples.testcases.testScope
import kotlinx.coroutines.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class NestedScopeTest: CoroutineTestSuite() {
    /**
     * CoroutineScope's JobContext can cancel children, but it's not adapted for dispatcher
     */
    @Test
    fun `cancelChild() not properly cancel all child when work with Dispatcher`() {
        var exceptionThrown = false
        var exceptionHandled = false
        var taskProcessed = false

        val handler = CoroutineExceptionHandler { _, _ -> exceptionHandled = true }
        val scope1 = testScope(SupervisorJob() + handler)
        val scope2 = testScope(scope1.coroutineContext + SupervisorJob(scope1.coroutineContext[Job.Key]))

        // compare CoroutineContext
        assertThat(scope2.coroutineContext[Job.Key]).isIn(scope1.coroutineContext[Job.Key]?.children?.toList())
        assertThat(scope2.coroutineContext[CoroutineDispatcher.Key]).isEqualTo(scope1.coroutineContext[CoroutineDispatcher.Key])

        scope2.launch {
            delay(1000)
            exceptionThrown = true
            throw Exception("test")
        }
        scope2.launch {
            delay(2000)
            taskProcessed = true
        }
        testScope(SupervisorJob()).launch {
            scope1.coroutineContext[CoroutineDispatcher.Key]?.cancelChildren()
        }
        // all task must not be canceled because dispatcher cancellation will not have impact on other.
        simulate {
            assertThat(exceptionThrown).isTrue
            assertThat(exceptionHandled).isTrue
            assertThat(taskProcessed).isTrue
        }
    }

    @Test
    fun `Test for nested scope-launch && context inheritance - interrupt`() {
        var exceptionThrown = false
        var exceptionHandled = false
        var child2Processed = false
        var child3Processed = false

        val handler = CoroutineExceptionHandler { _, _ -> exceptionHandled = true }
        val scope1 = testScope(SupervisorJob() + handler)
        val scope2 = testScope(scope1.coroutineContext + SupervisorJob(scope1.coroutineContext[Job.Key]))
        // CoroutineScope Inheritance check
        // Job of scope2 is supervisor job which is reated as the child of scope1's supervisor job.
        assertThat(scope1.coroutineContext[Job.Key]).isNotEqualTo(scope2.coroutineContext[Job.Key])
        // Dispatcher is inherited
        assertThat(scope1.coroutineContext[CoroutineDispatcher.Key]).isEqualTo(scope2.coroutineContext[CoroutineDispatcher.Key])

        scope2.launch {
            delay(1000)
            exceptionThrown = true
            throw Exception("test")
        }
        scope2.launch {
            delay(2000)
            println("cancel?")
            child2Processed = true
        }

        testScope(CoroutineName("another part")).launch {
            delay(500)
            // because scope1's Job ( SupervisorJob() ) will cancel all child jobs, scope2 will be canceled also.
            scope1.coroutineContext[Job.Key]?.cancelChildren()

            scope.launch {
                delay(1000)
                child3Processed = true
            }
        }

        simulate {
            assertThat(exceptionThrown).isFalse
            assertThat(exceptionHandled).isFalse
            assertThat(child2Processed).isFalse
            assertThat(
                arrayOf(exceptionThrown, exceptionHandled, child2Processed)
            ).filteredOn { it != false }.isEmpty()
            assertThat(child3Processed).isTrue
        }
    }
}
