import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.6.6"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    val kotlinVersion = "1.6.10"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
    kotlin("plugin.jpa") version kotlinVersion
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
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-security")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.webjars:stomp-websocket:2.3.4")

    implementation("mysql:mysql-connector-java")

    implementation("org.jetbrains.kotlin:kotlin-scripting-jsr223")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")

    val fuel = "2.3.1"
    implementation("com.github.kittinunf.fuel:fuel:$fuel")
    implementation("com.github.kittinunf.fuel:fuel-kotlinx-serialization:$fuel")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(module = "mockito-core")
    }
    testImplementation("com.ninja-squad:springmockk:3.1.1")
    testImplementation(kotlin("test"))
    testImplementation("io.mockk:mockk:1.12.3")
}


tasks {

    test {
        useJUnitPlatform()
    }

    withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict", "-opt-in=kotlin.RequiresOptIn")
            jvmTarget = "11"
        }
    }

    bootJar {
        requiresUnpack("**/kotlin-compiler-embeddable-*.jar")
    }

    jar {
        enabled = false
    }
}
