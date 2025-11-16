package backend.infrastructure.inbound.http.dto.chat

import kotlinx.serialization.Serializable

/**
 * DTO para un mensaje ENTRANTE (del cliente al servidor).
 * El cliente solo necesita enviar el cuerpo del mensaje.
 */
@Serializable
data class WsMessageIn(
    val body: String
)

/**
 * DTO para un mensaje SALIENTE (del servidor al cliente).
 * Este es el objeto que Angular recibirá como JSON.
 */
@Serializable
data class WsMessageOut(
    val messageId: Long,
    val chatId: Long,
    val senderId: Long,
    val body: String,
    val sentAt: String // Enviamos fechas como String (Formato ISO)
)