package com.crs.scheduler;

import com.crs.entity.EmailRecord.EmailType;
 
import java.time.LocalDateTime;
import java.util.UUID;

public class ScheduledNotification {
    public enum ScheduleStatus { PENDING, SENT, CANCELLED }
 
    // Attributes
    private final String         notificationId;
    private final String         recipientEmail;
    private final EmailType      emailType;
    private final String         subject;
    private final String         body;
    private final LocalDateTime  scheduledFor;
    private       ScheduleStatus status;
    private final String         createdByUserId;
    // Constructor 
    public ScheduledNotification(String recipientEmail, EmailType emailType, String subject, String body, LocalDateTime scheduledFor, String createdByUserId) {
        this.notificationId = UUID.randomUUID().toString();
        this.recipientEmail = recipientEmail;
        this.emailType = emailType;
        this.subject = subject;
        this.body = body;
        this.scheduledFor = scheduledFor;
        this.status = ScheduleStatus.PENDING;
        this.createdByUserId = createdByUserId;
    }
 
    // Getters 
    public String getNotificationId() { return notificationId; }
    public String getRecipientEmail() { return recipientEmail; }
    public EmailType getEmailType() { return emailType; }
    public String getSubject() { return subject; }
    public String getBody() { return body; }
    public LocalDateTime getScheduledFor() { return scheduledFor; }
    public ScheduleStatus getStatus() { return status; }
    public String getCreatedByUserId() { return createdByUserId; }
 
    // Mutation
    public void markSent() { this.status = ScheduleStatus.SENT; }
    public void markCancelled() { this.status = ScheduleStatus.CANCELLED; }
 
    public boolean isDue() {
        return status == ScheduleStatus.PENDING
                && !LocalDateTime.now().isBefore(scheduledFor);
    }
 
    @Override
    public String toString() {
        return String.format(
                "ScheduledNotification{id='%s', to='%s', type=%s, scheduledFor=%s, status=%s}",
                notificationId, recipientEmail, emailType, scheduledFor, status);
    }
}
