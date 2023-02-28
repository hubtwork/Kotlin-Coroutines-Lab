package com.hubtwork.examples.testcases.execution

import com.hubtwork.examples.testcases.CoroutineTestSuite
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ScopeTest: CoroutineTestSuite() {
    @Test
    fun `coroutineScope will get priority of execution order`() {
        var executionNo: Int? = null
        scope.launch {
            coroutineScope {
                launch {
                    executionNo = 1
                }
            }
            executionNo = 2
        }
        simulate { assertThat(executionNo).isEqualTo(2) }
    }
    @Test
    fun `Although coroutineScope launch with delay, it has priority than other blocks in same launch context`() {
        var executionNo: Int? = null
        scope.launch {
            executionNo = 3
            coroutineScope {
                launch {
                    // Although block has yield(), coroutineScope has priority
                    delay(1000)
                    executionNo = 1
                }
            }
            executionNo = 2
        }
        simulate { assertThat(executionNo).isEqualTo(2) }
    }
}
