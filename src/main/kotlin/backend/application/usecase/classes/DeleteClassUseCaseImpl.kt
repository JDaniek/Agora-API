package backend.application.usecase.classes

import backend.domain.port.inbound.DeleteClassUseCase
import backend.domain.port.outbound.ClassRepository

class DeleteClassUseCaseImpl(
    private val classRepository: ClassRepository
) : DeleteClassUseCase {

    override suspend fun execute(command: DeleteClassUseCase.Command): Result<Unit> =
        runCatching {
            val cls = classRepository.findById(command.classId)
                ?: throw NoSuchElementException("La clase no existe")

            if (cls.tutorId != command.tutorId) {
                throw IllegalAccessException("No puedes eliminar esta clase")
            }

            val deleted = classRepository.deleteClassByIdAndTutor(command.classId, command.tutorId)
            if (!deleted) {
                throw IllegalStateException("No se pudo eliminar la clase")
            }
        }
}
