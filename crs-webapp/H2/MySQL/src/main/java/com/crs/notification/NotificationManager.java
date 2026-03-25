package com.crs.email.notification;

import com.crs.email.config.EmailConfig;
import com.crs.email.exception.DuplicateNotificationException;
import com.crs.email.exception.EmailDeliveryException;
import com.crs.email.entity.AcademicReport;
import com.crs.email.entity.EmailRecord;
import com.crs.email.entity.EmailRecord.EmailType;
import com.crs.email.entity.Student;
import com.crs.email.entity.User;
import com.crs.email.entity.User.AccountStatus;
import com.crs.email.scheduler.NotificationScheduler;
import com.crs.email.scheduler.ScheduledNotification;
import com.crs.email.service.EmailService;
import com.crs.email.template.EmailTemplates;
import com.crs.email.template.TemplateEngine;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

public class NotificationManager {

    private static final Logger LOGGER = Logger.getLogger(NotificationManager.class.getName());

    // Dependencies
    private final EmailService emailService;
    private final NotificationScheduler scheduler;

    // Duplicate-suppression ledger   key: "recipientEmail|TYPE"
    private final Map<String, LocalDateTime> lastSentLedger = new HashMap<>();

    // Constructor
    public NotificationManager(EmailService emailService, NotificationScheduler scheduler) {
        if (emailService == null)
            throw new IllegalArgumentException("EmailService must not be null.");
        if (scheduler == null)
            throw new IllegalArgumentException("NotificationScheduler must not be null.");
        this.emailService = emailService;
        this.scheduler = scheduler;
    } 

    // Account lifecycle notifications
    public void notifyAccountCreated(User user) {
        validateUser(user);

        if (isDuplicate(user.getEmail(), EmailType.ACCOUNT_CREATED)) return;

        Map<String, String> tokens = commonTokens(user);
        tokens.put("{LOGIN_URL}", EmailConfig.LOGIN_URL);

        String body = TemplateEngine.resolve(EmailTemplates.BODY_ACCOUNT_CREATED, tokens);

        try {
            emailService.sendAccountEmail(
                    user.getEmail(),
                    EmailTemplates.SUBJECT_ACCOUNT_CREATED,
                    body);
            recordSent(user.getEmail(), EmailType.ACCOUNT_CREATED);
            LOGGER.info("[NotificationManager] Account-created notification sent to: "
                    + user.getEmail());
        } catch (EmailDeliveryException e) {
            LOGGER.severe("[NotificationManager] Failed to send account-created notification: "
                    + e.getMessage());
        }
    }

    public void notifyAccountUpdated(User user) {
        validateUser(user);

        if (user.getAccountStatus() == AccountStatus.INACTIVE) {
            notifyAccountDeactivated(user);
            return;
        }

        if (isDuplicate(user.getEmail(), EmailType.ACCOUNT_UPDATED)) return;

        Map<String, String> tokens = commonTokens(user);
        tokens.put("{LOGIN_URL}", EmailConfig.LOGIN_URL);

        String body = TemplateEngine.resolve(EmailTemplates.BODY_ACCOUNT_UPDATED, tokens);

        try {
            emailService.sendAccountEmail(
                    user.getEmail(),
                    EmailTemplates.SUBJECT_ACCOUNT_UPDATED,
                    body);
            recordSent(user.getEmail(), EmailType.ACCOUNT_UPDATED);
            LOGGER.info("[NotificationManager] Account-updated notification sent to: "
                    + user.getEmail());
        } catch (EmailDeliveryException e) {
            LOGGER.severe("[NotificationManager] Failed to send account-updated notification: "
                    + e.getMessage());
        }
    }

    public void notifyAccountDeactivated(User user) {
        validateUser(user);

        if (isDuplicate(user.getEmail(), EmailType.ACCOUNT_DEACTIVATED)) return;

        Map<String, String> tokens = commonTokens(user);

        String body = TemplateEngine.resolve(EmailTemplates.BODY_ACCOUNT_DEACTIVATED, tokens);

        try {
            emailService.sendAccountEmail(
                    user.getEmail(),
                    EmailTemplates.SUBJECT_ACCOUNT_DEACTIVATED,
                    body);
            recordSent(user.getEmail(), EmailType.ACCOUNT_DEACTIVATED);
            LOGGER.info("[NotificationManager] Account-deactivated notification sent to: "
                    + user.getEmail());
        } catch (EmailDeliveryException e) {
            LOGGER.severe("[NotificationManager] Failed to send deactivation notification: "
                    + e.getMessage());
        }
    }

