package com.crs.repository;

import com.crs.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link UserEntity}.
 *
 * Spring Boot auto-generates the implementation at startup — no
 * manual SQL or boilerplate required for standard CRUD operations.
 */
@Repository
public interface UserRepository extends JpaRepository<UserEntity, String> {

    /** Find a user by email address (case-insensitive). */
    Optional<UserEntity> findByEmailIgnoreCase(String email);

    /** Check whether an email is already registered. */
    boolean existsByEmailIgnoreCase(String email);

    /** List all users with a specific role. */
    List<UserEntity> findByRole(UserEntity.Role role);

    /** List all users with a specific account status. */
    List<UserEntity> findByAccountStatus(UserEntity.AccountStatus status);
}

// ════════════════════════════════════════════════════════════
//  StudentRepository
// ════════════════════════════════════════════════════════════

/**
 * Spring Data JPA repository for {@link StudentEntity}.
 */
@Repository
public interface StudentRepository extends JpaRepository<StudentEntity, String> {

    /** All students currently in a recovery programme. */
    List<StudentEntity> findByRecoveryStatusIn(
        List<StudentEntity.RecoveryStatus> statuses);

    /** Students at a particular level. */
    List<StudentEntity> findByCurrentLevel(int level);

    /** Students enrolled in a given semester. */
    List<StudentEntity> findByCurrentSemester(String semester);

    /**
     * Students whose CGPA is below a threshold — used to flag
     * candidates for recovery.
     */
    @Query("SELECT s FROM StudentEntity s WHERE s.cgpa < :threshold")
    List<StudentEntity> findByCgpaBelow(@Param("threshold") double threshold);

    /**
     * Students who have more than {@code maxFailed} failed courses
     * in a given semester (grade_point < passingThreshold).
     */
    @Query("""
        SELECT s FROM StudentEntity s
        WHERE (
            SELECT COUNT(cr) FROM CourseResultEntity cr
            WHERE cr.student = s
              AND cr.semester = :semester
              AND cr.gradePoint < :passingGp
        ) > :maxFailed
        """)
    List<StudentEntity> findWithTooManyFailures(
        @Param("semester")    String semester,
        @Param("passingGp")   double passingGp,
        @Param("maxFailed")   long   maxFailed);
}

// ════════════════════════════════════════════════════════════
//  CourseResultRepository
// ════════════════════════════════════════════════════════════

/**
 * Spring Data JPA repository for {@link CourseResultEntity}.
 */
@Repository
public interface CourseResultRepository
        extends JpaRepository<CourseResultEntity, Long> {

    /** All results for a student in a specific semester. */
    List<CourseResultEntity> findByStudentUserIdAndSemester(
        String studentId, String semester);

    /** All results for a student in a specific academic year. */
    List<CourseResultEntity> findByStudentUserIdAndAcademicYear(
        String studentId, int academicYear);

    /** All results for a student (full history). */
    List<CourseResultEntity> findByStudentUserId(String studentId);

    /** Count failed courses for a student in a semester. */
    @Query("""
        SELECT COUNT(cr) FROM CourseResultEntity cr
        WHERE cr.student.userId = :studentId
          AND cr.semester        = :semester
          AND cr.gradePoint      < :passingGp
        """)
    long countFailedCourses(
        @Param("studentId") String studentId,
        @Param("semester")  String semester,
        @Param("passingGp") double passingGp);

    /**
     * Compute CGPA inline: Σ(gradePoint × creditHours) / Σ(creditHours)
     * for a student in a specific semester.
     */
    @Query("""
        SELECT SUM(cr.gradePoint * cr.creditHours) / SUM(cr.creditHours)
        FROM CourseResultEntity cr
        WHERE cr.student.userId = :studentId
          AND cr.semester        = :semester
        """)
    Optional<Double> computeCgpaForSemester(
        @Param("studentId") String studentId,
        @Param("semester")  String semester);

    /**
     * Compute CGPA for a student across a full academic year.
     */
    @Query("""
        SELECT SUM(cr.gradePoint * cr.creditHours) / SUM(cr.creditHours)
        FROM CourseResultEntity cr
        WHERE cr.student.userId = :studentId
          AND cr.academicYear    = :year
        """)
    Optional<Double> computeCgpaForYear(
        @Param("studentId") String studentId,
        @Param("year")      int    year);

    /** Check whether a specific course has already been recorded. */
    boolean existsByStudentUserIdAndCourseCodeAndSemester(
        String studentId, String courseCode, String semester);
}

