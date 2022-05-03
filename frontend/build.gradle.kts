@file:OptIn(org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalDistributionDsl::class)

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("js")
    id("org.jetbrains.compose") version "1.1.0"
}

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    implementation(compose.web.core)
    implementation(compose.runtime)
}

kotlin {
    js(IR) {
        browser {
            distribution {
                directory = File("${projectDir.parent}/src/main/resources/static/")
            }
        }
        binaries.executable()
    }
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-opt-in=kotlin.RequiresOptIn")
        }
    }
}