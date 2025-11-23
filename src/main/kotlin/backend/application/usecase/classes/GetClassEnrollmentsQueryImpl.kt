package backend.application.usecase.classes

import backend.domain.model.ClassEnrollmentDetails
import backend.domain.port.inbound.GetClassEnrollmentsQuery
import backend.domain.port.outbound.ClassRepository

class GetClassEnrollmentsQueryImpl(
    private val classRepository: ClassRepository
) : GetClassEnrollmentsQuery {

    override suspend fun getEnrollments(
        tutorId: Long,
        classId: Long
    ): Result<List<ClassEnrollmentDetails>> = runCatching {
        val cls = classRepository.findById(classId)
            ?: throw NoSuchElementException("La clase no existe")

        if (cls.tutorId != tutorId) {
            throw SecurityException("No puedes ver las inscripciones de esta clase")
        }

        classRepository.findEnrollmentDetailsForClass(classId)
    }
}