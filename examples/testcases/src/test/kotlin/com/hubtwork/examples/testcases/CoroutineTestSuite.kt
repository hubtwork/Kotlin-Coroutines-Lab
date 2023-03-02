@file:OptIn(ExperimentalCoroutinesApi::class)

package com.hubtwork.examples.testcases

import kotlinx.coroutines.CoroutineDispatcher
import org.junit.jupiter.api.fail
import kotlinx.coroutines.CoroutineExceptionHandler
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
    private val failuresInCoroutineContext: MutableList<String> = mutableListOf()
    @BeforeEach
    fun setUp() {
        context = dispatcher
        failuresInCoroutineContext.clear()
    }
    /**
     * Simulate virtual time for test and check result-assertions.
     */
    fun simulate(
        timeMillis: Long? = null,
        assertBlock: () -> Unit = { }
    ) {
        timeMillis
            ?.let { dispatcher.scheduler.advanceTimeBy(it) }
            ?: run { dispatcher.scheduler.advanceUntilIdle() }
        // check if test-fail-assertion in coroutine.
        if (failuresInCoroutineContext.isNotEmpty()) {
            fail { "Exception occurred in CoroutineScope = $failuresInCoroutineContext " }
        }
        assertBlock()
    }

    /**
     * Test failure control management for [CoroutineScope].
     * - If using Junit5's [fail] API in [CoroutineScope] will not be treat as test failure.
     * - It seems to [AssertionError] in context is consumed by [CoroutineExceptionHandler].
     *
     * So if you want check failure in coroutineScope, use it for using test-failure definitely.
     *
     * [simulate] will internally check assertion that failure count is zero.
     */
    fun failInCoroutine(message: () -> String = { String() }) =
        failuresInCoroutineContext.add(message())

    /**
     * Get current scheduler's elapsed time millis
     */
    val currentTime: Long get() = dispatcher.scheduler.currentTime
}

fun CoroutineTestSuite.testScope(additionalContext: CoroutineContext): CoroutineScope {
    require(additionalContext !is CoroutineDispatcher) { "Dispatcher overriding in root test scope is blocked." }
    return CoroutineScope(context + additionalContext)
}
