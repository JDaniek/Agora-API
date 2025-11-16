package backend.infrastructure.plugins

// --- Imports de Adviser (Existentes y Nuevos) ---
import backend.application.usecase.users.SearchAdvisersQueryImpl
import backend.domain.port.inbound.SearchAdvisersQuery
import backend.infrastructure.inbound.http.handler.AdviserHandler

// --- Imports de Cloudinary (Existentes) ---
import backend.domain.port.outbound.StorageService
import backend.infrastructure.outbound.storage.CloudinaryStorageService
import backend.infrastructure.inbound.http.handler.MediaHandler
import com.cloudinary.Cloudinary

// --- Imports de Casos de Uso (Existentes) ---
import backend.application.usecase.users.GetProfileQueryImpl
import backend.application.usecase.users.LoginUseCaseImpl
import backend.application.usecase.users.RegisterUserUseCaseImpl
import backend.application.usecase.users.UpdateProfileUseCaseImpl

// --- Imports de Dominio (Existentes) ---
import backend.domain.port.inbound.GetProfileQuery
import backend.domain.port.inbound.LoginUseCase
import backend.domain.port.inbound.RegisterUserUseCase
import backend.domain.port.inbound.UpdateProfileUseCase
import backend.domain.port.outbound.PasswordService
import backend.domain.port.outbound.ProfileRepository
import backend.domain.port.outbound.SpecialtyRepository
import backend.domain.port.outbound.UserRepository

// --- Imports de Infraestructura (Existentes) ---
import backend.infrastructure.inbound.http.handler.AuthHandler
import backend.infrastructure.inbound.http.handler.ProfileHandler
import backend.infrastructure.outbound.persistence.repository.ProfileRepositoryPg
import backend.infrastructure.outbound.persistence.repository.SpecialtyRepositoryPg
import backend.infrastructure.outbound.persistence.repository.UserRepositoryPg
import backend.infrastructure.security.JwtConfig
import backend.infrastructure.security.JwtService
import backend.infrastructure.security.PasswordServiceImpl

// --- Imports de Ktor y Koin (Existentes) ---
import io.ktor.server.application.*
import io.ktor.server.config.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

// --- ¡NUEVOS IMPORTS PARA NOTIFICACIONES Y CHAT! ---
import backend.application.usecase.notifications.AcceptContactRequestUseCaseImpl
import backend.application.usecase.notifications.GetNotificationsQueryImpl
import backend.application.usecase.notifications.RequestContactUseCaseImpl
import backend.domain.port.inbound.AcceptContactRequestUseCase
import backend.domain.port.inbound.GetNotificationsQuery
import backend.domain.port.inbound.RequestContactUseCase
import backend.domain.port.outbound.ChatRepository
import backend.domain.port.outbound.NotificationRepository
import backend.infrastructure.inbound.http.handler.NotificationHandler
import backend.infrastructure.outbound.persistence.repository.ChatRepositoryPg
import backend.infrastructure.outbound.persistence.repository.NotificationRepositoryPg
// --- FIN DE NUEVOS IMPORTS ---

// Helpers (Existentes)
private fun ApplicationConfig.prop(path: String) =
    propertyOrNull(path)?.getString()

private fun sys(name: String) =
    System.getProperty(name) ?: System.getenv(name)

// --- Módulos de Koin ---

