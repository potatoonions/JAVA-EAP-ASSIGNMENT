package com.crs.service;

import com.crs.exception.StudentNotFoundException;
import com.crs.logic.EligibilityChecker;
import com.crs.logic.EligibilityChecker.EligibilityResult;
import com.crs.entity.EnrollmentLogEntity;
import com.crs.entity.StudentEntity;
import com.crs.entity.UserEntity;
import com.crs.repository.EnrollmentLogRepository;
import com.crs.repository.StudentRepository;
import com.crs.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class EnrollmentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final EnrollmentLogRepository logRepository;
    private final EligibilityChecker eligibilityChecker;

    // Read operations
    public StudentEntity findStudent(String studentId) {
        return studentRepository.findById(studentId.toUpperCase())
            .orElseThrow(() -> new StudentNotFoundException(studentId));
    }


    public EligibilityResult confirmEligibility(String studentId, String semester) {
        StudentEntity student = findStudent(studentId);
        return eligibilityChecker.verifyEligibility(student, semester);
    }

    public List<StudentEntity> listIneligibleStudents() {
        return logRepository.findIneligibleStudents();
    }

    // Write operations
    @Transactional
    public boolean allowRegistration(String studentId, String semester) {
        StudentEntity  student = findStudent(studentId);
        EligibilityResult result = eligibilityChecker.verifyEligibility(student, semester);
        EnrollmentLogEntity logEntry = new EnrollmentLogEntity();
        logEntry.setStudent(student);
        logEntry.setCgpaAtCheck(BigDecimal.valueOf(result.cgpa())
            .setScale(4, RoundingMode.HALF_UP));
        logEntry.setFailedCourses(result.failedCourseCount());

        if (result.eligible()) {
            advanceStudentLevel(student);
            student.setRecoveryStatus(StudentEntity.RecoveryStatus.COMPLETED);
            studentRepository.save(student);

            logEntry.setDecision(EnrollmentLogEntity.Decision.ENROLLED);
            logEntry.setNewLevel(student.getCurrentLevel());
            logEntry.setNotes("Auto-enrolled after eligibility check.");

            log.info("Student {} enrolled — new level {}, semester {}",
                studentId, student.getCurrentLevel(), student.getCurrentSemester());
        } else {
            student.setRecoveryStatus(StudentEntity.RecoveryStatus.IN_PROGRESS);
            studentRepository.save(student);

            logEntry.setDecision(EnrollmentLogEntity.Decision.INELIGIBLE);
            logEntry.setNotes(result.message());

            log.warn("Student {} ineligible — {}", studentId, result.message());
        }

        logRepository.save(logEntry);
        return result.eligible();
    }

    @Transactional
    public StudentEntity registerStudent(String userId, String firstName, String lastName, String email, String program, int level, String semester) {
        if (userRepository.existsByEmailIgnoreCase(email))
            throw new IllegalArgumentException(
                "Email address is already registered: " + email);

        UserEntity user = new UserEntity(
            userId.toUpperCase(), firstName, lastName,
            email.toLowerCase(), UserEntity.Role.STUDENT);
        user.setAccountStatus(UserEntity.AccountStatus.ACTIVE);
        userRepository.save(user);

        StudentEntity student = new StudentEntity();
        student.setUserId(user.getUserId());
        student.setUser(user);
        student.setProgram(program);
        student.setCurrentLevel(level);
        student.setCurrentSemester(semester);
        student = studentRepository.save(student);

        log.info("Registered new student: {}", student.getUserId());
        return student;
    }

    private void advanceStudentLevel(StudentEntity student) {
        String sem = student.getCurrentSemester();
        if (sem.startsWith("SEM2")) {
            student.setCurrentLevel(student.getCurrentLevel() + 100);
            student.setCurrentSemester(sem.replace("SEM2", "SEM1"));
        } else {
            student.setCurrentSemester(sem.replace("SEM1", "SEM2"));
        }
    }
}
