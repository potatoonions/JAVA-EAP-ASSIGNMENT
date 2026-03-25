package com.crs.entity;

import java.time.LocalDateTime;
import java.util.UUID;

public class EmailRecord {

    public enum EmailType {
        ACCOUNT_CREATED, ACCOUNT_UPDATED, ACCOUNT_DEACTIVATED,
        PASSWORD_RESET, RECOVERY_PLAN, PROGRESS_REMINDER, PERFORMANCE_REPORT
    }

    public enum DeliveryStatus { SENT, FAILED, PENDING }

    private final String id;
    private final String recipientEmail;
    private final EmailType emailType;
    private final String subject;
    private final LocalDateTime sentAt;
    private DeliveryStatus deliveryStatus;
    private String failureReason;

    public EmailRecord(String recipientEmail, EmailType emailType, String subject, DeliveryStatus status) {
        this.id = UUID.randomUUID().toString();
        this.recipientEmail = recipientEmail;
        this.emailType = emailType;
        this.subject = subject;
        this.sentAt = LocalDateTime.now();
        this.deliveryStatus = status;
    }

    public String getId() { return id; }
    public String getRecipientEmail() { return recipientEmail; }
    public EmailType getEmailType() { return emailType; }
    public String getSubject() { return subject; }
    public LocalDateTime getSentAt() { return sentAt; }
    public DeliveryStatus getDeliveryStatus() { return deliveryStatus; }
    public String getFailureReason() { return failureReason; }

    public void setDeliveryStatus(DeliveryStatus s) { this.deliveryStatus = s; }
    public void setFailureReason(String r) { this.failureReason  = r; }

    @Override
    public String toString() {
        return String.format("EmailRecord{id='%s', to='%s', type=%s, status=%s}",
            id, recipientEmail, emailType, deliveryStatus);
    }
}
