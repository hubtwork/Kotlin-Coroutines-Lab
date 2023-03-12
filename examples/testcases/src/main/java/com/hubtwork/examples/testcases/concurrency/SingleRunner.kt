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
     * PhoneInfo for User needs userData for Data Consistency.
     * With singleRunner, user must be saved before phone data.
     * So, there's no needs to check user before saving phone.
     * ```
     * class SaveService(
     *      private val repository: UserRepository
     * ) {
     *      val runner = SingleRunner()
     *
     *      fun saveUser(user: User) {
     *          runner.enqueue { repository.saveUser(user) }
     *      }
     *
     *      fun savePhone(userId: Int, phone: PhoneInfo) {
     *          runner.enqueue { repository.savePhone(userId, phone) }
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