    // Password reset
    public void notifyPasswordReset(User user) {
        validateUser(user);

        if (isDuplicate(user.getEmail(), EmailType.PASSWORD_RESET)) return;

        // Generate a reset token (in production use SecureRandom / JWT)
        String resetToken = UUID.randomUUID().toString().replace("-", "");
        String resetLink  = EmailConfig.PASSWORD_RESET_URL + resetToken;

        Map<String, String> tokens = commonTokens(user);
        tokens.put("{RESET_LINK}", resetLink);

        String body = TemplateEngine.resolve(EmailTemplates.BODY_PASSWORD_RESET, tokens);

        try {
            emailService.sendPasswordResetEmail(user.getEmail(), resetLink, body);
            recordSent(user.getEmail(), EmailType.PASSWORD_RESET);
            LOGGER.info("[NotificationManager] Password-reset notification sent to: "
                    + user.getEmail());
        } catch (EmailDeliveryException e) {
            LOGGER.severe("[NotificationManager] Failed to send password-reset notification: "
                    + e.getMessage());
        }
    }

    // Recovery plan notifications
    public void notifyRecoveryPlanCreated(Student student) {
        validateStudent(student);

        if (student.getRecoveryPlanDetails() == null
                || student.getRecoveryPlanDetails().isBlank()) {
            throw new IllegalArgumentException(
                    "Student " + student.getUserId()
                    + " has no recovery plan details to include in the notification.");
        }

        if (isDuplicate(student.getEmail(), EmailType.RECOVERY_PLAN)) return;

        Map<String, String> tokens = commonTokens(student);
        tokens.put("{PLAN_DETAILS}", student.getRecoveryPlanDetails());
        tokens.put("{LOGIN_URL}",    EmailConfig.LOGIN_URL);

        String body = TemplateEngine.resolve(EmailTemplates.BODY_RECOVERY_PLAN, tokens);

        try {
            emailService.sendRecoveryPlanEmail(
                    student.getEmail(),
                    student.getRecoveryPlanDetails(),
                    body);
            recordSent(student.getEmail(), EmailType.RECOVERY_PLAN);
            LOGGER.info("[NotificationManager] Recovery-plan notification sent to: "
                    + student.getEmail());

            scheduleProgressReminder(student,
                    EmailConfig.REMINDER_HOURS_BEFORE_DEADLINE * 60L);

        } catch (EmailDeliveryException e) {
            LOGGER.severe("[NotificationManager] Failed to send recovery-plan notification: "
                    + e.getMessage());
        }
    }

    public void notifyRecoveryProgress(Student student) {
        validateStudent(student);

        if (!student.hasPendingMilestones()) {
            LOGGER.info("[NotificationManager] No pending milestones for student "
                    + student.getUserId() + " — reminder skipped.");
            return;
        }

        if (isDuplicate(student.getEmail(), EmailType.PROGRESS_REMINDER)) return;

        Map<String, String> tokens = commonTokens(student);
        tokens.put("{MILESTONES}", buildMilestoneList(student.getPendingMilestones()));
        tokens.put("{LOGIN_URL}", EmailConfig.LOGIN_URL);

        String body = TemplateEngine.resolve(EmailTemplates.BODY_PROGRESS_REMINDER, tokens);

        try {
            emailService.sendProgressReminder(student.getEmail(), body);
            recordSent(student.getEmail(), EmailType.PROGRESS_REMINDER);
            LOGGER.info("[NotificationManager] Progress-reminder notification sent to: "
                    + student.getEmail());
        } catch (EmailDeliveryException e) {
            LOGGER.severe("[NotificationManager] Failed to send progress-reminder: "
                    + e.getMessage());
        }
    }

