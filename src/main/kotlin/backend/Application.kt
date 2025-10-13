package org.agora.backend

import io.ktor.server.application.*
import io.ktor.server.netty.EngineMain
import org.agora.backend.infrastructure.out.persistance.configureDatabases
import org.agora.backend.infrastructure.plugins.configureFrameworks
import org.agora.backend.infrastructure.plugins.configureMonitoring
import org.agora.backend.infrastructure.`in`.http.routes.configureRouting
import org.agora.backend.infrastructure.plugins.configureSecurity
import org.agora.backend.infrastructure.plugins.configureSerialization

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    configureFrameworks()
    configureMonitoring()
    configureSerialization()
    configureDatabases()
    configureSecurity()
    configureRouting()
}
