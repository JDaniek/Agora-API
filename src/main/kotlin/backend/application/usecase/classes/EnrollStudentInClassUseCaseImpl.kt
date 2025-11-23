package backend.application.usecase.classes

import backend.domain.port.inbound.EnrollStudentInClassUseCase
import backend.domain.port.outbound.ClassRepository

class EnrollStudentInClassUseCaseImpl(
    private val classRepository: ClassRepository
) : EnrollStudentInClassUseCase {

    override suspend fun execute(command: EnrollStudentInClassUseCase.Command): Result<Unit> {
        return runCatching {
            // 1. Verificar que la clase exista
            val cls = classRepository.findById(command.classId)
                ?: throw IllegalArgumentException("La clase no existe")

            // 2. Seguridad: solo el tutor dueño de la clase puede inscribir
            if (cls.tutorId != command.tutorId) {
                throw SecurityException("No puedes inscribir alumnos en esta clase")
            }

            // 3. Verificar si el alumno ya está inscrito
            if (classRepository.isStudentEnrolledInClass(command.classId, command.studentId)) {
                throw IllegalStateException("El alumno ya está inscrito en esta sesión")
            }

            // 4. Verificar capacidad
            val current = classRepository.countConfirmedEnrollments(command.classId)
            if (current >= cls.capacityPerSlot) {
                throw IllegalStateException("La clase ya alcanzó su capacidad máxima")
            }

            // 5. Insertar inscripción como 'confirmed'
            classRepository.addEnrollment(
                tutorId = command.tutorId,
                classId = command.classId,
                studentId = command.studentId,
                status = "confirmed"
            )
        }
    }
}
