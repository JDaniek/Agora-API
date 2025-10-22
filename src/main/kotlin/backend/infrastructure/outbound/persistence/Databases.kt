package backend.plugins

import backend.infrastructure.outbound.persistence.tables.*
import io.ktor.server.application.*
import org.agora.backend.infrastructure.outbound.persistance.tables.ChatMembersTable
import org.agora.backend.infrastructure.outbound.persistance.tables.ChatMessagesTable
import org.agora.backend.infrastructure.outbound.persistance.tables.ChatsTable
import org.agora.backend.infrastructure.outbound.persistance.tables.ClassEnrollmentsTable
import org.agora.backend.infrastructure.outbound.persistance.tables.ClassesTable
import org.agora.backend.infrastructure.outbound.persistance.tables.MediaTable
import org.agora.backend.infrastructure.outbound.persistance.tables.NotificationTypesTable
import org.agora.backend.infrastructure.outbound.persistance.tables.NotificationsTable
import org.agora.backend.infrastructure.outbound.persistance.tables.ProfilesTable
import org.agora.backend.infrastructure.outbound.persistance.tables.RolesTable
import org.agora.backend.infrastructure.outbound.persistance.tables.SpecialtiesTable
import org.agora.backend.infrastructure.outbound.persistance.tables.UserSpecialtiesTable
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

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