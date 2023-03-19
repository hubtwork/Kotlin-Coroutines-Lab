package com.hubtwork.examples.testcases.concurrency

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat

data class UserAccount(
    val id: Int,
    val name: String,
    val balance: Long,
)

private val mockUserAccountTable = UserAccount(1, "alen", 100000L)

class BankingRepository {
    private var table = mockUserAccountTable

    suspend fun callTransferAPI(amount: Long) {
        delay((5000L .. 100000L).random())
        val snapshot = table.balance
        table = table.copy(balance = snapshot - amount)
    }
    suspend fun callBalanceAPI(): Long {
        delay(200L)
        return table.balance
    }
}

class UserState(
    private val repository: BankingRepository,
    private val scope: CoroutineScope,
) {
    private val singleRunner = SingleRunner()

    fun transfer(amount: Long, expectedBalance: Long) {
        singleLaunch {
            assertThat(repository.callBalanceAPI()).isEqualTo(expectedBalance)
            repository.callTransferAPI(amount)
        }
    }

    private fun singleLaunch(
        block: suspend () -> Unit = { }
    ) {
        scope.launch {
            singleRunner.enqueue {
                block()
            }
        }
    }
}
