package com.crs;

import com.crs.controller.ApiResponse;
import com.crs.controller.EnrollmentController;
import com.crs.model.CourseResult;
import com.crs.model.EligibilityResult;
import com.crs.model.Student;
import com.crs.service.EnrollmentService;

import java.util.List;

public class CRSApplication {
    public static void main(String[] args) {
        EnrollmentService service = new EnrollmentService();
        EnrollmentController controller = new EnrollmentController(service);

        /* Eligible student */
        Student alice = new Student(1001, "Alice Smith", 200, 2);
        alice.addCourseResult(new CourseResult("CS201", 3, 3.7));
        alice.addCourseResult(new CourseResult("CS202", 3, 3.3));
        alice.addCourseResult(new CourseResult("MATH201", 3, 2.7));
        alice.addCourseResult(new CourseResult("ENG201", 3, 3.0));
        alice.addCourseResult(new CourseResult("PHY201", 3, 2.3));
        service.registerStudent(alice);

        /* Ineligible student - Low CGPA */
        Student bob = new Student(1002, "Bob Johnson", 150, 1);
        bob.addCourseResult(new CourseResult("CS201", 3, 1.7));
        bob.addCourseResult(new CourseResult("CS202", 3, 1.3));
        bob.addCourseResult(new CourseResult("MATH201", 3, 0.7));
        bob.addCourseResult(new CourseResult("ENG201", 3, 1.0));
        bob.addCourseResult(new CourseResult("PHY201", 3, 1.3));
        service.registerStudent(bob);

        /* Ineligible student - Failed Courses */
        Student charlie = new Student(1003, "Charlie Brown", 180, 2);
        charlie.addCourseResult(new CourseResult("CS201", 3, 0.0));
        charlie.addCourseResult(new CourseResult("CS202", 3, 0.0));
        charlie.addCourseResult(new CourseResult("MATH201", 3, 0.0));
        charlie.addCourseResult(new CourseResult("ENG201", 3, 0.0));
        charlie.addCourseResult(new CourseResult("PHY201", 3, 3.7));
        service.registerStudent(charlie);

        /* Check individual eligibility */
        printSectionHeader("Eligibility Checks");

        for (int id : new int[](1001, 1002, 1003)) {
            ApiResponse<EligibilityResult> res = controller.checkStudentEligibility(id);
            System.out.printf(" [%d] %s%n", res.getStatusCode(), res.getMessage());
            if (res.getData() != null) {
                EligibilityResult er = res.getData();
                System.out.printf("  CGPA: %.2f | Failed Courses: %d%n", er.getCgpa(), er.getFailedCourseCount());
            }
        }

        /* Request enrollment */
        printSectionHeader("Enrollment Requests");

        for (int id : new int[]{1001, 1002, 1003}) {
            ApiResponse<CourseResult> res = controller.requestEnrollment(id);
            System.out.printf(" [%d] %s%n", res.getStatusCode(), res.getMessage());
            if (res.getData() != null) {
                Student s = res.getData();
                System.out.printf("  New position (Level %d), Semester %d | Status: %s%n", s.getCurrentLevel(), s.getCurrentSemester(), s.getEnrollmentStatus());
            }
        }

        /* List all ineligible students */
        printSectionHeader("Ineligible Student List");

        ApiResponse<List<Student>> ineligibleRes = controller.getIneligibleStudents();
        System.out.println(" " + ineligibleRes.getMessage());
        if (ineligibleRes.getData() != null) {
            for (Student s : ineligibleRes.getData()) {
                System.out.printf("  [%d] %s | Status: %s%n", s.getId(), s.getName(), s.getEnrollmentStatus());
            }
        }

        /* Unknown Student ID */
        printSectionHeader("Unknown Student ID");
        ApiResponse<EligibilityResult> notFound = controller.checkStudentEligibility(9999);
        System.out.printf(" [%d] %s%n", notFound.getStatusCode(), notFound.getMessage());

        /* Invalid Student ID */
        printSectionHeader("Invalid Student ID");
        ApiResponse<Student> invalidId = controller.requestEnrollment(0);
        System.out.printf(" [%d] %s%n", invalidId.getStatusCode(), invalidId.getMessage());

    }

    private static void printSectionHeader(String title) {
        System.out.println("\n" + "=".repeat(50));
        System.out.println(" " + title);
        System.out.println("=".repeat(50));
    }
}