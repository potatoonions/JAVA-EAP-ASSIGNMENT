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

// UserRepository
@Repository
public interface UserRepository extends JpaRepository<UserEntity, String> {

    Optional<UserEntity> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    List<UserEntity> findByRole(UserEntity.Role role);

    List<UserEntity> findByAccountStatus(UserEntity.AccountStatus status);
}

//  StudentRepository
public interface StudentRepository extends JpaRepository<StudentEntity, String> {

    List<StudentEntity> findByRecoveryStatusIn(
        List<StudentEntity.RecoveryStatus> statuses);

    List<StudentEntity> findByCurrentLevel(int level);

    List<StudentEntity> findByCurrentSemester(String semester);

    @Query("SELECT s FROM StudentEntity s WHERE s.cgpa < :threshold")
    List<StudentEntity> findByCgpaBelow(@Param("threshold") double threshold);

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

//  CourseResultRepository
@Repository
public interface CourseResultRepository extends JpaRepository<CourseResultEntity, Long> {

    List<CourseResultEntity> findByStudentUserIdAndSemester(
        String studentId, String semester);

    List<CourseResultEntity> findByStudentUserIdAndAcademicYear(
        String studentId, int academicYear);

    List<CourseResultEntity> findByStudentUserId(String studentId);

    @Query("""
        SELECT COUNT(cr) FROM CourseResultEntity cr
        WHERE cr.student.userId = :studentId
          AND cr.semester        = :semester
          AND cr.gradePoint      < :passingGp
        """)
    long countFailedCourses(
        @Param("studentId") String studentId,
        @Param("semester") String semester,
        @Param("passingGp") double passingGp);

    @Query("""
        SELECT SUM(cr.gradePoint * cr.creditHours) / SUM(cr.creditHours)
        FROM CourseResultEntity cr
        WHERE cr.student.userId = :studentId
          AND cr.semester        = :semester
        """)
    Optional<Double> computeCgpaForSemester(
        @Param("studentId") String studentId,
        @Param("semester") String semester);

    @Query("""
        SELECT SUM(cr.gradePoint * cr.creditHours) / SUM(cr.creditHours)
        FROM CourseResultEntity cr
        WHERE cr.student.userId = :studentId
          AND cr.academicYear    = :year
        """)
    Optional<Double> computeCgpaForYear(
        @Param("studentId") String studentId,
        @Param("year")      int    year);

    boolean existsByStudentUserIdAndCourseCodeAndSemester(
        String studentId, String courseCode, String semester);
}

//  AcademicReportRepository
@Repository
public interface AcademicReportRepository extends JpaRepository<AcademicReportEntity, Long> {

    List<AcademicReportEntity> findByStudentUserIdOrderByGeneratedAtDesc(
        String studentId);

    Optional<AcademicReportEntity> findTopByStudentUserIdAndPeriodOrderByGeneratedAtDesc(
        String studentId, String period);

    List<AcademicReportEntity> findByReportTypeOrderByGeneratedAtDesc(
        AcademicReportEntity.ReportType type);
}

//  EnrollmentLogRepository
@Repository
public interface EnrollmentLogRepository extends JpaRepository<EnrollmentLogEntity, Long> {

    List<EnrollmentLogEntity> findByStudentUserIdOrderByDecidedAtDesc(
        String studentId);

    Optional<EnrollmentLogEntity> findTopByStudentUserIdOrderByDecidedAtDesc(
        String studentId);

    List<EnrollmentLogEntity> findByDecision(
        EnrollmentLogEntity.Decision decision);

    @Query("""
        SELECT DISTINCT el.student FROM EnrollmentLogEntity el
        WHERE el.decision = 'INELIGIBLE'
        ORDER BY el.decidedAt DESC
        """)
    List<StudentEntity> findIneligibleStudents();
}

//  EmailRecordRepository
@Repository
public interface EmailRecordRepository extends JpaRepository<EmailRecordEntity, String> {

    List<EmailRecordEntity> findByRecipientEmailIgnoreCaseOrderBySentAtDesc(
        String email);

    List<EmailRecordEntity> findByEmailTypeOrderBySentAtDesc(
        EmailRecordEntity.EmailType type);

    List<EmailRecordEntity> findByDeliveryStatus(
        EmailRecordEntity.DeliveryStatus status);

    boolean existsByRecipientEmailIgnoreCaseAndEmailTypeAndSentAtAfter(
        String email, EmailRecordEntity.EmailType type, LocalDateTime since);

    @Query("""
        SELECT COUNT(r) FROM EmailRecordEntity r
        WHERE r.recipientEmail = :email
          AND r.emailType       = :type
          AND r.sentAt         >= :since
          AND r.deliveryStatus  = 'SENT'
        """)
    long countRecentByTypeAndRecipient(
        @Param("email") String email,
        @Param("type") EmailRecordEntity.EmailType type,
        @Param("since") LocalDateTime since);
}

//  ScheduledNotificationRepository
@Repository
public interface ScheduledNotificationRepository extends JpaRepository<ScheduledNotificationEntity, String> {

    @Query("""
        SELECT sn FROM ScheduledNotificationEntity sn
        WHERE sn.status       = 'PENDING'
          AND sn.scheduledFor <= :now
        """)
    List<ScheduledNotificationEntity> findDueNotifications(
        @Param("now") LocalDateTime now);

    List<ScheduledNotificationEntity> findByRecipientEmailIgnoreCaseAndStatus(
        String email, ScheduledNotificationEntity.Status status);

    @Query("""
        UPDATE ScheduledNotificationEntity sn
        SET sn.status = 'CANCELLED'
        WHERE sn.recipientEmail = :email
          AND sn.status         = 'PENDING'
        """)
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    int cancelAllPendingForRecipient(@Param("email") String email);
}
