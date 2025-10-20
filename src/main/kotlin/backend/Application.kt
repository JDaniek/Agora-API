package backend

import backend.infrastructure.outbound.persistence.Databases
import backend.infrastructure.plugins.configureSecurity
import backend.infrastructure.plugins.configureSerialization
import backend.infrastructure.plugins.configureErrorHandling
import backend.infrastructure.inbound.http.routes.configureRouting
import io.ktor.server.application.*
import io.ktor.server.netty.*

fun main(args: Array<String>) = EngineMain.main(args)

@Suppress("unused")
fun Application.module() {
    // 1) DB + migraciones
    Databases.init(environment)

    // 2) Plugins Ktor
    configureSerialization()
    configureSecurity()
    configureErrorHandling()

    // 3) Rutas
    configureRouting()
}
