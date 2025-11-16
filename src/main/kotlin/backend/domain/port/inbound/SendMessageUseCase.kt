package backend.domain.port.inbound

import backend.domain.model.ChatMessage

interface SendMessageUseCase {
    suspend fun execute(chatId: Long, senderId: Long, body: String): Result<ChatMessage>
}