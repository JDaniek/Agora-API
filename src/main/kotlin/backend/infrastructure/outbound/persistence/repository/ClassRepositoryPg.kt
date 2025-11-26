package backend.infrastructure.outbound.persistence.repository

import backend.domain.model.ClassSession
import backend.domain.port.outbound.ClassRepository
import backend.infrastructure.outbound.persistence.tables.ClassesTable
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.LocalDate
import backend.domain.model.ClassEnrollment
import backend.domain.model.ClassEnrollmentDetails
import backend.infrastructure.outbound.persistence.tables.ClassEnrollmentsTable
import backend.infrastructure.outbound.persistence.tables.UserAccountsTable
import backend.domain.model.ClassEnrollmentForStudent
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.Instant

class ClassRepositoryPg : ClassRepository {

    private suspend fun <T> tx(block: suspend () -> T): T = newSuspendedTransaction(Dispatchers.IO) { block() }

    private fun ResultRow.toClassSession(): ClassSession = ClassSession(
        id = this[ClassesTable.id],
        tutorId = this[ClassesTable.tutorId],
        specialtyId = this[ClassesTable.specialtyId],
        title = this[ClassesTable.title],
        description = this[ClassesTable.description],
        classDate = this[ClassesTable.classDate],
        capacityPerSlot = this[ClassesTable.capacityPerSlot].toInt(),
        isActive = this[ClassesTable.isActive]
        // createdAt lo omitimos por ahora
    )

    override suspend fun create(
        tutorId: Long, title: String, description: String?, classDate: LocalDate, capacityPerSlot: Int, specialtyId: Int
    ): ClassSession = tx {
        val newId = ClassesTable.insert {
            it[this.tutorId] = tutorId
            it[this.title] = title
            it[this.description] = description
            it[this.classDate] = classDate
            it[this.capacityPerSlot] = capacityPerSlot.toShort()
            it[this.specialtyId] = specialtyId
            // isActive y created_at usan defaults de la BD
        } get ClassesTable.id

        ClassesTable.selectAll().where { ClassesTable.id eq newId }.single().toClassSession()
    }

    override suspend fun findById(id: Long): ClassSession? = tx {
        ClassesTable.selectAll().where { ClassesTable.id eq id }.firstOrNull()?.toClassSession()
    }

    override suspend fun findByTutor(tutorId: Long): List<ClassSession> = tx {
        ClassesTable.selectAll().where { ClassesTable.tutorId eq tutorId }
            .orderBy(ClassesTable.classDate to SortOrder.ASC).map { it.toClassSession() }
    }

    /**
     * UPDATE parcial:
     * - Solo actualiza campos no nulos
     * - Verifica que la clase pertenezca al tutor
     */
    override suspend fun update(
        classId: Long,
        tutorId: Long,
        title: String?,
        description: String?,
        classDate: LocalDate?,
        capacityPerSlot: Int?,
        specialtyId: Int?,
        isActive: Boolean?
    ): ClassSession? = tx {
        val updatedRows = ClassesTable.update(
            where = {
                (ClassesTable.id eq classId) and (ClassesTable.tutorId eq tutorId)
            }) {
            if (title != null) {
                it[ClassesTable.title] = title
            }
            if (description != null) {
                it[ClassesTable.description] = description
            }
            if (classDate != null) {
                it[ClassesTable.classDate] = classDate
            }
            if (capacityPerSlot != null) {
                it[ClassesTable.capacityPerSlot] = capacityPerSlot.toShort()
            }
            if (specialtyId != null) {
                it[ClassesTable.specialtyId] = specialtyId
            }
            if (isActive != null) {
                it[ClassesTable.isActive] = isActive
            }
        }

        if (updatedRows == 0) {
            null
        } else {
            ClassesTable.selectAll().where { ClassesTable.id eq classId }.single().toClassSession()
        }
    }

    private fun ResultRow.toClassEnrollment(): ClassEnrollment = ClassEnrollment(
        userId = this[ClassEnrollmentsTable.userId],
        classId = this[ClassEnrollmentsTable.classId],
        studentId = this[ClassEnrollmentsTable.studentId],
        status = this[ClassEnrollmentsTable.status],
        createdAt = this[ClassEnrollmentsTable.createdAt].toInstant()
    )
    // ===============================
    //   INSCRIPCIONES A CLASES
    // ===============================

    override suspend fun addEnrollment(
        tutorId: Long, classId: Long, studentId: Long, status: String
    ): Unit = tx {
        ClassEnrollmentsTable.insert {
            it[userId] = tutorId
            it[ClassEnrollmentsTable.classId] = classId
            it[ClassEnrollmentsTable.studentId] = studentId
            it[ClassEnrollmentsTable.status] = status
            // created_at se llena por default
        }
    }

