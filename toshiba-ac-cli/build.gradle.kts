plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktlint)
    application
}

dependencies {
    implementation(projects.toshibaAcKotlinClient)
    implementation(libs.logback.classic)
}

application {
    mainClass = "toshibaac.cli.MainKt"
}
