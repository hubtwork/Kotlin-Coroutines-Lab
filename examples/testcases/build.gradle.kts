@file:Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id("java-library")
    alias(libs.plugins.kotlin.jvm)
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    implementation(libs.kotlin.coroutine.core)
    // test
    testImplementation(libs.kotlin.coroutine.test)
    testRuntimeOnly(libs.junit.engine.jupiter)
    testImplementation(libs.junit.api)
    testImplementation(libs.junit.params)
    testImplementation(libs.assertj)
}
