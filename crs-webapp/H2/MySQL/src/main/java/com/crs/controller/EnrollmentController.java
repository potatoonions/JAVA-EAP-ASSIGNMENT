package com.crs.controller;

import com.crs.exception.StudentNotFoundException;
import com.crs.model.EligibilityResult;
import com.crs.model.Student;
import com.crs.service.EnrollmentService;

import java.util.List;
import java.util.logging.Logger;

public class EnrollmentController {
    private static final Logger LOGGER = Logger.getLogger(EnrollmentController.class.getName());

    /* Dependencies */
    private final EnrollmentService enrollmentService;

    /* Constructor */
    public EnrollmentController(EnrollmentService enrollmentService) {
        if (enrollmentService == null) {
            throw new IllegalArgumentException("EnrollmentService cannot be null.");
        }
        this.enrollmentService = enrollmentService;
    }

    /* Endpoints */
    public ApiResponse<Student> requestEnrollment(int studentId) {
        LOGGER.info("Enrollment request received for student ID: " + studentId);

        if (studentId <= 0) {
            return ApiResponse.badRequest("Student ID must be positive.");
        }

        try {
            Student student = enrollmentService.findStudentById(studentId);
            boolean enrolled = enrollmentService.allowingRegistration(student);

            if (enrolled) {
                return ApiResponse.created(
                    "Student successfully enrolled or Level 1 " + student.getCurrentLevel()
                    + ", Semester " + student.getCurrentSemester() + ".",
                    student);
            } else {
                return ApiResponse.conflict(
                    "Enrollement denied. Student does not meet eligibility criteria.");
            }
        } catch (StudentNotFoundException e) {
            LOGGER.warning(e.getMessage());
            return ApiResponse.notFound(e.getMessage());
        } catch (IllegalArgumentException e) {
            LOGGER.warning("Validation error during enrollment: " + e.getMessage());
            return ApiResponse.badRequest(e.getMessage());
        } catch (Exception e) {
            LOGGER.severe("Unexpected error during enrollment: " + e.getMessage());
            return ApiResponse.internalServerError("An unexpected error occurred. Please try again later.");
        }
    }

    public ApiResponse<EligibilityResult> checkStudentEligibility(int studentID) {
        LOGGER.info("Eligibility check request received for student ID: " + studentID);

        if (studentID <= 0) {
            return ApiResponse.badRequest("Student ID must be positive.");
        }

        try {
            Student student = enrollmentService.findStudentById(studentID);
            EligibilityResult result = enrollmentService.confirmEligibility(student);

            String msg = result.isEligibile()
                ? "Student is eligible for enrollment."
                : "Student is not eligible for enrollment. ";

            return ApiResponse.ok(msg, result);

        } catch (StudentNotFoundException e) {
            LOGGER.warning(e.getMessage());
            return ApiResponse.notFound(e.getMessage());

        } catch (IllegalArgumentException e) {
            LOGGER.warning("Validation error during eligibility check: " + e.getMessage());
            return ApiResponse.badRequest(e.getMessage());

        } catch (Exception e) {
            LOGGER.severe("Unexpected error during eligibility check: " + e.getMessage());
            return ApiResponse.internalServerError("An unexpected error occurred. Please try again later.");
        }
    }

    public ApiResponse<List<Student>> listIneligibleStudents() {
        LOGGER.info("Request received to list ineligible students.");

        try {
            List<Student> allStudents = enrollmentService.listAllStudents();
            List<Student> ineligible = enrollmentService.listIneligibleStudents(allStudents);

            String msg = ineligible.isEmpty()
                ? "All students are eligible for enrollment."
                : String.format("%d ineligible students found.", ineligible.size());

            return ApiResponse.ok(msg, ineligible);

        } catch (IllegalArgumentException e) {
            LOGGER.warning("Unexpected error while listing ineligible students: " + e.getMessage());
            return ApiResponse.badRequest(e.getMessage());

        } catch (Exception e) {
            LOGGER.severe("Unexpected error while listing ineligible students: " + e.getMessage());
            return ApiResponse.internalServerError("An unexpected error occurred. Please try again later.");
        }
    }
}