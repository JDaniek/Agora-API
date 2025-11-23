package backend.application.usecase.classes

import backend.domain.model.ClassEnrollmentForStudent
import backend.domain.port.inbound.GetStudentEnrolledClassesQuery
import backend.domain.port.outbound.ClassRepository

class GetStudentEnrolledClassesQueryImpl(
    private val classRepository: ClassRepository
) : GetStudentEnrolledClassesQuery {

    override suspend fun getForStudent(studentId: Long): Result<List<ClassEnrollmentForStudent>> =
        runCatching {
            classRepository.findClassesForStudent(studentId)
        }
}
