package backend.plugins

import backend.application.usecase.users.LoginUseCaseImpl
import backend.application.usecase.users.RegisterUserUseCaseImpl
import backend.domain.port.inbound.LoginUseCase
import backend.domain.port.inbound.RegisterUserUseCase
import backend.domain.port.outbound.PasswordService
import backend.domain.port.outbound.UserRepository
import backend.infrastructure.inbound.http.handler.AuthHandler
import backend.infrastructure.outbound.persistence.repository.UserRepositoryPg
import backend.infrastructure.security.JwtConfig
import backend.infrastructure.security.JwtService
import backend.infrastructure.security.PasswordServiceImpl
import io.ktor.server.application.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

// Módulo para la configuración cargada desde el environment
val configModule = module {
    single {
        JwtConfig(
            secret = get<Application>().environment.config.property("jwt.secret").getString(),
            issuer = get<Application>().environment.config.property("jwt.issuer").getString(),
            audience = get<Application>().environment.config.property("jwt.audience").getString(),
            realm = get<Application>().environment.config.property("jwt.realm").getString(),
            expirationMinutes = get<Application>().environment.config.property("jwt.expirationMinutes").getString().toLong()
        )
    }
}

// Módulo para los adaptadores de infraestructura
val infrastructureModule = module {
    // Adaptadores de Salida (Outbound)
    single<UserRepository> { UserRepositoryPg() }
    single<PasswordService> { PasswordServiceImpl() }
    single { JwtService(get()) } // Koin inyectará JwtConfig aquí
}

// Módulo para los casos de uso (Aplicación)
val applicationModule = module {
    single<RegisterUserUseCase> { RegisterUserUseCaseImpl(get(), get()) }
    single<LoginUseCase> { LoginUseCaseImpl(get(), get(), get()) }
}

// Módulo para los adaptadores de entrada (Inbound)
val inboundModule = module {
    single { AuthHandler(get(), get(), get()) }

    // TODO: Descomenta esto cuando implementes ClassHandler
    // single { ClassHandler(/* ...tus dependencias... */) }

    // TODO: Descomenta esto cuando implementes ChatHandler
    // single { ChatHandler(/* ...tus dependencias... */) }
}


// Función principal de configuración de Koin
fun Application.configureDependencyInjection() {
    install(Koin) {
        slf4jLogger()
        // Importante: Koin necesita acceso a 'Application' para leer la config
        modules(
            module { single { this@configureDependencyInjection } },
            configModule,
            infrastructureModule,
            applicationModule,
            inboundModule
        )
    }
}