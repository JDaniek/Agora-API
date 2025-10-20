package backend.infrastructure.outbound.persistence

import io.ktor.server.application.*
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database

object Databases {
    fun init(environment: ApplicationEnvironment) {
        val cfg = environment.config
        val url = cfg.property("db.url").getString()
        val user = cfg.property("db.user").getString()
        val password = cfg.property("db.password").getString()

        // Conexión a la base de datos
        Database.connect(url, "org.postgresql.Driver", user, password)

        // Configuración de Flyway con baseline activado
        Flyway.configure()
            .dataSource(url, user, password)
            .locations("classpath:db/migration")
            .baselineOnMigrate(true) // 👈 Esta línea evita el error
            .load()
            .migrate()
    }
}
