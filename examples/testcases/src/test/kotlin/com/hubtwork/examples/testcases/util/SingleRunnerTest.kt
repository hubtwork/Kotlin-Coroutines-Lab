package com.hubtwork.examples.testcases.util

import com.hubtwork.examples.testcases.CoroutineTestSuite
import com.hubtwork.examples.testcases.SingleRunner
import kotlinx.coroutines.launch
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SingleRunnerTest: CoroutineTestSuite() {

    @Test
    fun `Changeable user states must be worked on pending states`() {
        val state = UserState()
        val runner = SingleRunner()
        scope.launch {
            runner.enqueue { state.attend() }
            runner.enqueue { state.acceptOrder() }
            runner.enqueue { state.acceptOrder() }
            runner.enqueue { state.acceptOrder() }
            runner.enqueue { state.finishOrder() }
            runner.enqueue { state.leave() }
        }
        simulate {
            // User must be handle state-update sequentially, so accepted order is not 3, just one.
            // Single runner will ensure that just one coroutine is running at a time.
            assertThat(state.acceptedCount).isEqualTo(1)
            assertThat(state.finishedCount).isEqualTo(1)
        }
    }
}

class UserState {
    private var state: CommuteState = CommuteState.NotCommuted
    private var _acceptedCount: Int = 0
    private var _finishedCount: Int = 0
    val acceptedCount: Int get() = _acceptedCount
    val finishedCount: Int get() = _finishedCount

    fun attend() {
        check(state == CommuteState.NotCommuted) { "User already commuted !" }
        state = CommuteState.Waiting
    }
    fun acceptOrder() {
        try {
            check(state == CommuteState.Waiting) { "User is not available to accept order !" }
        } catch(e: IllegalStateException) {
            return
        }
        state = CommuteState.Working
        _acceptedCount ++
    }
    fun finishOrder() {
        try {
            check(state == CommuteState.Working) { "User is not on work !" }
        } catch(e: IllegalStateException) {
            return
        }
        state = CommuteState.Waiting
        _finishedCount ++
    }
    fun leave() {
        check(state != CommuteState.Working) { "User is on work, so can't leave out !" }
        state = CommuteState.NotCommuted
    }
}
enum class CommuteState {
    Waiting,
    Working,
    NotCommuted,
}
