package backend.infrastructure.inbound.http.mapper.classes

import backend.domain.model.ChatMessage
import backend.infrastructure.inbound.http.dto.chat.WsMessageOut
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Función de extensión para mapear (convertir) nuestro modelo de dominio
 * ChatMessage a un DTO público WsMessageOut.
 */
fun ChatMessage.toWsMessageOut(): WsMessageOut {

    // Usamos un formateador estándar ISO-8601 para el 'Instant'
    val formatter = DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.of("UTC"))

    return WsMessageOut(
        messageId = this.messageId,
        chatId = this.chatId,
        senderId = this.senderId,
        body = this.body,
        sentAt = formatter.format(this.sentAt)
    )
}