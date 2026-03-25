package com.crs.email.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class EmailRecord {
    public enum EmailType {
        ACCOUNT_CREATED,
        ACCOUNT_UPDATED,
        ACCOUNT_DEACTIVATED,
        PASSWORD_RESET,
        RECOVERY_PLAN,
        PROGRESS_REMINDER,
        PERFORMANCE_REPORT
    }

    public enum DeliveryStatus { SENT, FAILED, PENDING }

    // Attributes
    private final String recordId;
    private final String recipientEmail;
    private final EmailType emailType;
    private final String subject;
    private final LocalDateTime sentAt;
    private DeliveryStatus deliveryStatus;
    private String failureReason;
 
    // Constructor
    public EmailRecord(String recipientEmail, EmailType emailType, String subject, DeliveryStatus status) {
        this.recordId = UUID.randomUUID().toString();
        this.recipientEmail = recipientEmail;
        this.emailType = emailType;
        this.subject = subject;
        this.sentAt = LocalDateTime.now();
        this.deliveryStatus = status;
    }
 
    // Getters
    public String getRecordId() { return recordId; }
    public String getRecipientEmail() { return recipientEmail; }
    public EmailType getEmailType() { return emailType; }
    public String getSubject() { return subject; }
    public LocalDateTime getSentAt() { return sentAt; }
    public DeliveryStatus getDeliveryStatus() { return deliveryStatus; }
    public String getFailureReason() { return failureReason; }
 
    // Setters
    public void setDeliveryStatus(DeliveryStatus status) { this.deliveryStatus = status; }
    public void setFailureReason(String reason) { this.failureReason  = reason; }
 
    @Override
    public String toString() {
        return String.format("EmailRecord{id='%s', to='%s', type=%s, status=%s, at=%s}",
                recordId, recipientEmail, emailType, deliveryStatus, sentAt);
    }
}
