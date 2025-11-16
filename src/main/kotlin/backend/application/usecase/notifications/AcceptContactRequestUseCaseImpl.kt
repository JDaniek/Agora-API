package backend.application.usecase.notifications

import backend.domain.port.inbound.AcceptContactRequestUseCase
import backend.domain.port.outbound.ChatRepository
import backend.domain.port.outbound.NotificationRepository

/**
 * Implementación del caso de uso [AcceptContactRequestUseCase].
 * Depende de AMBOS repositorios para orquestar la lógica.
 */
class AcceptContactRequestUseCaseImpl(
    private val notificationRepository: NotificationRepository,
    private val chatRepository: ChatRepository
) : AcceptContactRequestUseCase {

    override suspend fun acceptRequest(notificationId: Long, acceptorUserId: Long): Result<Long> {
        // 1. Buscar la notificación para obtener los IDs y validarla
        val notificationResult = notificationRepository.findById(notificationId)
        val notification = notificationResult.getOrNull()
            ?: return Result.failure(Exception("Notificación no encontrada"))

        // 2. Validaciones de negocio
        if (notification.recipientId != acceptorUserId) {
            return Result.failure(Exception("No autorizado. Esta notificación no es tuya."))
        }
        if (notification.status != "pending") {
            return Result.failure(Exception("Esta solicitud ya fue ${notification.status}."))
        }

        // 3. Lógica principal (¡ambos pasos deben tener éxito!)
        return try {
            // Paso A: Crear el chat
            val newChatId = chatRepository.createPrivateChat(
                userOneId = notification.senderId,
                userTwoId = notification.recipientId
            ).getOrThrow() // Si esto falla, el 'catch' lo captura

            // Paso B: Actualizar la notificación a "accepted"
            notificationRepository.updateStatus(
                notificationId = notificationId,
                newStatus = "accepted",
                recipientId = acceptorUserId
            ).getOrThrow() // Si esto falla, el 'catch' lo captura (idealmente, se haría un rollback)

            // 4. Éxito: devolver el ID del nuevo chat
            Result.success(newChatId)

        } catch (e: Exception) {
            // Si CUALQUIERA de los pasos falla, devolvemos el error
            Result.failure(Exception("Error al aceptar la solicitud: ${e.message}"))
        }
    }
}