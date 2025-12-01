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
// Repositorio de clases
import backend.domain.port.inbound.UpdateClassUseCase
import backend.application.usecase.classes.UpdateClassUseCaseImpl
import backend.domain.port.outbound.ClassRepository
import backend.infrastructure.outbound.persistence.repository.ClassRepositoryPg
import backend.domain.port.inbound.EnrollStudentInClassUseCase
import backend.application.usecase.classes.EnrollStudentInClassUseCaseImpl
// Casos de uso de clases
import backend.domain.port.inbound.GetClassEnrollmentsQuery
import backend.application.usecase.classes.GetClassEnrollmentsQueryImpl

// Caso de uso
import backend.domain.port.inbound.CreateClassUseCase
import backend.application.usecase.classes.CreateClassUseCaseImpl
// Handler
import backend.infrastructure.inbound.http.handler.ClassHandler

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
import backend.domain.port.inbound.MarkNotificationAsReadUseCase
import backend.application.usecase.notifications.MarkNotificationAsReadUseCaseImpl
import backend.domain.port.inbound.GetNotificationsQuery
import backend.domain.port.inbound.RequestContactUseCase
import backend.domain.port.inbound.RejectContactRequestUseCase   // <--- NUEVO
import backend.domain.port.inbound.AcceptContactRequestUseCase
import backend.application.usecase.notifications.GetNotificationsQueryImpl
import backend.application.usecase.notifications.RequestContactUseCaseImpl
import backend.application.usecase.notifications.AcceptContactRequestUseCaseImpl
import backend.application.usecase.notifications.RejectContactRequestUseCaseImpl   // <--- NUEVO


// 3. Chat
import backend.domain.port.inbound.SendMessageUseCase
import backend.domain.port.inbound.GetChatMessagesQuery
import backend.application.usecase.chat.SendMessageUseCaseImpl
import backend.application.usecase.chat.ListMessagesQueryImpl
import backend.application.usecase.classes.DeleteClassUseCaseImpl
import backend.application.usecase.classes.GetStudentEnrolledClassesQueryImpl
import backend.application.usecase.reviews.CreateStudentReviewUseCaseImpl
import backend.domain.port.inbound.DeleteClassUseCase
import backend.domain.port.inbound.GetStudentEnrolledClassesQuery

//4.Reviews
import backend.domain.port.outbound.ReviewRepository
import backend.infrastructure.outbound.persistence.repository.ReviewRepositoryPg
import backend.domain.port.inbound.CreateTeacherReviewUseCase
import backend.domain.port.inbound.GetTeacherReviewsQuery
import backend.application.usecase.reviews.CreateTeacherReviewUseCaseImpl
import backend.application.usecase.reviews.GetStudentReviewSummaryQueryImpl
import backend.application.usecase.reviews.GetStudentReviewsForStudentQueryImpl
import backend.application.usecase.reviews.GetTeacherReviewsQueryImpl
import backend.infrastructure.inbound.http.handler.ReviewHandler
import backend.domain.port.inbound.GetTeacherReviewSummaryQuery
import backend.application.usecase.reviews.GetTeacherReviewSummaryQueryImpl
import backend.domain.port.inbound.CreateStudentReviewUseCase
import backend.domain.port.inbound.GetStudentReviewsForStudentQuery
import backend.domain.port.outbound.StudentReviewRepository
import backend.infrastructure.outbound.persistence.repository.StudentReviewRepositoryPg
import backend.domain.port.inbound.GetStudentReviewSummaryQuery
// --- IMPORTS: Handlers (Infraestructura Inbound) ---
import backend.infrastructure.inbound.http.handler.AuthHandler
import backend.infrastructure.inbound.http.handler.ProfileHandler
import backend.infrastructure.inbound.http.handler.MediaHandler
import backend.infrastructure.inbound.http.handler.AdviserHandler
import backend.infrastructure.inbound.http.handler.NotificationHandler
import backend.infrastructure.inbound.http.handler.ChatHandler // Nuevo
//Imports TeacherFavorite
import backend.domain.port.outbound.TeacherFavoritesRepository
import backend.infrastructure.outbound.persistence.repository.TeacherFavoritesRepositoryPg
import backend.infrastructure.inbound.http.handler.TeacherFavoritesHandler


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
    //Clases
    single<ClassRepository> { ClassRepositoryPg() }   // <--- NUEVO

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

    //Repositorio reviews
    single<ReviewRepository> { ReviewRepositoryPg() }
    //Repositorio reviews alumno
    single<StudentReviewRepository> { StudentReviewRepositoryPg() }
    //Repositorio TeacherFavorite
    single<TeacherFavoritesRepository> { TeacherFavoritesRepositoryPg() }

}

