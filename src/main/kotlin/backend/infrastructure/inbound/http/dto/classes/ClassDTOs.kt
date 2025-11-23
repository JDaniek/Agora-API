package backend.infrastructure.inbound.http.dto.classes

import kotlinx.serialization.Serializable

@Serializable
data class CreateClassRequest(
    val title: String,
    val description: String? = null,
    val classDate: String,      // "YYYY-MM-DD"
    val capacityPerSlot: Int,
    val specialtyId: Int
)

@Serializable
data class ClassResponse(
    val id: Long,
    val title: String,
    val description: String?,
    val classDate: String,
    val capacityPerSlot: Int,
    val specialtyId: Int,
    val isActive: Boolean
)
