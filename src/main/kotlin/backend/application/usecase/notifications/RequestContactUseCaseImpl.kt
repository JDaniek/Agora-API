package backend.application.usecase.notifications

import backend.domain.port.inbound.RequestContactUseCase
import backend.domain.port.outbound.NotificationRepository

/**
 * Implementación del caso de uso [RequestContactUseCase].
 * Depende del [NotificationRepository] para la persistencia.
 */
class RequestContactUseCaseImpl(
    private val notificationRepository: NotificationRepository
) : RequestContactUseCase {

    override suspend fun requestContact(senderId: Long, recipientId: Long): Result<Long> {
        // Por ahora, la lógica es simple: solo llama al repositorio.
        // En el futuro, aquí podrías añadir lógica de negocio
        // (ej. "no se puede contactar si ya existe un chat").
        return notificationRepository.createContactRequest(senderId, recipientId)
    }
}