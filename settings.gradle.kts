plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "toshiba-ac-kotlin-client-root"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
include(
    "api",
    "toshiba-ac-kotlin-client",
    "toshiba-ac-cli",
)
