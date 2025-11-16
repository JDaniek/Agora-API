package backend.infrastructure.inbound.http.handler

import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.isActive
import java.util.concurrent.ConcurrentHashMap

/**
 * Objeto Singleton para gestionar todas las conexiones WebSocket activas.
 * Mantiene un mapa de [chatId] -> (Set de [WebSocketSession]).
 */
object ChatConnectionManager {

    private val chatConnections = ConcurrentHashMap<Long, MutableSet<WebSocketSession>>()

    /**
     * Registra a un usuario cuando se conecta al WebSocket de un chat.
     */
    fun join(chatId: Long, session: WebSocketSession) {
        val connections = chatConnections.computeIfAbsent(chatId) {
            ConcurrentHashMap.newKeySet()
        }
        connections.add(session)
        println("WS JOIN: Usuario ${session.hashCode()} se unió al chat $chatId. Conexiones: ${connections.size}")
    }

    /**
     * Elimina a un usuario cuando se desconecta.
     */
    fun leave(chatId: Long, session: WebSocketSession) {
        val connections = chatConnections[chatId]
        connections?.remove(session)
        println("WS LEAVE: Usuario ${session.hashCode()} abandonó el chat $chatId. Quedan: ${connections?.size}")

        if (connections?.isEmpty() == true) {
            chatConnections.remove(chatId)
        }
    }

    /**
     * Envía un mensaje JSON a TODOS los miembros conectados a una sala de chat.
     */
    suspend fun broadcast(chatId: Long, messageJson: String) {
        chatConnections[chatId]?.forEach { session ->
            if (session.isActive) {
                try {
                    session.send(Frame.Text(messageJson))
                } catch (e: Exception) {
                    println("WS ERROR al enviar a ${session.hashCode()}: ${e.message}")
                }
            }
        }
    }
}