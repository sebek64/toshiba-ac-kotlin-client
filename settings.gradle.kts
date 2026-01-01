plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "toshiba-ac-kotlin-client"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
include(
    "cli",
    "lib:api",
    "lib:root",
)
