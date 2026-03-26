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

@Repository
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
        @Param("semester") String semester,
        @Param("passingGp") double passingGp,
        @Param("maxFailed") long maxFailed);
}

//  CourseResultRepository

@Repository
public interface CourseResultRepository
        extends JpaRepository<CourseResultEntity, Long> {

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
        @Param("semester")  String semester);

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
public interface AcademicReportRepository
        extends JpaRepository<AcademicReportEntity, Long> {

    List<AcademicReportEntity> findByStudentUserIdOrderByGeneratedAtDesc(
        String studentId);

    Optional<AcademicReportEntity> findTopByStudentUserIdAndPeriodOrderByGeneratedAtDesc(
        String studentId, String period);

    List<AcademicReportEntity> findByReportTypeOrderByGeneratedAtDesc(
        AcademicReportEntity.ReportType type);
}
