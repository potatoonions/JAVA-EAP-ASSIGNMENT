package com.crs.controller;

import com.crs.entity.StudentEntity;
import com.crs.exception.StudentNotFoundException;
import com.crs.logic.EligibilityChecker.EligibilityResult;
import com.crs.service.EnrollmentService;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/enrollment")
@RequiredArgsConstructor
@Validated
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    /** GET /enrollment/eligibility/{studentId}?semester=SEM1+2024/2025 */
    @GetMapping("/eligibility/{studentId}")
    public ResponseEntity<ApiResponse<EligibilityResult>> checkEligibility(
            @PathVariable String studentId,
            @RequestParam @NotBlank String semester) {
        try {
            EligibilityResult r = enrollmentService.confirmEligibility(studentId, semester);
            String msg = r.eligible()
                ? "Student is eligible for progression."
                : "Student is NOT eligible for progression.";
            return ApiResponse.ok(msg, r);
        } catch (StudentNotFoundException e) { return ApiResponse.notFound(e.getMessage()); }
          catch (IllegalArgumentException  e) { return ApiResponse.badRequest(e.getMessage()); }
    }

    /** POST /enrollment/{studentId}?semester=SEM1+2024/2025 */
    @PostMapping("/{studentId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> requestEnrolment(
            @PathVariable String studentId,
            @RequestParam @NotBlank String semester) {
        try {
            boolean enrolled = enrollmentService.allowRegistration(studentId, semester);
            if (enrolled) {
                StudentEntity s = enrollmentService.findStudent(studentId);
                return ApiResponse.created("Student successfully enrolled.",
                    Map.of("studentId", studentId,
                           "newLevel", s.getCurrentLevel(),
                           "newSemester", s.getCurrentSemester()));
            }
            return ApiResponse.conflict("Enrolment denied — student does not meet eligibility criteria.");
        } catch (StudentNotFoundException e) { return ApiResponse.notFound(e.getMessage()); }
          catch (IllegalArgumentException  e) { return ApiResponse.badRequest(e.getMessage()); }
    }

    /** GET /enrollment/ineligible */
    @GetMapping("/ineligible")
    public ResponseEntity<ApiResponse<List<String>>> getIneligibleStudents() {
        List<String> ids = enrollmentService.listIneligibleStudents()
            .stream().map(StudentEntity::getUserId).toList();
        return ApiResponse.ok(ids.size() + " ineligible student(s).", ids);
    }

    /** POST /enrollment/register */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Map<String, String>>> registerStudent(
            @RequestParam @NotBlank String userId,
            @RequestParam @NotBlank String firstName,
            @RequestParam @NotBlank String lastName,
            @RequestParam @Email String email,
            @RequestParam @NotBlank String program,
            @RequestParam @Min(100) @Max(900) int level,
            @RequestParam @NotBlank String semester) {
        try {
            StudentEntity s = enrollmentService.registerStudent(
                userId, firstName, lastName, email, program, level, semester);
            return ApiResponse.created("Student registered.",
                Map.of("userId", s.getUserId(), "email", s.getEmail()));
        } catch (IllegalArgumentException e) { return ApiResponse.badRequest(e.getMessage()); }
          catch (Exception e) { return ApiResponse.conflict(e.getMessage()); }
    }
}
