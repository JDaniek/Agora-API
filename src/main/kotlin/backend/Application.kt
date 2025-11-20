package backend

import backend.infrastructure.inbound.http.routes.configureRouting
import io.ktor.server.application.*
import backend.infrastructure.plugins.*
// (El import de configureDatabases ya está cubierto por el .* de arriba, pero está bien si lo dejas)
import configureMonitoring

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    // 1. Koin primero
    configureDependencyInjection()

    // 2. El resto de plugins
    configureSecurity()
    configureSerialization()
    configureSockets()       // <--- ¡AÑADE ESTA LÍNEA OBLIGATORIA!
    configureMonitoring()
    configureDatabases()
    configureRouting()
    configureErrorHandling()

    configureCORS()
}