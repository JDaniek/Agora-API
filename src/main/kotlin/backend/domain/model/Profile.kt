package backend.domain.model

data class Profile(
    val userId: Long,
    val description: String?,
    val photoUrl: String?,
    val city: String?,
    val stateCode: String?, // (ej: "JAL", "CDMX")
    val level: String?      // (ej: "Universidad")
    // Las especialidades van en una tabla separada
)
