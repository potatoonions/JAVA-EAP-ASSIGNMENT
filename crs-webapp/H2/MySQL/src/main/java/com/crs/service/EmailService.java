package com.crs.service;

import crs.config.EmailConfig;
import crs.exception.EmailDeliveryException;
import crs.entity.AcademicReport;
import crs.entity.EmailRecord;
import crs.entity.EmailRecord.DeliveryStatus;
import crs.entity.EmailRecord.EmailType;
 
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public class EmailService {
    private static final Logger LOGGER = Logger.getLogger(EmailService.class.getName());
 
    public EmailRecord sendAccountEmail(String email, String subject, String message) {
        validateEmail(email);
        validateContent(message, "Account notification message");
        return dispatchEmail(email, subject, message, EmailType.ACCOUNT_CREATED);
    }
 
    public EmailRecord sendPasswordResetEmail(String email, String resetLink, String message) {
        validateEmail(email);
        if (resetLink == null || resetLink.isBlank())
            throw new IllegalArgumentException("Reset link must not be blank.");
        validateContent(message, "Password reset message");
        return dispatchEmail(email,
                "[CRS] Password Reset Request",
                message,
                EmailType.PASSWORD_RESET);
    }
 
    public EmailRecord sendRecoveryPlanEmail(String email, String planDetails, String message) {
        validateEmail(email);
        if (planDetails == null || planDetails.isBlank())
            throw new IllegalArgumentException("Plan details must not be blank.");
        validateContent(message, "Recovery plan message");
        return dispatchEmail(email,
                "[CRS] Your Course Recovery Plan Is Ready",
                message,
                EmailType.RECOVERY_PLAN);
    }
 
    public EmailRecord sendProgressReminder(String email, String reminderMessage) {
        validateEmail(email);
        validateContent(reminderMessage, "Reminder message");
        return dispatchEmail(email,
                "[CRS] Recovery Milestone Reminder",
                reminderMessage,
                EmailType.PROGRESS_REMINDER);
    }
 
    public EmailRecord sendPerformanceReport(String email, AcademicReport report, String message) {
        validateEmail(email);
        if (report == null)
            throw new IllegalArgumentException("AcademicReport must not be null.");
        validateContent(message, "Performance report message");
        return dispatchEmail(email,
                "[CRS] Your Academic Performance Report — " + report.getSemester(),
                message,
                EmailType.PERFORMANCE_REPORT);
    }
 
    // Audit log access
    public List<EmailRecord> getAuditLog() {
        return Collections.unmodifiableList(auditLog);
    }
 
    public List<EmailRecord> getSentEmails() {
        List<EmailRecord> sent = new ArrayList<>();
        for (EmailRecord r : auditLog)
            if (r.getDeliveryStatus() == DeliveryStatus.SENT) sent.add(r);
        return sent;
    }
 
    public int getTotalDispatched() { return auditLog.size(); }
 
    // Core dispatch engine
    private EmailRecord dispatchEmail(String to, String subject, String body, EmailType emailType) {
        EmailRecord record = new EmailRecord(to, emailType, subject, DeliveryStatus.PENDING);
 
        try {
            if (EmailConfig.SIMULATION_MODE) {
                simulateSend(to, subject, body);
            } else {
                liveSend(to, subject, body);
            }
            record.setDeliveryStatus(DeliveryStatus.SENT);
            LOGGER.info(String.format("[EmailService] SENT — type=%s, to=%s", emailType, to));
 
        } catch (Exception e) {
            record.setDeliveryStatus(DeliveryStatus.FAILED);
            record.setFailureReason(e.getMessage());
            LOGGER.severe(String.format(
                    "[EmailService] FAILED — type=%s, to=%s, reason=%s",
                    emailType, to, e.getMessage()));
            throw new EmailDeliveryException(to,
                    "Email delivery failed for recipient: " + to, e);
        } finally {
            auditLog.add(record);
        }
 
        return record;
    }
 
    // Transport implementations
    private void simulateSend(String to, String subject, String body) {
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss"));
        String fence = "━".repeat(72);
 
        System.out.println("\n" + fence);
        System.out.println("  ✉  SIMULATED EMAIL DISPATCH");
        System.out.println(fence);
        System.out.println("  From    : " + EmailConfig.SENDER_NAME + " <" + EmailConfig.SENDER_EMAIL + ">");
        System.out.println("  To      : " + to);
        System.out.println("  Reply-To: " + EmailConfig.REPLY_TO_EMAIL);
        System.out.println("  Subject : " + subject);
        System.out.println("  Sent At : " + ts);
        System.out.println("  " + "─".repeat(68));
        System.out.println(body);
        System.out.println(fence + "\n");
    }

    private void liveSend(String to, String subject, String body) {
        throw new UnsupportedOperationException(
            "Live email sending is not configured. Enable SIMULATION_MODE or "
            + "implement liveSend() with your SMTP/API credentials.");
    }
 
    // Validation helpers 
    private void validateEmail(String email) {
        if (email == null || email.isBlank())
            throw new IllegalArgumentException("Recipient email address must not be blank.");
        if (!email.contains("@") || !email.contains("."))
            throw new IllegalArgumentException(
                    "Recipient email address format is invalid: " + email);
    }
 
    private void validateContent(String content, String fieldName) {
        if (content == null || content.isBlank())
            throw new IllegalArgumentException(fieldName + " must not be blank.");
    }
}