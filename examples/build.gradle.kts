// Top-level build file where you can add configuration options common to all sub-projects/modules.
@file:Suppress("DSL_SCOPE_VIOLATION")
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
}

allprojects {
    afterEvaluate {
        tasks.withType(Test::class) {
            useJUnitPlatform()
            testLogging {
                events.addAll(arrayOf(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED))
            }
        }
    }
}
