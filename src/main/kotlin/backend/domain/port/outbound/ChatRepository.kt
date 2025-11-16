package backend.domain.port.outbound

import backend.domain.model.Chat // <-- (Necesitarás este modelo)
import backend.domain.model.ChatMessage // <-- (Y este)

/**
 * Puerto de salida para manejar la persistencia de Chats y Mensajes.
 */
interface ChatRepository {

    /**
     * Crea un nuevo chat privado entre dos usuarios
     * y añade a ambos usuarios como miembros.
     *
     * @param userOneId El ID del primer usuario.
     * @param userTwoId El ID del segundo usuario.
     * @return Result con el ID del nuevo chat creado.
     */
    suspend fun createPrivateChat(userOneId: Long, userTwoId: Long): Result<Long>

    // (Estos son los métodos que tus Casos de Uso de Chat (SendMessage) usarán después)
    // suspend fun getChatMessages(chatId: Long): Result<List<ChatMessage>>
    // suspend fun createMessage(chatId: Long, senderId: Long, body: String): Result<ChatMessage>
}