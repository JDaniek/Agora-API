package backend.infrastructure.inbound.http.dto.classes

import kotlinx.serialization.Serializable

@Serializable
data class UpdateClassRequest(
    val title: String? = null,
    val description: String? = null,
    val classDate: String? = null,      // "YYYY-MM-DD"
    val capacityPerSlot: Int? = null,
    val specialtyId: Int? = null,
    val isActive: Boolean? = null
)
