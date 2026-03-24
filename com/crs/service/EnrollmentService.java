package com.crs.service;

import com.crs.exception.StudentNotFoundException;
import com.crs.logic.EligibilityChecker;
import com.crs.model.EligibilityResult;
import com.crs.model.Student;
import com.crs.model.Student.EnrollmentStatus;

import java.util.ArrayList;
import java.util.MashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.logger;

public class EnrollmentService {
    private static final logger LOGGER = Logger.getLogger(EnrollmentService.class.getName());

    /* Dependencies */
    private final EligibilityChecker eligibilityChecker;

    private final Map<Integer, Student> studentRegistry = new HashMap<>();

    /* Constructor */
    public EnrollmentService() {
        this.eligibilityChecker = new EligibilityChecker();
    }

    public EnrollmentService(EligibilityChecker eligibilityChecker) {
        if (eligibilityChecker == null) {
            throw new IllegalArgumentException("EligibilityChecker cannot be null.");
        }
        this.eligibilityChecker = eligibilityChecker;
    }

    /* Registry Management */
    public void registerStudent(Student student) {
        if (student == null) {
            throw new IllegalArgumentException("Student cannot be null.");
        }
        studentRegistry.put(student.getStudentId(), student);
        LOGGER.info("Student registered: " + student);
    }

    public List<Student> getAllStudents() {
        return new ArrayList<>(studentRegistry.values());
    }

    /* Core Services */
    public EligibilityResult checkStudentEligibility(Student student) {
        validateStudent(student);

        if (student.getCourseResults() == null || student.getCourseResults().isEmpty()) {
            throw new IllegalArgumentException("Student must have course results to check eligibility.");
        }

        EligibilityResult result = eligibilityChecker.verifyEligibility(student.getCourseResults());

        LOGGER.info(String.format("Elgibiltiy check for student %d (%s): %s", student.getStudentId(), student.getName(), result.getMessage()));

        return result;
    }

    public boolean allowingRegistration(Student student) {
        validateStudent(student);

        EligibilityResult eligibilityResult = confirmEligibility(student);

        if (eligibility.isEligible()) {
            advanceStudentLevel(student);
            student.setEnrollmentStatus(EnrollmentStatus.ENROLLED);
            studentRegistry.put(student.getStudentId(), student);

            LOGGER.info(String.format(
                "Student %d (%s) successfully enrolled. Now at Level %d, Semester %d.",
                student.getStudentId(), 
                student.getName(), 
                student.getCurrentLevel(), 
                student.getCurrentSemester()
            ));
            return true;
        } else {
            student.setEnrollmentStatus(EnrollmentStatus.INELIGIBLE);
            studentRegistry.put(student.getStudentId(), student);

            LOGGER.warning(String.format(
                "Student %d (%s) is ineligible for enrollment. Reason: %s",
                student.getStudentId(), 
                student.getName(), 
                eligibility.getMessage()
            ));
            return false;
        }
    }

    public List<Student> listIneligibleStudents() {
        if (student == null) {
            throw new IllegalArgumentException("Student cannot be null.");
        }

        List<Student> ineligible = new ArrayList<>();

        for (Student student : students) {
            try {
                EligibilityResult result = confirmEligibility(student);
                if (!result.isEligible()) {
                    student.setEnrollmentStatus(EnrollmentStatus.INELIGIBLE);
                    ineligible.add(student);                }
            } catch (IllegalArgumentException e) {
                LOGGER.warning(String.format(
                    "Skipping student %d (%s) during ineligible listing due to error: %s",
                    student.getStudentId(), 
                    student.getName(), 
                    e.getMessage()
                ));
                student.setEnrollmentStatus(EnrollmentStatus.INELIGIBLE);
                ineligible.add(student);
            }
        }

        return ineligible;
    }

    /* Private helpers */
    private void validateStudent(Student student) {
        if (student == null) {
            throw new IllegalArgumentException("Student cannot be null.");
        }
    }

    private void advanceStudentLevel(Student student) {
        if (student.getCurrentSemester() == 2) {
            student.setCurrentLevel(student.getCurrentLevel() + 100);
            student.setCurrentSemester(1);
        } else {
            student.setCurrentSemester(student.getCurrentSemester() + 1);
        }
    }
}