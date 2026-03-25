package com.crs.controller;

import com.crs.exception.StudentNotFoundException;
import com.crs.logic.EligibilityChecker.EligibilityResult;
import com.crs.model.*;
import com.crs.service.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// Shared API envelope
record ApiResponse<T>(int status, String message, T data) {
    static <T> ResponseEntity<ApiResponse<T>> ok(String msg, T data) {
        return ResponseEntity.ok(new ApiResponse<>(200, msg, data));
    }
    static <T> ResponseEntity<ApiResponse<T>> created(String msg, T data) {
        return ResponseEntity.status(201).body(new ApiResponse<>(201, msg, data));
    }
    static <T> ResponseEntity<ApiResponse<T>> badRequest(String msg) {
        return ResponseEntity.badRequest().body(new ApiResponse<>(400, msg, null));
    }
    static <T> ResponseEntity<ApiResponse<T>> notFound(String msg) {
        return ResponseEntity.status(404).body(new ApiResponse<>(404, msg, null));
    }
    static <T> ResponseEntity<ApiResponse<T>> conflict(String msg) {
        return ResponseEntity.status(409).body(new ApiResponse<>(409, msg, null));
    }
}

// EnrollmentController – /enrollment

@RestController
@RequestMapping("/enrollment")
@RequiredArgsConstructor
@Validated
class EnrollmentController {

    private final EnrollmentService enrollmentService;

    /** GET /enrollment/eligibility/{studentId}?semester=SEM1+2024/2025 */
    @GetMapping("/eligibility/{studentId}")
    ResponseEntity<ApiResponse<EligibilityResult>> checkEligibility(
            @PathVariable String studentId,
            @RequestParam String semester) {
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
    ResponseEntity<ApiResponse<Map<String,Object>>> requestEnrollment(
            @PathVariable String studentId,
            @RequestParam String semester) {
        try {
            boolean enrolled = enrollmentService.allowRegistration(studentId, semester);
            if (enrolled) {
                StudentEntity s = enrollmentService.findStudent(studentId);
                return ApiResponse.created("Student successfully enrolled.",
                    Map.of("studentId", studentId,
                           "newLevel",   s.getCurrentLevel(),
                           "newSemester",s.getCurrentSemester()));
            }
            return ApiResponse.conflict("Enrolment denied — student does not meet eligibility criteria.");
        } catch (StudentNotFoundException e) { return ApiResponse.notFound(e.getMessage()); }
          catch (IllegalArgumentException  e) { return ApiResponse.badRequest(e.getMessage()); }
    }

    /** GET /enrollment/ineligible */
    @GetMapping("/ineligible")
    ResponseEntity<ApiResponse<List<String>>> getIneligibleStudents() {
        List<String> ids = enrollmentService.listIneligibleStudents()
            .stream().map(StudentEntity::getUserId).toList();
        return ApiResponse.ok(ids.size() + " ineligible student(s).", ids);
    }

    /** POST /enrollment/register */
    @PostMapping("/register")
    ResponseEntity<ApiResponse<Map<String,String>>> registerStudent(
            @RequestParam String userId,
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam @Email String email,
            @RequestParam String program,
            @RequestParam @Min(100) @Max(900) int level,
            @RequestParam String semester) {
        try {
            StudentEntity s = enrollmentService.registerStudent(
                userId, firstName, lastName, email, program, level, semester);
            return ApiResponse.created("Student registered.",
                Map.of("userId", s.getUserId(), "email", s.getEmail()));
        } catch (IllegalArgumentException e) { return ApiResponse.badRequest(e.getMessage()); }
    }
}

//  ReportController  –  /reports
@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@Validated
class ReportController {

    private final ReportService reportService;

    /** GET /reports/semester?studentId=S001&semester=SEM1+2024/2025 */
    @GetMapping("/semester")
    ResponseEntity<ApiResponse<AcademicReportEntity>> semesterReport(
            @RequestParam String studentId,
            @RequestParam String semester) {
        try {
            AcademicReportEntity r = reportService.generateSemesterReport(studentId, semester);
            return ApiResponse.ok(
                "Semester report generated — CGPA: " + r.getCgpa(), r);
        } catch (StudentNotFoundException e) { return ApiResponse.notFound(e.getMessage()); }
          catch (IllegalArgumentException  e) { return ApiResponse.badRequest(e.getMessage()); }
    }

    /** GET /reports/yearly?studentId=S001&year=2024 */
    @GetMapping("/yearly")
    ResponseEntity<ApiResponse<AcademicReportEntity>> yearlyReport(
            @RequestParam String studentId,
            @RequestParam @Min(2000) int year) {
        try {
            AcademicReportEntity r = reportService.generateYearlyReport(studentId, year);
            return ApiResponse.ok(
                "Yearly report generated — CGPA: " + r.getCgpa(), r);
        } catch (StudentNotFoundException e) { return ApiResponse.notFound(e.getMessage()); }
          catch (IllegalArgumentException  e) { return ApiResponse.badRequest(e.getMessage()); }
    }

