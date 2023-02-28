package com.hubtwork.examples.testcases.execution

import com.hubtwork.examples.testcases.CoroutineTestSuite
import com.hubtwork.examples.testcases.testScope
import kotlinx.coroutines.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class LaunchTest: CoroutineTestSuite() {
    @Test
    fun `Launch will execute on asynchronous`() {
        var isProcessed = false
        scope.launch {
            isProcessed = true
        }
        // ordinary block
        assertThat(isProcessed).isFalse
        // after scope's virtual time processed.
        simulate {
            assertThat(isProcessed).isTrue
        }
    }
    @Test
    fun `Order of launch operation is not guaranteed`() {
        var task1Count = 0
        var isTask2Processed = false
        scope.launch {
            task1Count ++
            delay(100)
            task1Count ++
        }
        scope.launch {
            delay(50)
            isTask2Processed = true
        }
        simulate(50) {
            assertThat(task1Count).isEqualTo(1)
            assertThat(isTask2Processed).isEqualTo(false)
        }
        simulate(50) {
            assertThat(task1Count).isEqualTo(1)
            assertThat(isTask2Processed).isEqualTo(true)
        }
        simulate {
            assertThat(task1Count).isEqualTo(2)
            assertThat(isTask2Processed).isEqualTo(true)
        }
    }
    @Test
    fun `Nested launch operation order is not guaranteed`() {
        var lastExecutionNo: String? = null
        testScope(CoroutineName("lv0")).launch {
            launch(CoroutineName("lv1")) {
                lastExecutionNo = coroutineContext[CoroutineName.Key]?.name
                launch(CoroutineName("lv2")) {
                    lastExecutionNo = coroutineContext[CoroutineName.Key]?.name
                }
            }
            lastExecutionNo = coroutineContext[CoroutineName.Key]?.name
        }
        simulate {
            // launch block will be add to scope inorder by scope level in nested level.
            assertThat(lastExecutionNo).isEqualTo("lv2")
        }
    }
    @Test
    fun `Nested launch operation order can be handled by yield`() {
        var lastExecutionNo: String? = null
        testScope(CoroutineName("lv0")).launch {
            launch(CoroutineName("lv1")) {
                lastExecutionNo = coroutineContext[CoroutineName.Key]?.name
            }
            yield()
            lastExecutionNo = coroutineContext[CoroutineName.Key]?.name
        }
        simulate {
            // launch block will be add to scope inorder by scope level in nested level.
            assertThat(lastExecutionNo).isEqualTo("lv0")
        }
    }
}