    // Academic performance report
    public void notifyPerformanceReport(Student student, AcademicReport report) {
        validateStudent(student);
        if (report == null)
            throw new IllegalArgumentException("AcademicReport must not be null.");

        if (isDuplicate(student.getEmail(), EmailType.PERFORMANCE_REPORT)) return;

        Map<String, String> tokens = commonTokens(student);
        tokens.put("{REPORT_BODY}", report.toEmailBody());
        tokens.put("{LOGIN_URL}",   EmailConfig.LOGIN_URL);

        String body = TemplateEngine.resolve(EmailTemplates.BODY_PERFORMANCE_REPORT, tokens);

        try {
            emailService.sendPerformanceReport(student.getEmail(), report, body);
            recordSent(student.getEmail(), EmailType.PERFORMANCE_REPORT);
            LOGGER.info("[NotificationManager] Performance-report notification sent to: "
                    + student.getEmail());

            if (student.getAdvisorEmail() != null && !student.getAdvisorEmail().isBlank()) {
                Map<String, String> advisorTokens = new HashMap<>(tokens);
                advisorTokens.put("{FULL_NAME}", "Academic Advisor");
                String advisorBody = TemplateEngine.resolve(
                        EmailTemplates.BODY_PERFORMANCE_REPORT, advisorTokens);
                emailService.sendPerformanceReport(
                        student.getAdvisorEmail(), report, advisorBody);
                LOGGER.info("[NotificationManager] Performance-report CC sent to advisor: "
                        + student.getAdvisorEmail());
            }

        } catch (EmailDeliveryException e) {
            LOGGER.severe("[NotificationManager] Failed to send performance-report: "
                    + e.getMessage());
        }
    }

    // Scheduler integration
    public ScheduledNotification scheduleProgressReminder(Student student, long delayMinutes) {
        validateStudent(student);

        Map<String, String> tokens = commonTokens(student);
        tokens.put("{MILESTONES}", student.hasPendingMilestones()
                ? buildMilestoneList(student.getPendingMilestones())
                : "  • Please check your CRS portal for any outstanding tasks.");
        tokens.put("{LOGIN_URL}", EmailConfig.LOGIN_URL);

        String body = TemplateEngine.resolve(EmailTemplates.BODY_PROGRESS_REMINDER, tokens);

        ScheduledNotification scheduled = scheduler.scheduleAfter(
                student.getEmail(),
                EmailType.PROGRESS_REMINDER,
                EmailTemplates.SUBJECT_PROGRESS_REMINDER,
                body,
                delayMinutes,
                student.getUserId());

        LOGGER.info(String.format(
                "[NotificationManager] Scheduled reminder for student %s in %d minute(s). ID: %s",
                student.getUserId(), delayMinutes, scheduled.getNotificationId()));

        return scheduled;
    }

    // Duplicate suppression helpers
    private boolean isDuplicate(String email, EmailType type) {
        String key = email.toLowerCase() + "|" + type.name();
        LocalDateTime lastSent = lastSentLedger.get(key);
        if (lastSent == null) return false;

        LocalDateTime suppressUntil = lastSent.plusMinutes(
                EmailConfig.DUPLICATE_SUPPRESSION_MINUTES);
        if (LocalDateTime.now().isBefore(suppressUntil)) {
            LOGGER.warning(String.format(
                    "[NotificationManager] Duplicate suppressed — type=%s, to=%s, "
                    + "last sent=%s, suppress until=%s",
                    type, email,
                    lastSent.format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                    suppressUntil.format(DateTimeFormatter.ofPattern("HH:mm:ss"))));
            return true;
        }
        return false;
    }

    private void recordSent(String email, EmailType type) {
        String key = email.toLowerCase() + "|" + type.name();
        lastSentLedger.put(key, LocalDateTime.now());
    }

    // Template token builders
    private Map<String, String> commonTokens(User user) {
        Map<String, String> tokens = new HashMap<>();
        tokens.put("{FULL_NAME}", user.getFullName());
        tokens.put("{USER_ID}", user.getUserId());
        tokens.put("{ROLE}", capitalise(user.getRole().name()));
        tokens.put("{SENDER_NAME}", EmailConfig.SENDER_NAME);
        tokens.put("{SUPPORT_EMAIL}", EmailConfig.REPLY_TO_EMAIL);
        return tokens;
    }

    private String buildMilestoneList(List<String> milestones) {
        if (milestones == null || milestones.isEmpty())
            return "  • No specific milestones listed — please check the CRS portal.";
        StringBuilder sb = new StringBuilder();
        for (String m : milestones) {
            sb.append("  • ").append(m).append("\n");
        }
        return sb.toString().stripTrailing();
    }

    // Validation helpers
    private void validateUser(User user) {
        if (user == null)
            throw new IllegalArgumentException("User must not be null.");
        if (user.getEmail() == null || user.getEmail().isBlank())
            throw new IllegalArgumentException(
                    "User email must not be blank (userId=" + user.getUserId() + ").");
    }

    private void validateStudent(Student student) {
        if (student == null)
            throw new IllegalArgumentException("Student must not be null.");
        validateUser(student);
    }

    // String utilities
    private static String capitalise(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase();
    }
}