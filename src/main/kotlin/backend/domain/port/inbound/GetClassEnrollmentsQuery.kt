package backend.domain.port.inbound

import backend.domain.model.ClassEnrollmentDetails

interface GetClassEnrollmentsQuery {
    suspend fun getEnrollments(
        tutorId: Long,
        classId: Long
    ): Result<List<ClassEnrollmentDetails>>
}