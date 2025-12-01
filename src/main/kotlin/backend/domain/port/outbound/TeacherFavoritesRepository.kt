package backend.domain.port.outbound

import backend.domain.model.AdviserCard

interface TeacherFavoritesRepository {
    suspend fun addFavorite(studentId: Long, teacherId: Long)
    suspend fun removeFavorite(studentId: Long, teacherId: Long)
    suspend fun isFavorite(studentId: Long, teacherId: Long): Boolean
    suspend fun findFavoritesForStudent(studentId: Long): List<Long> // lista de teacherId
    suspend fun findFavoriteAdvisersForStudent(studentId: Long): List<AdviserCard>


}