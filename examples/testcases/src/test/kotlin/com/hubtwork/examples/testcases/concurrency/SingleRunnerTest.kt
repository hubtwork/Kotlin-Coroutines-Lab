package com.hubtwork.examples.testcases.concurrency

import com.hubtwork.examples.testcases.CoroutineTestSuite
import com.hubtwork.examples.testcases.testScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SingleRunnerTest: CoroutineTestSuite() {

    @Test
    fun `Changeable user states must be worked on pending states`() {
        val state = UserState(BankingRepository(), scope)
        // initial balance is 100000L
        testScope(Job()).launch {
            state.transfer(1000L, expectedBalance = 100000L)
        }
        testScope(Job()).launch {
            state.transfer(5000L, expectedBalance = 99000L)
        }
        testScope(Job()).launch {
            state.transfer(4000L, expectedBalance = 94000L)
        }
        simulate {
            // Calls must be handle state-update sequentially.
            // Single runner will ensure that just one coroutine is running at a time.
        }
    }
}
