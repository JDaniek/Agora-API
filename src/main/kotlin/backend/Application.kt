package backend

// Añade estos imports:
import backend.infrastructure.inbound.http.routes.configureRouting
import backend.plugins.*
import io.ktor.server.application.*

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
}