import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.ktlint)
    alias(libs.plugins.kotlin.jvm) apply false
}

private val jvmTargetValue = JvmTarget.JVM_21

allprojects {
    group = "toshibaac.client"
    version = findProperty("release.version") as? String ?: "0.0.0-development"

    repositories {
        mavenCentral()
    }

    plugins.withType<JavaPlugin> {
        configure<JavaPluginExtension> {
            toolchain {
                languageVersion = JavaLanguageVersion.of(21)
            }
        }
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        sourceCompatibility = jvmTargetValue.target
        targetCompatibility = jvmTargetValue.target
    }

    tasks.withType<KotlinCompile> {
        compilerOptions {
            jvmTarget = jvmTargetValue
            freeCompilerArgs.add("-opt-in=kotlin.RequiresOptIn")
            allWarningsAsErrors = true
        }
        dependencies {
            val implementation = configurations.getByName("implementation")

            implementation(libs.kotlin.logging)
            implementation(libs.slf4j.api)
        }
    }

    plugins.withType<KotlinPluginWrapper> {
        configure<KotlinJvmProjectExtension> {
            explicitApi()
        }
    }

    tasks.withType<Jar> {
        archiveBaseName = project.path
            .split(":")
            .drop(1)
            .dropLast(1)
            .let { components ->
                val lastComponent = if (project.name == "root") {
                    emptyList()
                } else {
                    listOf(project.name)
                }
                (listOf(rootProject.name) + components + lastComponent).joinToString("-")
            }
    }
}
