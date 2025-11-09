package backend.domain.model

/**
 * Modelo de datos para la "tarjeta" de un asesor que se muestra
 * en la búsqueda. Combina datos de 'user_accounts', 'profiles' y 'specialty'.
 */
data class AdviserCard(
    val userId: Long,
    val firstName: String,
    val lastName: String,
    val photoUrl: String?,
    val level: String?,
    val description: String?,
    val specialties: List<String> // Lista de nombres, ej: ["Idiomas", "Artes"]
)