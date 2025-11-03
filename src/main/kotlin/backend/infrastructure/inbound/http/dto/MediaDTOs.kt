// src/main/kotlin/backend/infrastructure/inbound/http/dto/MediaDTOs.kt
package backend.infrastructure.inbound.http.dto

import kotlinx.serialization.Serializable

@Serializable
data class UploadAndAttachResponse(
    val photoUrl: String
)
