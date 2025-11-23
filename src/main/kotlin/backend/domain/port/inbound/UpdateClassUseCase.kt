package backend.domain.port.inbound

import backend.domain.model.ClassSession
import java.time.LocalDate

interface UpdateClassUseCase {

    data class UpdateClassCommand(
        val classId: Long,
        val tutorId: Long,
        val title: String?,
        val description: String?,
        val classDate: LocalDate?,
        val capacityPerSlot: Int?,
        val specialtyId: Int?,
        val isActive: Boolean?
    )

    suspend fun execute(command: UpdateClassCommand): Result<ClassSession>
}