    /** GET /reports/student/{studentId} */
    @GetMapping("/student/{studentId}")
    ResponseEntity<ApiResponse<List<AcademicReportEntity>>> listReports(
            @PathVariable String studentId) {
        List<AcademicReportEntity> reports = reportService.getReportsForStudent(studentId);
        return ApiResponse.ok(reports.size() + " report(s) found.", reports);
    }

    /** POST /reports/export?studentId=S001&semester=SEM1+2024/2025 */
    @PostMapping("/export")
    ResponseEntity<ApiResponse<String>> exportReport(
            @RequestParam String studentId,
            @RequestParam String semester) {
        try {
            AcademicReportEntity r = reportService.generateSemesterReport(studentId, semester);
            String content = reportService.exportReport(r);
            return ApiResponse.ok("Report exported.", content);
        } catch (StudentNotFoundException e) { return ApiResponse.notFound(e.getMessage()); }
          catch (IllegalArgumentException  e) { return ApiResponse.badRequest(e.getMessage()); }
    }

    /** POST /reports/email?studentId=S001&semester=SEM1+2024/2025&email=alice@uni.edu */
    @PostMapping("/email")
    ResponseEntity<ApiResponse<Void>> emailReport(
            @RequestParam String studentId,
            @RequestParam String semester,
            @RequestParam @Email String email) {
        try {
            AcademicReportEntity r = reportService.generateSemesterReport(studentId, semester);
            reportService.emailReport(r, email);
            return ApiResponse.ok("Report emailed to " + email + " [SIMULATED].", null);
        } catch (StudentNotFoundException e) { return ApiResponse.notFound(e.getMessage()); }
          catch (IllegalArgumentException  e) { return ApiResponse.badRequest(e.getMessage()); }
    }
}

//  NotificationController  –  /notifications
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Validated
class NotificationController {

    private final EmailRecordService emailRecordService;

    /** GET /notifications/audit */
    @GetMapping("/audit")
    ResponseEntity<ApiResponse<List<EmailRecordEntity>>> getAuditLog() {
        List<EmailRecordEntity> log = emailRecordService.getAuditLog();
        return ApiResponse.ok(log.size() + " email record(s).", log);
    }

    /** GET /notifications/audit/{email} */
    @GetMapping("/audit/{email}")
    ResponseEntity<ApiResponse<List<EmailRecordEntity>>> getAuditForRecipient(
            @PathVariable @Email String email) {
        return ApiResponse.ok("Audit log for " + email,
            emailRecordService.getLogForRecipient(email));
    }

    /** POST /notifications/schedule */
    @PostMapping("/schedule")
    ResponseEntity<ApiResponse<ScheduledNotificationEntity>> schedule(
            @RequestParam @Email String recipientEmail,
            @RequestParam String emailType,
            @RequestParam String subject,
            @RequestParam String body,
            @RequestParam String scheduledFor,   // ISO-8601: 2024-11-01T09:00:00
            @RequestParam(required = false) String createdBy) {
        try {
            var n = emailRecordService.scheduleNotification(
                recipientEmail, emailType, subject, body,
                java.time.LocalDateTime.parse(scheduledFor),
                createdBy);
            return ApiResponse.created("Notification scheduled for " + scheduledFor, n);
        } catch (Exception e) {
            return ApiResponse.badRequest("Invalid request: " + e.getMessage());
        }
    }

    /** POST /notifications/scheduler/process */
    @PostMapping("/scheduler/process")
    ResponseEntity<ApiResponse<Integer>> triggerScheduler() {
        int dispatched = emailRecordService.processDueNotifications();
        return ApiResponse.ok(dispatched + " notification(s) dispatched.", dispatched);
    }

    /** DELETE /notifications/schedule/{email} */
    @DeleteMapping("/schedule/{email}")
    ResponseEntity<ApiResponse<Integer>> cancelScheduled(
            @PathVariable @Email String email) {
        int cancelled = emailRecordService.cancelScheduledForRecipient(email);
        return ApiResponse.ok(cancelled + " pending notification(s) cancelled.", cancelled);
    }
}

//  GlobalExceptionHandler
@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(StudentNotFoundException.class)
    ResponseEntity<ApiResponse<Void>> onStudentNotFound(StudentNotFoundException e) {
        return ApiResponse.notFound(e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ApiResponse<Void>> onBadArgument(IllegalArgumentException e) {
        return ApiResponse.badRequest(e.getMessage());
    }

    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    ResponseEntity<ApiResponse<Void>> onConstraintViolation(
            jakarta.validation.ConstraintViolationException e) {
        return ApiResponse.badRequest("Validation error: " + e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiResponse<Void>> onGeneral(Exception e) {
        return ResponseEntity.internalServerError()
            .body(new ApiResponse<>(500, "An unexpected error occurred: " + e.getMessage(), null));
    }
}