package backend

// Añade estos imports:
import backend.infrastructure.inbound.http.routes.configureRouting
import io.ktor.server.application.*
import backend.infrastructure.plugins.*
import configureDatabases
import configureMonitoring

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    // 1. Koin primero
    configureDependencyInjection()

    // 2. El resto de plugins
    configureSecurity()
    configureSerialization() // <-- Ahora se resuelve
    configureMonitoring()    // <-- Ahora se resuelve
    configureDatabases()     // <-- Ahora se resuelve
    configureRouting()       // <-- Ahora se resuelve
    configureErrorHandling()

    configureCORS() // <-- Aquí instalamos y configuramos CORS

}