val applicationModule = module {
    //Clases
    single<CreateClassUseCase> { CreateClassUseCaseImpl(get()) }
    single<UpdateClassUseCase> { UpdateClassUseCaseImpl(get()) }
    single<EnrollStudentInClassUseCase> { EnrollStudentInClassUseCaseImpl(get()) }
    single<GetClassEnrollmentsQuery> { GetClassEnrollmentsQueryImpl(get()) }
    single<GetStudentEnrolledClassesQuery> { GetStudentEnrolledClassesQueryImpl(get()) }   // 🔥
    single<DeleteClassUseCase> { DeleteClassUseCaseImpl(get()) }

    // Usuarios
    single<RegisterUserUseCase> { RegisterUserUseCaseImpl(get(), get()) }
    single<LoginUseCase> { LoginUseCaseImpl(get(), get(), get()) }
    single<UpdateProfileUseCase> { UpdateProfileUseCaseImpl(get()) }
    single<GetProfileQuery> { GetProfileQueryImpl(get(), get()) }
    single<SearchAdvisersQuery> { SearchAdvisersQueryImpl(get()) }

    // Notificaciones
    single<GetNotificationsQuery> { GetNotificationsQueryImpl(get()) }
    single<RequestContactUseCase> { RequestContactUseCaseImpl(get()) }
    single<AcceptContactRequestUseCase> { AcceptContactRequestUseCaseImpl(get(), get()) }
    single<RejectContactRequestUseCase> { RejectContactRequestUseCaseImpl(get()) }
    single<MarkNotificationAsReadUseCase> { MarkNotificationAsReadUseCaseImpl(get()) }


    // Chat (¡AGREGADOS!)
    single<SendMessageUseCase> { SendMessageUseCaseImpl(get()) }
    single<GetChatMessagesQuery> { ListMessagesQueryImpl(get()) }

    // Reviews profesor (teacher-side)
    single<GetTeacherReviewSummaryQuery> { GetTeacherReviewSummaryQueryImpl(get()) }
    single<GetTeacherReviewsQuery> { GetTeacherReviewsQueryImpl(get()) }
    single<CreateTeacherReviewUseCase> { CreateTeacherReviewUseCaseImpl(get(), get()) }

    // Reviews alumno (student-side)
    single<CreateStudentReviewUseCase> { CreateStudentReviewUseCaseImpl(get(), get()) }
    single<GetStudentReviewsForStudentQuery> { GetStudentReviewsForStudentQueryImpl(get()) }
    single<GetStudentReviewSummaryQuery> { GetStudentReviewSummaryQueryImpl(get()) }


}

val inboundModule = module {
    //Clases
    single {
        ClassHandler(
            get(), // CreateClassUseCase
            get(), // ClassRepository
            get(), // UpdateClassUseCase
            get(), // EnrollStudentInClassUseCase
            get(), // GetClassEnrollmentsQuery
            get(), // GetStudentEnrolledClassesQuery
            get()  // DeleteClassUseCase
        )
    }
    // Handlers existentes
    single { AuthHandler(get(), get(), get()) }
    single { ProfileHandler(get(), get()) }
    single { MediaHandler(get(), get()) }
    single { AdviserHandler(get(), get()) }

    // Handler de Notificaciones
    single { NotificationHandler(get(), get(), get(), get()) }

    // Handler de Chat (¡AGREGADO!)
    // Necesita: GetChatMessagesQuery, SendMessageUseCase, ChatRepository
    single { ChatHandler(get(), get(), get()) }
    //Reviews
    single {
        ReviewHandler(
            createTeacherReviewUseCase = get(),
            getTeacherReviewsQuery = get(),
            getTeacherReviewSummaryQuery = get(),
            createStudentReviewUseCase = get(),
            getStudentReviewsForStudentQuery = get(),
            getStudentReviewSummaryQuery = get()
        )
    }
    //TeacherFavorite
    single { TeacherFavoritesHandler(get()) }
}

// --- Función Principal de Instalación ---
fun Application.configureDependencyInjection() {
    install(Koin) {
        slf4jLogger()
        modules(
            module { single { this@configureDependencyInjection } }, // Inyectar Application
            configModule, infrastructureModule, applicationModule, inboundModule
        )
    }
}