plugins {
    id("org.jetbrains.kotlin.jvm") version "2.0.21"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.21"
    id("io.ktor.plugin") version "3.0.2"
}


kotlin {
    jvmToolchain(21)
    sourceSets.all {
        languageSettings.languageVersion = "2.0"
        languageSettings.apiVersion = "2.0"
    }
    application {
        mainClass.set("backend.ApplicationKt")
    }
}

dependencies {
    // Cloudinary (Ahora sí lo encontrará)
// Cloudinary (Ahora sí lo encontrará)
    implementation(libs.cloudinary.http44)
    // Dependencia para CORS
    implementation(libs.ktor.server.cors)

    // Koin
    implementation(libs.koin.ktor)
    implementation(libs.koin.logger.slf4j)

    // Ktor
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.server.config.yaml)
    implementation(libs.ktor.serialization.kotlinx.json)

    // Serialization
    implementation(libs.kotlinx.serialization.json)

    // Exposed
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.java.time)

    // Security
    implementation(libs.bcrypt)
    implementation(libs.java.jwt)

    // Database
    implementation(libs.postgresql)
    implementation(libs.flyway.core)
    implementation(libs.flyway.postgresql)

    // Utils
    implementation(libs.kotlinx.datetime)

    // Logging
    runtimeOnly(libs.logback.classic)
}

ktor {
    fatJar {
        archiveFileName.set("agora-api-all.jar")
    }
}
