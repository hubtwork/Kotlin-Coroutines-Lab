@file:OptIn(ExperimentalCoroutinesApi::class)

package com.hubtwork.examples.testcases

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import kotlin.coroutines.CoroutineContext

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class CoroutineTestSuite {
    private val dispatcher: TestDispatcher = StandardTestDispatcher()
    lateinit var context: CoroutineContext
    protected val scope: CoroutineScope get() = CoroutineScope(context)

    @BeforeEach
    fun setUp() {
        context = dispatcher
    }
    /**
     * Simulate virtual time for test.
     */
    fun simulate(
        timeMillis: Long? = null,
        assertBlock: () -> Unit = { }
    ) {
        timeMillis?.let { dispatcher.scheduler.advanceTimeBy(it) }
            ?: run { dispatcher.scheduler.advanceUntilIdle() }
        assertBlock()
    }
    /**
     * Get current scheduler's elapsed time millis
     */
    val currentTime: Long get() = dispatcher.scheduler.currentTime
}

fun CoroutineTestSuite.testScope(additionalContext: CoroutineContext): CoroutineScope {
    require(additionalContext !is CoroutineDispatcher) { "Dispatcher overriding in root test scope is blocked." }
    return CoroutineScope(context + additionalContext)
}
