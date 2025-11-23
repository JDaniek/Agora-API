package backend.domain.port.inbound

import backend.domain.model.ClassSession
import java.time.LocalDate

interface CreateClassUseCase {

    suspend fun execute(command: CreateClassCommand): Result<ClassSession>

    data class CreateClassCommand(
        val tutorId: Long,
        val title: String,
        val description: String?,
        val classDate: LocalDate,
        val capacityPerSlot: Int,
        val specialtyId: Int
    )
}
