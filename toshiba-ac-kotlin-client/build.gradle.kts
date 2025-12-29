plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktlint)
    `java-library`
}

dependencies {
    implementation(projects.api)
    implementation(libs.microsoft.iot.client)
    implementation(libs.kotlinx.coroutines.core)
}
