package backend.application.usecase.reviews

import backend.domain.model.Review
import backend.domain.port.inbound.CreateTeacherReviewUseCase
import backend.domain.port.outbound.ClassRepository
import backend.domain.port.outbound.ReviewRepository
import java.time.LocalDate

class CreateTeacherReviewUseCaseImpl(
    private val reviewRepository: ReviewRepository,
    private val classRepository: ClassRepository
) : CreateTeacherReviewUseCase {

    override suspend fun execute(command: CreateTeacherReviewUseCase.Command): Result<Review> =
        runCatching {
            val rating = command.rating
            if (rating !in 1..5) {
                throw IllegalArgumentException("La calificación debe estar entre 1 y 5")
            }

            if (command.studentId == command.teacherId) {
                throw IllegalArgumentException("No puedes dejar una reseña sobre ti mismo")
            }

            // 1) Validar que el alumno ya tuvo al menos una clase pasada con ese asesor
            val today = LocalDate.now()
            val hasCompleted = classRepository.hasStudentCompletedClassWithTeacher(
                studentId = command.studentId,
                teacherId = command.teacherId,
                untilDate = today
            )

            if (!hasCompleted) {
                throw IllegalStateException(
                    "Solo puedes dejar reseñas a asesores con quienes ya tuviste una clase pasada"
                )
            }

            // 2) Evitar reseñas duplicadas (1 reseña por alumno/asesor)
            if (reviewRepository.hasStudentReviewForTeacher(command.studentId, command.teacherId)) {
                throw IllegalStateException("Ya has dejado una reseña para este asesor")
            }

            // 3) Crear reseña
            reviewRepository.create(
                studentId = command.studentId,
                teacherId = command.teacherId,
                rating = rating,
                comment = command.comment
            )
        }
}