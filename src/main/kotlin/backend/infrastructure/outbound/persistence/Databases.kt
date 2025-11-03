import backend.infrastructure.plugins.*   // ✅

import io.ktor.server.application.*
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database

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

    // 4. (Opcional, solo para desarrollo)
    // Crea las tablas si no existen. Flyway debería manejar esto.
    // Comenta esto en producción.

//    transaction(database) {
//        SchemaUtils.createMissingTablesAndColumns(
//            RolesTable,
//            UserAccountsTable,
//            ProfilesTable,
//            SpecialtiesTable,
//            UserSpecialtiesTable,
//            ClassesTable,
//            ClassEnrollmentsTable,
//            ReviewsTable,
//            ChatsTable,
//            ChatMembersTable,
//            ChatMessagesTable,
//            MediaTable,
//            NotificationTypesTable,
//            NotificationsTable
//        )
//    }
}