    override suspend fun isStudentEnrolledInClass(
        classId: Long, studentId: Long
    ): Boolean = tx {
        ClassEnrollmentsTable.selectAll().where {
            (ClassEnrollmentsTable.classId eq classId) and (ClassEnrollmentsTable.studentId eq studentId) and (ClassEnrollmentsTable.status neq "canceled")
        }.count() > 0
    }

    override suspend fun countConfirmedEnrollments(classId: Long): Int = tx {
        ClassEnrollmentsTable.selectAll().where {
            (ClassEnrollmentsTable.classId eq classId) and (ClassEnrollmentsTable.status eq "confirmed")
        }.count().toInt() //Conversion explicita de Long a Int
    }

    override suspend fun findEnrollmentsForClass(classId: Long): List<ClassEnrollment> = tx {
        ClassEnrollmentsTable.selectAll().where { ClassEnrollmentsTable.classId eq classId }
            .orderBy(ClassEnrollmentsTable.createdAt to SortOrder.ASC).map { it.toClassEnrollment() }
    }

    // 👇 NUEVO: join con UserAccountsTable para sacar nombre y correo
    override suspend fun findEnrollmentDetailsForClass(
        classId: Long
    ): List<ClassEnrollmentDetails> = tx {
        ClassEnrollmentsTable
            .join(
                UserAccountsTable,
                JoinType.INNER,
                onColumn = ClassEnrollmentsTable.studentId,  //unir por student_id
                otherColumn = UserAccountsTable.id
            )
            .selectAll()
            .where { ClassEnrollmentsTable.classId eq classId }
            .orderBy(ClassEnrollmentsTable.createdAt to SortOrder.ASC)
            .map { row ->
                ClassEnrollmentDetails(
                    studentId = row[ClassEnrollmentsTable.studentId],
                    firstName = row[UserAccountsTable.firstName],
                    lastName = row[UserAccountsTable.lastName],
                    email = row[UserAccountsTable.email],
                    status = row[ClassEnrollmentsTable.status],
                    enrolledAt = row[ClassEnrollmentsTable.createdAt].toInstant()
                )
            }
    }

    //Nuevo: Para mostrar a los alumnos sus clases
    private fun ResultRow.toClassEnrollmentForStudent(): ClassEnrollmentForStudent =
        ClassEnrollmentForStudent(
            classId = this[ClassesTable.id],
            title = this[ClassesTable.title],
            description = this[ClassesTable.description],
            classDate = this[ClassesTable.classDate],
            status = this[ClassEnrollmentsTable.status],
            tutorId = this[ClassesTable.tutorId],
            specialtyId = this[ClassesTable.specialtyId]
        )

    override suspend fun findClassesForStudent(
        studentId: Long
    ): List<ClassEnrollmentForStudent> = tx {
        ClassEnrollmentsTable
            .join(
                ClassesTable,
                JoinType.INNER,
                onColumn = ClassEnrollmentsTable.classId,
                otherColumn = ClassesTable.id
            )
            .select(
                ClassesTable.id,
                ClassesTable.title,
                ClassesTable.description,
                ClassesTable.classDate,
                ClassesTable.tutorId,
                ClassesTable.specialtyId,
                ClassEnrollmentsTable.status
            )
            .where { ClassEnrollmentsTable.studentId eq studentId }
            .orderBy(ClassesTable.classDate to SortOrder.ASC)
            .map { row -> row.toClassEnrollmentForStudent() }
    }

    override suspend fun deleteClassByIdAndTutor(
        classId: Long,
        tutorId: Long
    ): Boolean = tx {
        ClassesTable.deleteWhere {
            (ClassesTable.id eq classId) and (ClassesTable.tutorId eq tutorId)
        } > 0
    }

    /*Nuevo: Con este caso ya se verifica el planteamiento de si el alumno ya estuvo en al menos una clase confirmada
    con este asesor en una fecha anterior o igual a hoy
     */
    override suspend fun hasStudentCompletedClassWithTeacher(
        studentId: Long,
        teacherId: Long,
        untilDate: LocalDate
    ): Boolean = tx {
        ClassEnrollmentsTable
            .join(
                ClassesTable,
                JoinType.INNER,
                onColumn = ClassEnrollmentsTable.classId,
                otherColumn = ClassesTable.id
            )
            .selectAll()
            .where {
                (ClassEnrollmentsTable.studentId eq studentId) and
                        (ClassesTable.tutorId eq teacherId) and
                        (ClassesTable.classDate lessEq untilDate) and
                        (ClassEnrollmentsTable.status eq "confirmed")
            }
            .limit(1)
            .any()
    }


}
