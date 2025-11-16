package backend.application.usecase.chat

import backend.domain.model.ChatMessage
import backend.domain.port.inbound.SendMessageUseCase
import backend.domain.port.outbound.ChatRepository

class SendMessageUseCaseImpl(
    private val chatRepository: ChatRepository
) : SendMessageUseCase {

    override suspend fun execute(chatId: Long, senderId: Long, body: String): Result<ChatMessage> {
        if (body.isBlank()) {
            return Result.failure(IllegalArgumentException("El mensaje no puede estar vacío"))
        }
        return chatRepository.createMessage(chatId, senderId, body)
    }
}