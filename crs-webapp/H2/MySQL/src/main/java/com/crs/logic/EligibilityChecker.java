package com.crs.logic;
 
import com.crs.entity.CourseResult;
import com.crs.entity.StudentEntity;
import com.crs.repository.CourseResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
 
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class EligibilityChecker {
 
    public static final double PASSING_GRADE_POINT = 1.0;
    public static final double MINIMUM_CGPA = 2.0;
    public static final int MAXIMUM_FAILED_COURSES = 3;
 
    private final CourseResultRepository courseResultRepository;

    public double calculateCGPA(String studentId, String semester) {
        return courseResultRepository
            .computeCgpaForSemester(studentId, semester)
            .orElse(0.0);
    }

    public int checkFailedCourseCount(String studentId, String semester) {
        return (int) courseResultRepository.countFailedCourses(
            studentId, semester, PASSING_GRADE_POINT);
    }

    public EligibilityResult verifyEligibility(StudentEntity student, String semester) {
        if (student == null)
            throw new IllegalArgumentException("Student must not be null.");
        if (semester == null || semester.isBlank())
            throw new IllegalArgumentException("Semester must not be blank.");
 
        String studentId = student.getUserId();
        double cgpa = calculateCGPA(studentId, semester);
        int failedCount = checkFailedCourseCount(studentId, semester);
        boolean cgpaMet = cgpa >= MINIMUM_CGPA;
        boolean failedMet = failedCount <= MAXIMUM_FAILED_COURSES;
        boolean eligible = cgpaMet && failedMet;
 
        String message = buildMessage(eligible, cgpaMet, failedMet, cgpa, failedCount);
 
        log.info("Eligibility — student={}, semester={}, cgpa={}, failed={}, eligible={}", studentId, semester, cgpa, failedCount, eligible);
 
        return new EligibilityResult(eligible, cgpa, failedCount, message);
    }

    private String buildMessage(boolean elig, boolean cgpaMet, boolean failedMet,
                                double cgpa, int failed) {
        if (elig) {
            return String.format(
                "Student is ELIGIBLE. CGPA: %.2f (required ≥ %.1f), " + "Failed courses: %d (max allowed: %d).", cgpa, MINIMUM_CGPA, failed, MAXIMUM_FAILED_COURSES);
        }
        StringBuilder sb = new StringBuilder("Student is INELIGIBLE. ");
        if (!cgpaMet)
            sb.append(String.format("CGPA %.2f is below the required %.1f. ", cgpa, MINIMUM_CGPA));
        if (!failedMet)
            sb.append(String.format("Failed courses %d exceeds the maximum of %d. ", failed, MAXIMUM_FAILED_COURSES));
        return sb.toString().trim();
    }

    public record EligibilityResult(
        boolean eligible,
        double cgpa,
        int failedCourseCount,
        String message
    ) {}
}