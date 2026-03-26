package com.crs.controller;

import com.crs.entity.UserEntity;
import com.crs.exception.StudentNotFoundException;
import com.crs.exception.UserManagementExceptions.*;
import com.crs.logic.EligibilityChecker.EligibilityResult;
import com.crs.repository.EmailRecordRepository;
import com.crs.repository.ScheduledNotificationRepository;
import com.crs.service.*;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

//  ReportController  ─  /reports

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@Validated
class ReportController {

    private final ReportService reportService;

    /** GET /reports/semester?studentId=S001&semester=SEM1+2024/2025 */
    @GetMapping("/semester")
    ResponseEntity<ApiResponse<?>> semesterReport(
            @RequestParam @NotBlank String studentId,
            @RequestParam @NotBlank String semester) {
        try {
            var r = reportService.generateSemesterReport(studentId, semester);
            return (ResponseEntity<ApiResponse<?>>) (ResponseEntity<?>) ApiResponse.ok("Semester report generated. CGPA: " + r.getCgpa(), r);
        } catch (StudentNotFoundException e) { return (ResponseEntity<ApiResponse<?>>) (ResponseEntity<?>) ApiResponse.notFound(e.getMessage()); }
          catch (IllegalArgumentException  e) { return (ResponseEntity<ApiResponse<?>>) (ResponseEntity<?>) ApiResponse.badRequest(e.getMessage()); }
    }

    /** GET /reports/yearly?studentId=S001&year=2024 */
    @GetMapping("/yearly")
    ResponseEntity<ApiResponse<?>> yearlyReport(
            @RequestParam @NotBlank String studentId,
            @RequestParam @Min(2000) int year) {
        try {
            var r = reportService.generateYearlyReport(studentId, year);
            return (ResponseEntity<ApiResponse<?>>) (ResponseEntity<?>) ApiResponse.ok("Yearly report generated. CGPA: " + r.getCgpa(), r);
        } catch (StudentNotFoundException e) { return (ResponseEntity<ApiResponse<?>>) (ResponseEntity<?>) ApiResponse.notFound(e.getMessage()); }
          catch (IllegalArgumentException  e) { return (ResponseEntity<ApiResponse<?>>) (ResponseEntity<?>) ApiResponse.badRequest(e.getMessage()); }
    }

    /** GET /reports/student/{studentId} */
    @GetMapping("/student/{studentId}")
    ResponseEntity<ApiResponse<?>> listReports(@PathVariable String studentId) {
        return (ResponseEntity<ApiResponse<?>>) (ResponseEntity<?>) ApiResponse.ok("Reports retrieved.",
            reportService.getReportsForStudent(studentId));
    }

    /** POST /reports/export?studentId=S001&semester=SEM1+2024/2025 */
    @PostMapping("/export")
    ResponseEntity<ApiResponse<String>> exportReport(
            @RequestParam @NotBlank String studentId,
            @RequestParam @NotBlank String semester) {
        try {
            var r = reportService.generateSemesterReport(studentId, semester);
            return ApiResponse.ok("Report exported.", reportService.exportReport(r));
        } catch (StudentNotFoundException e) { return (ResponseEntity<ApiResponse<String>>) (ResponseEntity<?>) ApiResponse.notFound(e.getMessage()); }
          catch (IllegalArgumentException  e) { return (ResponseEntity<ApiResponse<String>>) (ResponseEntity<?>) ApiResponse.badRequest(e.getMessage()); }
    }

    /** POST /reports/email?studentId=S001&semester=SEM1+2024/2025&email=a@b.com */
    @PostMapping("/email")
    ResponseEntity<ApiResponse<Void>> emailReport(
            @RequestParam @NotBlank String studentId,
            @RequestParam @NotBlank String semester,
            @RequestParam @Email String email) {
        try {
            var r = reportService.generateSemesterReport(studentId, semester);
            reportService.emailReport(r, email);
            return ApiResponse.ok("Report emailed to " + email + " [SIMULATED].", null);
        } catch (StudentNotFoundException e) { return (ResponseEntity<ApiResponse<Void>>) (ResponseEntity<?>) ApiResponse.notFound(e.getMessage()); }
          catch (IllegalArgumentException  e) { return (ResponseEntity<ApiResponse<Void>>) (ResponseEntity<?>) ApiResponse.badRequest(e.getMessage()); }
    }
}
 
//  NotificationController  ─  /notifications

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Validated
class NotificationController {

    private final EmailRecordService emailRecordService;

    /** GET /notifications/audit */
    @GetMapping("/audit")
    ResponseEntity<ApiResponse<?>> getAuditLog() {
        var log = emailRecordService.getAuditLog();
        return (ResponseEntity<ApiResponse<?>>) (ResponseEntity<?>) ApiResponse.ok(log.size() + " email record(s).", log);
    }