// (Tu configModule está perfecto)
val configModule = module {
    single<ApplicationConfig> { get<Application>().environment.config }
    single { /* ... tu lógica de JwtConfig ... */
        val cfg = get<ApplicationConfig>()
        val secret = cfg.prop("security.jwt.secret") ?: sys("JWT_SECRET")
        ?: error("Falta security.jwt.secret o env/system JWT_SECRET")
        val issuer = cfg.prop("security.jwt.issuer") ?: "agora.auth"
        val audience = cfg.prop("security.jwt.audience") ?: "agora.clients"
        val realm = cfg.prop("security.jwt.realm") ?: "Access to Agora API"
        val expMinutes = (cfg.prop("security.jwt.expiresInMinutes") ?: "60").toLong()
        JwtConfig(secret, issuer, audience, realm, expMinutes)
    }
    single<Cloudinary> { /* ... tu lógica de Cloudinary ... */
        val cfg = get<ApplicationConfig>()
        val cloudinaryUrl = sys("CLOUDINARY_URL") ?: cfg.prop("cloudinary.url")

        if (cloudinaryUrl != null) {
            Cloudinary(cloudinaryUrl) // Usar URL si existe
        } else {
            val name = cfg.prop("cloudinary.cloud_name")
                ?: sys("CLOUDINARY_CLOUD_NAME")
                ?: error("Falta cloudinary.cloud_name o env CLOUDINARY_CLOUD_NAME")
            val key = cfg.prop("cloudinary.api_key")
                ?: sys("CLOUDINARY_API_KEY")
                ?: error("Falta cloudinary.api_key o env CLOUDINARY_API_KEY")
            val secret = cfg.prop("cloudinary.api_secret")
                ?: sys("CLOUDINARY_API_SECRET")
                ?: error("Falta cloudinary.api_secret o env CLOUDINARY_API_SECRET")

            Cloudinary(
                mapOf(
                    "cloud_name" to name, "api_key" to key, "api_secret" to secret, "secure" to true
                )
            )
        }
    }
}

// (Tu infrastructureModule existente + nuevos repos)
val infrastructureModule = module {
    // Repositorios existentes
    single<UserRepository> { UserRepositoryPg() }
    single<ProfileRepository> { ProfileRepositoryPg() }
    single<SpecialtyRepository> { SpecialtyRepositoryPg() }
    single<PasswordService> { PasswordServiceImpl() }
    single { JwtService(get()) }
    single<StorageService> { CloudinaryStorageService(get()) }

    // --- NUEVO: Repositorios de Notificaciones y Chat ---
    single<NotificationRepository> { NotificationRepositoryPg() }
    single<ChatRepository> { ChatRepositoryPg() }
}

// (Tu applicationModule existente + nuevos casos de uso)
val applicationModule = module {
    // Casos de uso existentes
    single<RegisterUserUseCase> { RegisterUserUseCaseImpl(get(), get()) }
    single<LoginUseCase> { LoginUseCaseImpl(get(), get(), get()) }
    single<UpdateProfileUseCase> { UpdateProfileUseCaseImpl(get()) }
    single<GetProfileQuery> { GetProfileQueryImpl(get(), get()) }
    single<SearchAdvisersQuery> { SearchAdvisersQueryImpl(get()) }

    // --- NUEVO: Casos de Uso de Notificaciones ---
    single<GetNotificationsQuery> { GetNotificationsQueryImpl(get()) }
    single<RequestContactUseCase> { RequestContactUseCaseImpl(get()) }
    single<AcceptContactRequestUseCase> { AcceptContactRequestUseCaseImpl(get(), get()) }
}

// --- ¡¡MÓDULO CORREGIDO!! ---
// Construido basándonos en los constructores de tus Handlers
val inboundModule = module {
    // AuthHandler(RegisterUserUseCase, LoginUseCase, JwtService) -> 3 dependencias
    single { AuthHandler(get(), get(), get()) }

    // ProfileHandler(UpdateProfileUseCase, GetProfileQuery) -> 2 dependencias
    single { ProfileHandler(get(), get()) }

    // MediaHandler(StorageService, ProfileRepository) -> 2 dependencias
    single { MediaHandler(get(), get()) }

    // AdviserHandler(SearchAdvisersQuery, RequestContactUseCase) -> 2 dependencias
    single { AdviserHandler(get(), get()) }

    // NotificationHandler(GetNotificationsQuery, AcceptContactRequestUseCase) -> 2 dependencias
    single { NotificationHandler(get(), get()) }
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