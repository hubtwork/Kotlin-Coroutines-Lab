package com.hubtwork.examples.testcases.concurrency

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Single Runner to help developing sequential task execution in Coroutines.
 *
 * All [enqueue] calls will be guaranteed on sequential single coroutine running.
 * If previous running task is not completed, [enqueue] process will be queueing on current scope.
 * Any future calls will wait for running or pending before enqueued
 * and start after all pending blocks sequentially executed.
 */
class SingleRunner {
    /**
     * [Mutex] for implementing lock to execute one coroutine at a time in single runner.
     * It'll ensure that enqueued job will be executed sequentially on enqueued order.
     */
    private val mutex = Mutex()

    /**
     * Enqueue suspend block.
     * If previous task is waiting or executing, mutex won't give priority to enqueued jobs.
     * So, it will guarantee order of enqueued tasks to launch just one at a time and sequentially.
     *
     * Commute Service for User needs to be guaranteed on sequential-execution.
     * With singleRunner, user's commute apiCall is guaranteed.
     * So, there's no needs to consider API asynchronous responses for commute service.
     * ```
     * class UserCommuteService(
     *      private val repository: CommuteRepository
     * ) {
     *      val runner = SingleRunner()
     *
     *      fun commute(user: User) {
     *          runner.enqueue { repository.apiOnCommute(user) }
     *      }
     *
     *      fun leaveOut(user: User) {
     *          runner.enqueue { repository.apiOnLeaveOut(user) }
     *      }
     * }
     * ```
     */
    suspend fun <T> enqueue(block: suspend () -> T): T {
        mutex.withLock {
            return block()
        }
    }
}
