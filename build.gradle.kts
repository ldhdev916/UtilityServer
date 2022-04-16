import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.6.6"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.spring") version "1.6.10"
}

group = "com.ldhdev"
version = "1.0.0"
java.sourceCompatibility = JavaVersion.VERSION_11

allprojects {
    repositories {
        mavenCentral()
    }
}

dependencies {
    implementation(deps.spring.web)
    implementation(deps.jackson)
    implementation(deps.kotlin.scripting)

    testImplementation(deps.bundles.tests) {
        exclude(module = "mockito-core")
    }
}

tasks {

    test {
        useJUnitPlatform()
    }

    withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "11"
        }
    }

    bootJar {
        requiresUnpack("**/kotlin-compiler-embeddable-*.jar")
    }
}
