plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "toshiba-ac-kotlin-client-root"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
include(
    "toshiba-ac-kotlin-client",
)
