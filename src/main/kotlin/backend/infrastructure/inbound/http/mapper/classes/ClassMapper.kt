package backend.infrastructure.inbound.http.mapper.classes

import backend.domain.model.ClassSession
import backend.domain.model.ClassEnrollmentDetails
import backend.domain.model.ClassEnrollmentForStudent
import backend.infrastructure.inbound.http.dto.classes.ClassResponse
import backend.infrastructure.inbound.http.dto.classes.ClassEnrollmentResponse
import backend.infrastructure.inbound.http.dto.classes.StudentClassResponse
import java.time.format.DateTimeFormatter

private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

// 🔹 Para listar clases del tutor
fun ClassSession.toResponse(): ClassResponse =
    ClassResponse(
        id = id,
        title = title,
        description = description,
        classDate = dateFormatter.format(classDate),
        capacityPerSlot = capacityPerSlot,
        specialtyId = specialtyId,
        isActive = isActive
    )

// 🔹 Para listar alumnos de una clase (vista del asesor)
fun ClassEnrollmentDetails.toResponse(): ClassEnrollmentResponse =
    ClassEnrollmentResponse(
        studentId = studentId,
        fullName = "$firstName $lastName".trim(),
        email = email,
        photoUrl = photoUrl,

        status = status,
        enrolledAt = enrolledAt.toString() // Instant → ISO-8601
    )

// 🔹 Para listar clases en las que un ALUMNO está inscrito
fun ClassEnrollmentForStudent.toStudentResponse(): StudentClassResponse =
    StudentClassResponse(
        classId = classId,
        title = title,
        description = description,
        classDate = dateFormatter.format(classDate),
        status = status,
        tutorId = tutorId,
        specialtyId = specialtyId
    )