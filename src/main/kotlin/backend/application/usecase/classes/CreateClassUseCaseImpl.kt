package backend.application.usecase.classes

import backend.domain.model.ClassSession
import backend.domain.port.inbound.CreateClassUseCase
import backend.domain.port.outbound.ClassRepository

class CreateClassUseCaseImpl(
    private val classRepository: ClassRepository
) : CreateClassUseCase {

    override suspend fun execute(
        command: CreateClassUseCase.CreateClassCommand
    ): Result<ClassSession> {

        if (command.title.isBlank()) {
            return Result.failure(IllegalArgumentException("El título no puede estar vacío"))
        }
        if (command.capacityPerSlot <= 0) {
            return Result.failure(IllegalArgumentException("La capacidad debe ser mayor a 0"))
        }

        return runCatching {
            classRepository.create(
                tutorId = command.tutorId,
                title = command.title.trim(),
                description = command.description?.trim(),
                classDate = command.classDate,
                capacityPerSlot = command.capacityPerSlot,
                specialtyId = command.specialtyId
            )
        }
    }
}
