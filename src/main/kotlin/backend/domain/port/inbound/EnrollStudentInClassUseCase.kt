package backend.domain.port.inbound

interface EnrollStudentInClassUseCase {

    data class Command(
        val tutorId: Long,
        val classId: Long,
        val studentId: Long
    )

    suspend fun execute(command: Command): Result<Unit>
}
