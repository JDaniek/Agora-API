package backend.infrastructure.inbound.http.dto

import backend.domain.model.Profile
import kotlinx.serialization.Serializable
import backend.domain.model.Specialty

// --- DTO para PUT (Para actualizar el perfil) ---
@Serializable
data class UpdateProfileRequest(
    val description: String,
    val photoUrl: String,
    val city: String,
    val stateCode: String,
    val level: String,
    val specialtyIds: List<Long>
)

// --- DTOs para GET (Para leer el perfil) ---

@Serializable
data class SpecialtyDTO(
    val id: Long,
    val name: String
)

@Serializable
data class ProfileResponse(
    val userId: Long,
    val description: String?,
    val photoUrl: String?,
    val city: String?,
    val stateCode: String?,
    val level: String?,
    val specialties: List<SpecialtyDTO> // Incluimos las especialidades
)

// --- Mappers (Funciones para convertir Modelos a DTOs) ---

fun Specialty.toDTO(): SpecialtyDTO = SpecialtyDTO(
    id = this.id,
    name = this.name
)

fun Profile.toResponseDTO(specialties: List<Specialty>): ProfileResponse = ProfileResponse(
    userId = this.userId,
    description = this.description,
    photoUrl = this.photoUrl,
    city = this.city,
    stateCode = this.stateCode,
    level = this.level,
    specialties = specialties.map { it.toDTO() } // Convertimos la lista
)