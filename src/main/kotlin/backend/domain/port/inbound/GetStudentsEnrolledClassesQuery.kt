package backend.domain.port.inbound

import backend.domain.model.ClassEnrollmentForStudent

interface GetStudentEnrolledClassesQuery {
    suspend fun getForStudent(studentId: Long): Result<List<ClassEnrollmentForStudent>>
}
