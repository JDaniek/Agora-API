plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.plugin.serialization)
}

group = "org.agora"
version = "0.0.1"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

dependencies {
    implementation(libs.flyway.core)
    implementation(libs.flyway.database.postgresql) // ← NECESARIO con Flyway 10+
    // DI (opcional)
    implementation(libs.koin.ktor)
    implementation(libs.koin.logger.slf4j)


    implementation(libs.ktor.server.status.pages)
    implementation(libs.exposed.java.time)
    // Ktor
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.config.yaml)

    // DB
    implementation(libs.postgresql)
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.flyway.core)

    // Crypto (passwords)
    implementation(libs.bcrypt)

    // (Opcional) H2 solo para tests locales
    implementation(libs.h2)

    // Logging
    implementation(libs.logback.classic)

    // Tests
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)
}
