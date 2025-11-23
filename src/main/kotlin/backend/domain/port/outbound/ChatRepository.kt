package backend.domain.port.outbound

import backend.domain.model.ChatMessage

interface ChatRepository {

    suspend fun createPrivateChat(userOneId: Long, userTwoId: Long): Result<Long>

    suspend fun createMessage(chatId: Long, senderId: Long, body: String): Result<ChatMessage>

    suspend fun getChatMessages(chatId: Long): Result<List<ChatMessage>>

    suspend fun isUserMemberOfChat(userId: Long, chatId: Long): Boolean

    /**
     * Busca un chat privado donde estén ambos usuarios (sin importar el orden).
     * @return Result con el ID del chat si existe, o null si no existe.
     */
    suspend fun findPrivateChatBetweenUsers(userOneId: Long, userTwoId: Long): Result<Long?>
}
