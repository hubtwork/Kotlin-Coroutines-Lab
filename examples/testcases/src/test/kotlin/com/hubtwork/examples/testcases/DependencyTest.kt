package com.hubtwork.examples.testcases

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class DependencyTest {
    @Test
    fun `Test for junit && assertJ usage`() {
        val a = ""
        assertThat(a).isEmpty()
    }
    @ParameterizedTest
    @ValueSource(longs = [1, 3, 5, 7, 9])
    fun `Test for parameterized`(n: Long) {
        val processed = n * 2
        assertThat(processed).isNotNegative
        assertThat(processed).isGreaterThan(n)
    }
}
