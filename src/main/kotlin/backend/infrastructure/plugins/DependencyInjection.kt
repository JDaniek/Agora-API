package backend.infrastructure.plugins

// --- IMPORTS: Configuración y Ktor ---
import io.ktor.server.application.*
import io.ktor.server.config.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

// --- IMPORTS: Cloudinary ---
import com.cloudinary.Cloudinary
import backend.domain.port.outbound.StorageService
import backend.infrastructure.outbound.storage.CloudinaryStorageService

// --- IMPORTS: Seguridad ---
import backend.domain.port.outbound.PasswordService
import backend.infrastructure.security.JwtConfig
import backend.infrastructure.security.JwtService
import backend.infrastructure.security.PasswordServiceImpl

// --- IMPORTS: Repositorios (Infraestructura) ---
import backend.domain.port.outbound.UserRepository
import backend.domain.port.outbound.ProfileRepository
import backend.domain.port.outbound.SpecialtyRepository
import backend.domain.port.outbound.NotificationRepository // Nuevo
import backend.domain.port.outbound.ChatRepository         // Nuevo
import backend.infrastructure.outbound.persistence.repository.UserRepositoryPg
import backend.infrastructure.outbound.persistence.repository.ProfileRepositoryPg
import backend.infrastructure.outbound.persistence.repository.SpecialtyRepositoryPg
import backend.infrastructure.outbound.persistence.repository.NotificationRepositoryPg // Nuevo
import backend.infrastructure.outbound.persistence.repository.ChatRepositoryPg         // Nuevo

// --- IMPORTS: Casos de Uso (Application -> Domain) ---
// 1. Usuarios y Perfil
import backend.domain.port.inbound.RegisterUserUseCase
import backend.domain.port.inbound.LoginUseCase
import backend.domain.port.inbound.UpdateProfileUseCase
import backend.domain.port.inbound.GetProfileQuery
import backend.domain.port.inbound.SearchAdvisersQuery
import backend.application.usecase.users.RegisterUserUseCaseImpl
import backend.application.usecase.users.LoginUseCaseImpl
import backend.application.usecase.users.UpdateProfileUseCaseImpl
import backend.application.usecase.users.GetProfileQueryImpl
import backend.application.usecase.users.SearchAdvisersQueryImpl

// 2. Notificaciones
import backend.domain.port.inbound.GetNotificationsQuery
import backend.domain.port.inbound.RequestContactUseCase
import backend.domain.port.inbound.AcceptContactRequestUseCase
import backend.application.usecase.notifications.GetNotificationsQueryImpl
import backend.application.usecase.notifications.RequestContactUseCaseImpl
import backend.application.usecase.notifications.AcceptContactRequestUseCaseImpl

// 3. Chat (¡Estos faltaban!)
import backend.domain.port.inbound.SendMessageUseCase
import backend.domain.port.inbound.GetChatMessagesQuery
import backend.application.usecase.chat.SendMessageUseCaseImpl
import backend.application.usecase.chat.ListMessagesQueryImpl

// --- IMPORTS: Handlers (Infraestructura Inbound) ---
import backend.infrastructure.inbound.http.handler.AuthHandler
import backend.infrastructure.inbound.http.handler.ProfileHandler
import backend.infrastructure.inbound.http.handler.MediaHandler
import backend.infrastructure.inbound.http.handler.AdviserHandler
import backend.infrastructure.inbound.http.handler.NotificationHandler
import backend.infrastructure.inbound.http.handler.ChatHandler // Nuevo

// --- Helpers de Configuración ---
private fun ApplicationConfig.prop(path: String) = propertyOrNull(path)?.getString()
private fun sys(name: String) = System.getProperty(name) ?: System.getenv(name)

// ==========================================
//              MÓDULOS KOIN
// ==========================================

