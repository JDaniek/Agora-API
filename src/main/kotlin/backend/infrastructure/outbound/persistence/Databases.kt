// Asumo que tu package es este, basado en tu import
package backend.infrastructure.plugins

import io.ktor.server.application.*
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
// --- NUEVOS IMPORTS (¡AÑADE ESTOS!) ---
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

// --- ESTA ES TU FUNCIÓN ORIGINAL (¡Está perfecta!) ---
fun Application.configureDatabases() {
    // Lee la configuración de application.yaml
    val dbUrl = environment.config.property("db.url").getString()
    val dbUser = environment.config.property("db.user").getString()
    val dbPassword = environment.config.property("db.password").getString()

    // 1. Configura Flyway para migraciones
    val flyway = Flyway.configure()
        .dataSource(dbUrl, dbUser, dbPassword)
        .load()

    // 2. Ejecuta las migraciones
    try {
        flyway.migrate()
    } catch (e: Exception) {
        log.error("Error en la migración de Flyway", e)
        throw e
    }

    // 3. Conecta Exposed a la misma base de datos
    val database = Database.connect(dbUrl, user = dbUser, password = dbPassword)

    // ... (Tu código de SchemaUtils comentado) ...
}


// --- ¡AÑADE ESTA FUNCIÓN AQUÍ ABAJO! ---
// Esta es la función 'helper' que el repositorio necesita para
// ejecutar consultas de forma asíncrona.

/**
 * Función helper para ejecutar una transacción de base de datos
 * de forma suspendida (asíncrona) en el pool de hilos de IO.
 */
suspend fun <T> dbQuery(block: suspend () -> T): T =
    newSuspendedTransaction(Dispatchers.IO) {
        block()
    }