    /** GET /notifications/audit/{email} */
    @GetMapping("/audit/{email}")
    ResponseEntity<ApiResponse<?>> auditByRecipient(@PathVariable @Email String email) {
        return (ResponseEntity<ApiResponse<?>>) (ResponseEntity<?>) ApiResponse.ok("Audit log for " + email,
            emailRecordService.getLogForRecipient(email));
    }

    /** POST /notifications/schedule */
    @PostMapping("/schedule")
    ResponseEntity<ApiResponse<?>> schedule(
            @RequestParam @Email String recipientEmail,
            @RequestParam @NotBlank String emailType,
            @RequestParam @NotBlank String subject,
            @RequestParam @NotBlank String body,
            @RequestParam @NotBlank String scheduledFor,
            @RequestParam(required = false) String createdBy) {
        try {
            var n = emailRecordService.scheduleNotification(
                recipientEmail, emailType, subject, body,
                LocalDateTime.parse(scheduledFor), createdBy);
            return (ResponseEntity<ApiResponse<?>>) (ResponseEntity<?>) ApiResponse.created("Notification scheduled.", n);
        } catch (Exception e) {
            return (ResponseEntity<ApiResponse<?>>) (ResponseEntity<?>) ApiResponse.badRequest("Invalid request: " + e.getMessage());
        }
    }

    /** POST /notifications/scheduler/process */
    @PostMapping("/scheduler/process")
    ResponseEntity<ApiResponse<Integer>> processScheduler() {
        int n = emailRecordService.processDueNotifications();
        return ApiResponse.ok(n + " notification(s) dispatched.", n);
    }

    /** DELETE /notifications/schedule/{email} */
    @DeleteMapping("/schedule/{email}")
    ResponseEntity<ApiResponse<Integer>> cancelScheduled(
            @PathVariable @Email String email) {
        int n = emailRecordService.cancelScheduledForRecipient(email);
        return ApiResponse.ok(n + " notification(s) cancelled.", n);
    }
}

//  UserController  ─  /users

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Validated
class UserController {

    private final UserService userService;

    /** POST /users — create a new user account */
    @PostMapping
    ResponseEntity<ApiResponse<?>> createUser(
            @RequestParam @NotBlank String userId,
            @RequestParam @NotBlank String firstName,
            @RequestParam @NotBlank String lastName,
            @RequestParam @Email String email,
            @RequestParam @NotBlank String password,
            @RequestParam(defaultValue = "STUDENT") String role) {
        try {
            UserEntity.Role r = UserEntity.Role.valueOf(role.toUpperCase());
            var u = userService.createUserAccount(firstName + " " + lastName, email, password, r);
            return (ResponseEntity<ApiResponse<?>>) (ResponseEntity<?>) ApiResponse.created("User account created.", Map.of("email", u.getEmail()));
        } catch (DuplicateEmailException e) { return (ResponseEntity<ApiResponse<?>>) (ResponseEntity<?>) ApiResponse.conflict(e.getMessage()); }
          catch (ValidationException e) { return (ResponseEntity<ApiResponse<?>>) (ResponseEntity<?>) ApiResponse.badRequest(e.getMessage()); }
          catch (Exception e) { return (ResponseEntity<ApiResponse<?>>) (ResponseEntity<?>) ApiResponse.badRequest(e.getMessage()); }
    }

    /** GET /users/{userId} */
    @GetMapping("/{userId}")
    ResponseEntity<ApiResponse<?>> getUser(@PathVariable String userId) {
        try {
            return (ResponseEntity<ApiResponse<?>>) (ResponseEntity<?>) ApiResponse.ok("User retrieved.", userService.getUserById(userId));
        } catch (UserNotFoundException e) { return (ResponseEntity<ApiResponse<?>>) (ResponseEntity<?>) ApiResponse.notFound(e.getMessage()); }
    }

    /** GET /users/email/{email} */
    @GetMapping("/email/{email}")
    ResponseEntity<ApiResponse<?>> getUserByEmail(@PathVariable @Email String email) {
        try {
            return (ResponseEntity<ApiResponse<?>>) (ResponseEntity<?>) ApiResponse.ok("User retrieved.", userService.getUserByEmail(email));
        } catch (UserNotFoundException e) { return (ResponseEntity<ApiResponse<?>>) (ResponseEntity<?>) ApiResponse.notFound(e.getMessage()); }
    }

    /** GET /users */
    @GetMapping
    ResponseEntity<ApiResponse<?>> listUsers() {
        return (ResponseEntity<ApiResponse<?>>) (ResponseEntity<?>) ApiResponse.ok("Users retrieved.", userService.getAllUsers());
    }

