plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktlint)
    application
}

dependencies {
    implementation(projects.lib.root)
    implementation(libs.clikt)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.logback.classic)
}

application {
    mainClass = "toshibaac.cli.MainKt"
}