// ════════════════════════════════════════════════════════════
//  AcademicReportRepository
// ════════════════════════════════════════════════════════════

/**
 * Spring Data JPA repository for {@link AcademicReportEntity}.
 */
@Repository
public interface AcademicReportRepository
        extends JpaRepository<AcademicReportEntity, Long> {

    /** All reports for a student. */
    List<AcademicReportEntity> findByStudentUserIdOrderByGeneratedAtDesc(
        String studentId);

    /** Latest report for a student in a specific period. */
    Optional<AcademicReportEntity> findTopByStudentUserIdAndPeriodOrderByGeneratedAtDesc(
        String studentId, String period);

    /** All reports of a given type, newest first. */
    List<AcademicReportEntity> findByReportTypeOrderByGeneratedAtDesc(
        AcademicReportEntity.ReportType type);
}

// ════════════════════════════════════════════════════════════
//  EnrollmentLogRepository
// ════════════════════════════════════════════════════════════

/**
 * Spring Data JPA repository for {@link EnrollmentLogEntity}.
 */
@Repository
public interface EnrollmentLogRepository
        extends JpaRepository<EnrollmentLogEntity, Long> {

    /** Full history for a student. */
    List<EnrollmentLogEntity> findByStudentUserIdOrderByDecidedAtDesc(
        String studentId);

    /** Latest decision for a student. */
    Optional<EnrollmentLogEntity> findTopByStudentUserIdOrderByDecidedAtDesc(
        String studentId);

    /** All log entries with a specific decision. */
    List<EnrollmentLogEntity> findByDecision(
        EnrollmentLogEntity.Decision decision);

    /** Students who were declared ineligible. */
    @Query("""
        SELECT DISTINCT el.student FROM EnrollmentLogEntity el
        WHERE el.decision = 'INELIGIBLE'
        ORDER BY el.decidedAt DESC
        """)
    List<StudentEntity> findIneligibleStudents();
}

// ════════════════════════════════════════════════════════════
//  EmailRecordRepository
// ════════════════════════════════════════════════════════════

/**
 * Spring Data JPA repository for {@link EmailRecordEntity}.
 */
@Repository
public interface EmailRecordRepository
        extends JpaRepository<EmailRecordEntity, String> {

    /** Full audit log for a recipient. */
    List<EmailRecordEntity> findByRecipientEmailIgnoreCaseOrderBySentAtDesc(
        String email);

    /** All records of a specific type. */
    List<EmailRecordEntity> findByEmailTypeOrderBySentAtDesc(
        EmailRecordEntity.EmailType type);

    /** All records with a given delivery status. */
    List<EmailRecordEntity> findByDeliveryStatus(
        EmailRecordEntity.DeliveryStatus status);

    /**
     * Duplicate-suppression check: was a notification of this type sent
     * to this address since {@code since}?
     */
    boolean existsByRecipientEmailIgnoreCaseAndEmailTypeAndSentAtAfter(
        String email, EmailRecordEntity.EmailType type, LocalDateTime since);

    /** Count sent emails by type within a time window. */
    @Query("""
        SELECT COUNT(r) FROM EmailRecordEntity r
        WHERE r.recipientEmail = :email
          AND r.emailType       = :type
          AND r.sentAt         >= :since
          AND r.deliveryStatus  = 'SENT'
        """)
    long countRecentByTypeAndRecipient(
        @Param("email") String email,
        @Param("type")  EmailRecordEntity.EmailType type,
        @Param("since") LocalDateTime since);
}
