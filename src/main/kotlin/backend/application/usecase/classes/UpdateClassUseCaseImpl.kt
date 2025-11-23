package backend.application.usecase.classes

import backend.domain.model.ClassSession
import backend.domain.port.inbound.UpdateClassUseCase
import backend.domain.port.outbound.ClassRepository
import java.time.LocalDate

class UpdateClassUseCaseImpl(
    private val classRepository: ClassRepository
) : UpdateClassUseCase {

    override suspend fun execute(
        command: UpdateClassUseCase.UpdateClassCommand
    ): Result<ClassSession> {

        // 1. Validaciones de entrada

        command.title?.let {
            if (it.isBlank()) {
                return Result.failure(
                    IllegalArgumentException("El título no puede estar vacío")
                )
            }
        }

        command.capacityPerSlot?.let {
            if (it <= 0) {
                return Result.failure(
                    IllegalArgumentException("La capacidad debe ser mayor a 0")
                )
            }
        }

        command.classDate?.let { date ->
            // Si quieres evitar fechas pasadas, descomenta:
            // if (date.isBefore(LocalDate.now())) {
            //     return Result.failure(
            //         IllegalArgumentException("La fecha de la clase no puede ser en el pasado")
            //     )
            // }
        }

        // 2. Verificar que la clase exista y sea del tutor
        val existing = classRepository.findById(command.classId)
        if (existing == null) {
            return Result.failure(
                NoSuchElementException("La clase no existe")
            )
        }
        if (existing.tutorId != command.tutorId) {
            return Result.failure(
                IllegalAccessException("No estás autorizado para modificar esta clase")
            )
        }

        // 3. Ejecutar actualización parcial
        return runCatching {
            val updated = classRepository.update(
                classId = command.classId,
                tutorId = command.tutorId,
                title = command.title?.trim(),
                description = command.description?.trim(),
                classDate = command.classDate,
                capacityPerSlot = command.capacityPerSlot,
                specialtyId = command.specialtyId,
                isActive = command.isActive
            ) ?: throw IllegalStateException("No se pudo actualizar la clase")
            updated
        }
    }
}
