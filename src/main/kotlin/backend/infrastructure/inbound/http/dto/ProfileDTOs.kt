package backend.infrastructure.inbound.http.dto

import backend.domain.model.AdviserCard
import backend.domain.model.Profile
import kotlinx.serialization.Serializable
import backend.domain.model.Specialty

// --- DTO para PUT (Para actualizar el perfil) ---
@Serializable
data class UpdateProfileRequest(
    val description: String?,   // ← antes String
    val photoUrl: String?,      // ← antes String
    val city: String?,          // ← antes String
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


@Serializable
data class AdviserCardResponse(
    val userId: Long,
    val firstName: String,
    val lastName: String,
    val photoUrl: String?,
    val level: String?,
    val description: String?,
    val specialties: List<String>
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


// --- 3. AÑADIR ESTA FUNCIÓN MAPPER ---
fun AdviserCard.toDTO(): AdviserCardResponse = AdviserCardResponse(
    userId = this.userId,
    firstName = this.firstName,
    lastName = this.lastName,
    photoUrl = this.photoUrl,
    level = this.level,
    description = this.description,
    specialties = this.specialties
)