val configModule = module {
    single<ApplicationConfig> { get<Application>().environment.config }

    // Configuración JWT
    single {
        val cfg = get<ApplicationConfig>()
        val secret = cfg.prop("security.jwt.secret") ?: sys("JWT_SECRET") ?: error("Falta JWT_SECRET")
        val issuer = cfg.prop("security.jwt.issuer") ?: "agora.auth"
        val audience = cfg.prop("security.jwt.audience") ?: "agora.clients"
        val realm = cfg.prop("security.jwt.realm") ?: "Access to Agora API"
        val expMinutes = (cfg.prop("security.jwt.expiresInMinutes") ?: "60").toLong()
        JwtConfig(secret, issuer, audience, realm, expMinutes)
    }

    // Configuración Cloudinary
    single<Cloudinary> {
        val cfg = get<ApplicationConfig>()
        val cloudinaryUrl = sys("CLOUDINARY_URL") ?: cfg.prop("cloudinary.url")

        if (cloudinaryUrl != null) {
            Cloudinary(cloudinaryUrl)
        } else {
            val name = cfg.prop("cloudinary.cloud_name") ?: sys("CLOUDINARY_CLOUD_NAME") ?: error("Falta Cloud Name")
            val key = cfg.prop("cloudinary.api_key") ?: sys("CLOUDINARY_API_KEY") ?: error("Falta API Key")
            val secret = cfg.prop("cloudinary.api_secret") ?: sys("CLOUDINARY_API_SECRET") ?: error("Falta API Secret")

            Cloudinary(mapOf("cloud_name" to name, "api_key" to key, "api_secret" to secret, "secure" to true))
        }
    }
}

val infrastructureModule = module {
    // Servicios Base
    single<PasswordService> { PasswordServiceImpl() }
    single { JwtService(get()) }
    single<StorageService> { CloudinaryStorageService(get()) }

    // Repositorios Base
    single<UserRepository> { UserRepositoryPg() }
    single<ProfileRepository> { ProfileRepositoryPg() }
    single<SpecialtyRepository> { SpecialtyRepositoryPg() }

    // Repositorios Nuevos (Notificaciones y Chat)
    single<NotificationRepository> { NotificationRepositoryPg() }
    single<ChatRepository> { ChatRepositoryPg() }
}

val applicationModule = module {
    // Usuarios
    single<RegisterUserUseCase> { RegisterUserUseCaseImpl(get(), get()) }
    single<LoginUseCase> { LoginUseCaseImpl(get(), get(), get()) }
    single<UpdateProfileUseCase> { UpdateProfileUseCaseImpl(get()) }
    single<GetProfileQuery> { GetProfileQueryImpl(get(), get()) }
    single<SearchAdvisersQuery> { SearchAdvisersQueryImpl(get()) }

    // Notificaciones
    single<GetNotificationsQuery> { GetNotificationsQueryImpl(get()) }
    single<RequestContactUseCase> { RequestContactUseCaseImpl(get()) }
    // AcceptContact necesita NotificationRepo Y ChatRepo, por eso get(), get()
    single<AcceptContactRequestUseCase> { AcceptContactRequestUseCaseImpl(get(), get()) }

    // Chat (¡AGREGADOS!)
    single<SendMessageUseCase> { SendMessageUseCaseImpl(get()) }
    single<GetChatMessagesQuery> { ListMessagesQueryImpl(get()) }
}

val inboundModule = module {
    // Handlers existentes
    single { AuthHandler(get(), get(), get()) }
    single { ProfileHandler(get(), get()) }
    single { MediaHandler(get(), get()) }
    single { AdviserHandler(get(), get()) }

    // Handler de Notificaciones
    single { NotificationHandler(get(), get()) }

    // Handler de Chat (¡AGREGADO!)
    // Necesita: GetChatMessagesQuery, SendMessageUseCase, ChatRepository
    single { ChatHandler(get(), get(), get()) }
}

// --- Función Principal de Instalación ---
fun Application.configureDependencyInjection() {
    install(Koin) {
        slf4jLogger()
        modules(
            module { single { this@configureDependencyInjection } }, // Inyectar Application
            configModule,
            infrastructureModule,
            applicationModule,
            inboundModule
        )
    }
}