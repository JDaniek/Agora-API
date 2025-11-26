package backend.application.usecase.reviews

import backend.domain.model.StudentReview
import backend.domain.port.inbound.CreateStudentReviewUseCase
import backend.domain.port.outbound.ClassRepository
import backend.domain.port.outbound.StudentReviewRepository
import java.time.LocalDate

class CreateStudentReviewUseCaseImpl(
    private val studentReviewRepository: StudentReviewRepository,
    private val classRepository: ClassRepository
) : CreateStudentReviewUseCase {

    override suspend fun execute(command: CreateStudentReviewUseCase.Command): Result<StudentReview> =
        runCatching {
            val rating = command.rating
            if (rating !in 1..5) {
                throw IllegalArgumentException("La calificación debe estar entre 1 y 5")
            }

            // No puede evaluarse a sí mismo
            if (command.teacherId == command.studentId) {
                throw IllegalArgumentException("No puedes dejar una reseña sobre ti mismo")
            }

            // 1) Validar que el alumno haya tenido al menos una clase pasada con este asesor
            val today = LocalDate.now()
            val hasCompleted = classRepository.hasStudentCompletedClassWithTeacher(
                studentId = command.studentId,
                teacherId = command.teacherId,
                untilDate = today
            )

            if (!hasCompleted) {
                throw IllegalStateException(
                    "Solo puedes calificar alumnos con los que ya tuviste una clase pasada"
                )
            }

            // 2) Evitar reseñas duplicadas (1 reseña por asesor/alumno)
            if (studentReviewRepository.hasTeacherReviewForStudent(
                    teacherId = command.teacherId,
                    studentId = command.studentId
                )
            ) {
                throw IllegalStateException("Ya has dejado una reseña para este alumno")
            }

            // 3) Crear reseña
            studentReviewRepository.create(
                studentId = command.studentId,
                teacherId = command.teacherId,
                rating = rating,
                comment = command.comment
            )
        }
}