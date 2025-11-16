package backend.infrastructure.plugins

import io.ktor.server.application.*
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

fun Application.configureDatabases() {
    // Lee la configuración de application.yaml
    val dbUrl = environment.config.property("db.url").getString()
    val dbUser = environment.config.property("db.user").getString()
    val dbPassword = environment.config.property("db.password").getString()

    // 1. Configura Flyway para migraciones
    val flyway = Flyway.configure()
        .dataSource(dbUrl, dbUser, dbPassword)
        .baselineOnMigrate(true) // <-- ¡¡AÑADE ESTA LÍNEA!!
        .load()

    // 2. Ejecuta las migraciones
    try {
        flyway.migrate() // Ahora 'migrate()' funcionará
    } catch (e: Exception) {
        log.error("Error en la migración de Flyway", e)
        throw e
    }

    // 3. Conecta Exposed a la misma base de datos
    val database = Database.connect(dbUrl, user = dbUser, password = dbPassword)

    // ... (Tu código de SchemaUtils comentado) ...
}


// (Tu función dbQuery está perfecta, la dejamos como está)
suspend fun <T> dbQuery(block: suspend () -> T): T =
    newSuspendedTransaction(Dispatchers.IO) {
        block()
    }