package backend.domain.port.outbound

import backend.domain.model.ClassEnrollment
import backend.domain.model.ClassSession
import backend.domain.model.ClassEnrollmentDetails
import backend.domain.model.ClassEnrollmentForStudent
import java.time.LocalDate

interface ClassRepository {

    suspend fun create(
        tutorId: Long, title: String, description: String?, classDate: LocalDate, capacityPerSlot: Int, specialtyId: Int
    ): ClassSession

    suspend fun findById(id: Long): ClassSession?

    suspend fun findByTutor(tutorId: Long): List<ClassSession>
    // === NUEVOS MÉTODOS PARA INSCRIPCIONES ===

    /**
     * Inserta una inscripción en class_enrollments.
     */
    suspend fun addEnrollment(
        tutorId: Long, classId: Long, studentId: Long, status: String = "confirmed"
    )

    /**
     * Verifica si un alumno ya está inscrito (no cancelado) en una clase.
     */
    suspend fun isStudentEnrolledInClass(
        classId: Long, studentId: Long
    ): Boolean

    /**
     * Número de inscripciones confirmadas para una clase.
     */
    suspend fun countConfirmedEnrollments(
        classId: Long
    ): Int

    /**
     * Lista de inscripciones de una clase (por si luego quieres mostrarlo).
     */
    suspend fun findEnrollmentsForClass(
        classId: Long
    ): List<ClassEnrollment>

    /**
     * Actualiza parcialmente una clase.
     * Solo se modifican los campos no nulos.
     *
     * @return La clase actualizada o null si no existe o no pertenece al tutor.
     */
    suspend fun update(
        classId: Long,
        tutorId: Long,
        title: String? = null,
        description: String? = null,
        classDate: LocalDate? = null,
        capacityPerSlot: Int? = null,
        specialtyId: Int? = null,
        isActive: Boolean? = null
    ): ClassSession?

    // NUEVO: read model con datos del alumno
    suspend fun findEnrollmentDetailsForClass(classId: Long): List<ClassEnrollmentDetails>

    //Nuevo: Clases donde esta inscrito el alumno
    suspend fun findClassesForStudent(studentId: Long): List<ClassEnrollmentForStudent>

    //Nuevo: Eliminar clase (con verificacion de tutor)
    suspend fun deleteClassByIdAndTutor(classId: Long, tutorId: Long): Boolean

    //Nuevo: En esste metodo metemos la "tarea" para revisar si ya paso el dia de la clase y si el alumno estuvo inscrito en una clase confirmada con ese asesor
    suspend fun hasStudentCompletedClassWithTeacher(
        studentId: Long,
        teacherId: Long,
        untilDate: LocalDate
    ): Boolean



}
