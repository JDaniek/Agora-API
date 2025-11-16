package backend.domain.port.outbound

import backend.domain.model.ChatMessage

interface ChatRepository {

    suspend fun createPrivateChat(userOneId: Long, userTwoId: Long): Result<Long>

    /**
     * Guarda un mensaje. Retorna el objeto ChatMessage completo.
     */
    suspend fun createMessage(chatId: Long, senderId: Long, body: String): Result<ChatMessage>

    /**
     * Obtiene el historial de mensajes.
     */
    suspend fun getChatMessages(chatId: Long): Result<List<ChatMessage>>

    suspend fun isUserMemberOfChat(userId: Long, chatId: Long): Boolean
}