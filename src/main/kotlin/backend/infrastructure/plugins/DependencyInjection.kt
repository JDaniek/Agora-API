// DependencyInjection.kt
package backend.infrastructure.plugins

// --- Imports de Cloudinary ---
import backend.domain.port.outbound.StorageService
import backend.infrastructure.outbound.storage.CloudinaryStorageService
import backend.infrastructure.inbound.http.handler.MediaHandler
import com.cloudinary.Cloudinary

// --- Imports de Casos de Uso ---
import backend.application.usecase.users.GetProfileQueryImpl
import backend.application.usecase.users.LoginUseCaseImpl
import backend.application.usecase.users.RegisterUserUseCaseImpl
import backend.application.usecase.users.UpdateProfileUseCaseImpl

// --- Imports de Dominio ---
import backend.domain.port.inbound.GetProfileQuery
import backend.domain.port.inbound.LoginUseCase
import backend.domain.port.inbound.RegisterUserUseCase
import backend.domain.port.inbound.UpdateProfileUseCase
import backend.domain.port.outbound.PasswordService
import backend.domain.port.outbound.ProfileRepository
import backend.domain.port.outbound.SpecialtyRepository
import backend.domain.port.outbound.UserRepository

// --- Imports de Infraestructura ---
import backend.infrastructure.inbound.http.handler.AuthHandler
import backend.infrastructure.inbound.http.handler.ProfileHandler
import backend.infrastructure.outbound.persistence.repository.ProfileRepositoryPg
import backend.infrastructure.outbound.persistence.repository.SpecialtyRepositoryPg
import backend.infrastructure.outbound.persistence.repository.UserRepositoryPg
import backend.infrastructure.security.JwtConfig
import backend.infrastructure.security.JwtService
import backend.infrastructure.security.PasswordServiceImpl

// --- Imports de Ktor y Koin ---
import io.ktor.server.application.*
import io.ktor.server.config.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

// Helpers de lectura
private fun ApplicationConfig.prop(path: String) =
    propertyOrNull(path)?.getString()
private fun sys(name: String) =
    System.getProperty(name) ?: System.getenv(name)

// --- Módulos de Koin ---

val configModule = module {
    // expone la config de Ktor a Koin
    single<ApplicationConfig> { get<Application>().environment.config }

    // JWT (Configuración robusta)
    single {
        val cfg = get<ApplicationConfig>()
        val secret = cfg.prop("security.jwt.secret") ?: sys("JWT_SECRET")
        ?: error("Falta security.jwt.secret o env/system JWT_SECRET")
        val issuer = cfg.prop("security.jwt.issuer") ?: "agora.auth"
        val audience = cfg.prop("security.jwt.audience") ?: "agora.clients"
        val realm = cfg.prop("security.jwt.realm") ?: "Access to Agora API"
        val expMinutes = (cfg.prop("security.jwt.expiresInMinutes") ?: "60").toLong()
        JwtConfig(secret, issuer, audience, realm, expMinutes)
    }

    // Cloudinary (Lógica de URL o 3 claves DENTRO del 'single')
    single<Cloudinary> {
        val cfg = get<ApplicationConfig>()
        val cloudinaryUrl = sys("CLOUDINARY_URL") ?: cfg.prop("cloudinary.url")

        if (cloudinaryUrl != null) {
            Cloudinary(cloudinaryUrl) // Usar URL si existe
        } else {
            // Usar claves sueltas del application.yaml
            val name = cfg.prop("cloudinary.cloud_name")
                ?: sys("CLOUDINARY_CLOUD_NAME")
                ?: error("Falta cloudinary.cloud_name o env CLOUDINARY_CLOUD_NAME")
            val key = cfg.prop("cloudinary.api_key")
                ?: sys("CLOUDINARY_API_KEY")
                ?: error("Falta cloudinary.api_key o env CLOUDINARY_API_KEY")
            val secret = cfg.prop("cloudinary.api_secret")
                ?: sys("CLOUDINARY_API_SECRET")
                ?: error("Falta cloudinary.api_secret o env CLOUDINARY_API_SECRET")

            Cloudinary(mapOf(
                "cloud_name" to name, "api_key" to key, "api_secret" to secret, "secure" to true
            ))
        }
    }
}

val infrastructureModule = module {

    single<UserRepository> { UserRepositoryPg() }
    single<ProfileRepository> { ProfileRepositoryPg() }
    single<SpecialtyRepository> { SpecialtyRepositoryPg() }
    single<PasswordService> { PasswordServiceImpl() }
    single { JwtService(get()) }
    single<StorageService> { CloudinaryStorageService(get()) }
}

val applicationModule = module {
    single<RegisterUserUseCase> { RegisterUserUseCaseImpl(get(), get()) }
    single<LoginUseCase> { LoginUseCaseImpl(get(), get(), get()) }
    single<UpdateProfileUseCase> { UpdateProfileUseCaseImpl(get()) }
    single<GetProfileQuery> { GetProfileQueryImpl(get(), get()) }
}

val inboundModule = module {
    single { AuthHandler(get(), get(), get()) }
    single { ProfileHandler(get(), get()) }
    single { MediaHandler(get(), get()) }// ahora pasa StorageService y ProfileRepository
}

fun Application.configureDependencyInjection() {
    install(Koin) {
        slf4jLogger()
        modules(
            module { single { this@configureDependencyInjection } },
            configModule,
            infrastructureModule,
            applicationModule,
            inboundModule
        )
    }
}