    /** PUT /users/{userId}/status?status=INACTIVE */
    @PutMapping("/{userId}/status")
    ResponseEntity<ApiResponse<Void>> updateStatus(
            @PathVariable String userId,
            @RequestParam @NotBlank String status) {
        try {
            if ("INACTIVE".equalsIgnoreCase(status) || "DEACTIVATED".equalsIgnoreCase(status)) {
                userService.deactivateAccount(userId);
                return ApiResponse.ok("Account deactivated.", null);
            }
            if ("ACTIVE".equalsIgnoreCase(status)) {
                // Recovery by email — fetch email first
                var u = userService.getUserById(userId);
                userService.recoverAccount(u.getEmail());
                return ApiResponse.ok("Account recovered.", null);
            }
            return ApiResponse.badRequest("Unknown status: " + status);
        } catch (UserNotFoundException e) { return ApiResponse.notFound(e.getMessage()); }
          catch (IllegalArgumentException e) { return ApiResponse.badRequest(e.getMessage()); }
    }

    /** POST /users/recover?email=a@b.com */
    @PostMapping("/recover")
    ResponseEntity<ApiResponse<Void>> recoverAccount(@RequestParam @Email String email) {
        try {
            userService.recoverAccount(email);
            return ApiResponse.ok("Account recovered.", null);
        } catch (UserNotFoundException e) { return ApiResponse.notFound(e.getMessage()); }
          catch (DeactivatedAccountException e) { return ApiResponse.forbidden(e.getMessage()); }
          catch (Exception e) { return ApiResponse.badRequest(e.getMessage()); }
    }

    /** POST /users/{userId}/role?role=INSTRUCTOR */
    @PostMapping("/{userId}/role")
    ResponseEntity<ApiResponse<Void>> assignRole(
            @PathVariable String userId,
            @RequestParam @NotBlank String role) {
        try {
            UserEntity.Role r = UserEntity.Role.valueOf(role.toUpperCase());
            var u = userService.getUserById(userId);
            u.setRole(r);
            userService.updateUserAccount(userId, null, null, r, null);
            return ApiResponse.ok("Role '" + role + "' assigned to userId=" + userId + ".", null);
        } catch (UserNotFoundException e) { return ApiResponse.notFound(e.getMessage()); }
          catch (IllegalArgumentException e) { return ApiResponse.badRequest(e.getMessage()); }
    }
}

//  AuthController  ─  /auth

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Validated
class AuthController {

    private final UserService userService;

    /** POST /auth/reset-password?email=a@b.com */
    @PostMapping("/reset-password")
    ResponseEntity<ApiResponse<String>> resetPassword(
            @RequestParam @Email String email) {
        try {
            String token = userService.resetPassword(email);
            return ApiResponse.ok("Password reset email dispatched [SIMULATED].", token);
        } catch (UserNotFoundException e) {
            return ApiResponse.ok("If that email exists, a reset link has been sent.", null);
        }
    }

    /** POST /auth/reset-password/complete?token=xxx&newPassword=yyy */
    @PostMapping("/reset-password/complete")
    ResponseEntity<ApiResponse<Void>> completeReset(
            @RequestParam @NotBlank String token,
            @RequestParam @NotBlank String newPassword) {
        try {
            userService.completePasswordReset(token, newPassword);
            return ApiResponse.ok("Password has been reset successfully.", null);
        } catch (InvalidTokenException e) { return ApiResponse.badRequest(e.getMessage()); }
          catch (ValidationException e) { return ApiResponse.badRequest(e.getMessage()); }
    }

    /** POST /auth/validate?email=a@b.com&password=xxx */
    @PostMapping("/validate")
    ResponseEntity<ApiResponse<Boolean>> validateCredentials(
            @RequestParam @Email String email,
            @RequestParam @NotBlank String password) {
        boolean valid = userService.validateCredentials(email, password);
        return ApiResponse.ok(valid ? "Credentials valid." : "Credentials invalid.", valid);
    }
}

//  HealthController  ─  /health

@RestController
@RequestMapping("/health")
class HealthController {
    @GetMapping
    ResponseEntity<ApiResponse<Map<String, String>>> health() {
        return ApiResponse.ok("CRS is running.", Map.of(
            "status", "UP",
            "version", "1.0.0",
            "profile", System.getProperty("spring.profiles.active", "default")));
    }
}

//  GlobalExceptionHandler

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(StudentNotFoundException.class)
    ResponseEntity<ApiResponse<Void>> onStudentNotFound(StudentNotFoundException e) {
        return ApiResponse.notFound(e.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    ResponseEntity<ApiResponse<Void>> onUserNotFound(UserNotFoundException e) {
        return ApiResponse.notFound(e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ApiResponse<Void>> onBadArg(IllegalArgumentException e) {
        return ApiResponse.badRequest(e.getMessage());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ApiResponse<Void>> onConstraintViolation(ConstraintViolationException e) {
        return ApiResponse.badRequest("Validation error: " + e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiResponse<Void>> onGeneral(Exception e) {
        return ApiResponse.error("Unexpected error: " + e.getMessage());
    }
}
