package backend.domain.port.inbound

interface DeleteClassUseCase {

    data class Command(
        val tutorId: Long,
        val classId: Long
    )

    suspend fun execute(command: Command): Result<Unit>
}