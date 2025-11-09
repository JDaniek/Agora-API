package backend.infrastructure.inbound.http.handler

import backend.domain.port.inbound.SearchAdvisersQuery
import backend.infrastructure.inbound.http.dto.toDTO
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.http.*

class AdviserHandler(
    private val searchAdvisersQuery: SearchAdvisersQuery
) {
    suspend fun handleSearchAdvisers(call: ApplicationCall) {
        try {
            // 1. Leer los parámetros de la URL
            val params = call.request.queryParameters
            val query = params["q"]
            val state = params["state"]
            val level = params["level"]
            // 'getAll' lee todos los parámetros "specialty" (ej. ...?specialty=1&specialty=3)
            val specialtyIds = params.getAll("specialty")?.mapNotNull { it.toIntOrNull() }

            // 2. Crear el comando del caso de uso
            val command = SearchAdvisersQuery.SearchCommand(
                query = query,
                state = state,
                level = level,
                specialtyIds = specialtyIds
            )

            // 3. Ejecutar la búsqueda
            val adviserCards = searchAdvisersQuery.execute(command)

            // 4. Mapear a DTOs y responder
            call.respond(HttpStatusCode.OK, adviserCards.map { it.toDTO() })

        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, "Error al buscar asesores: ${e.message}")
        }
    }
}