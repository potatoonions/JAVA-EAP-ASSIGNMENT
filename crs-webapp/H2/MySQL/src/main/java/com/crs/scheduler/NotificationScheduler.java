package com.crs.scheduler;

import com.crs.entity.EmailRecord.EmailType;
import com.crs.service.EmailService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@org.springframework.stereotype.Component
@lombok.RequiredArgsConstructor
@lombok.extern.slf4j.Slf4j
public class NotificationScheduler {

    private static final Logger LOGGER =
            Logger.getLogger(NotificationScheduler.class.getName());

    // State
    private final EmailService emailService;
    private final List<ScheduledNotification> queue = new ArrayList<>();

    // Constructor
    public NotificationScheduler(EmailService emailService) {
        if (emailService == null)
            throw new IllegalArgumentException("EmailService must not be null.");
        this.emailService = emailService;
    }

    // Scheduling API
    public ScheduledNotification schedule(String recipientEmail, EmailType emailType, String subject, String body, LocalDateTime scheduledFor, String createdByUserId) {
        ScheduledNotification notification = new ScheduledNotification(
                recipientEmail, emailType, subject, body, scheduledFor, createdByUserId);
        queue.add(notification);
        LOGGER.info("[Scheduler] Queued: " + notification);
        return notification;
    }

    public ScheduledNotification scheduleAfter(String recipientEmail, EmailType emailType, String subject, String body, long delayMinutes, String createdByUserId) {
        return schedule(recipientEmail, emailType, subject, body,
                LocalDateTime.now().plusMinutes(delayMinutes), createdByUserId);
    }

    public int processDueNotifications() {
        List<ScheduledNotification> due = queue.stream()
                .filter(ScheduledNotification::isDue)
                .collect(Collectors.toList());

        int dispatched = 0;
        for (ScheduledNotification n : due) {
            try {
                emailService.sendProgressReminder(n.getRecipientEmail(), n.getBody());
                n.markSent();
                dispatched++;
                LOGGER.info("[Scheduler] Dispatched: " + n.getNotificationId());
            } catch (Exception e) {
                LOGGER.warning("[Scheduler] Failed to dispatch " + n.getNotificationId()
                        + " — " + e.getMessage());
            }
        }

        if (dispatched > 0)
            LOGGER.info("[Scheduler] Cycle complete — dispatched " + dispatched + " notification(s).");
        return dispatched;
    }

    // Cancellation
    public int cancelForRecipient(String recipientEmail) {
        int count = 0;
        for (ScheduledNotification n : queue) {
            if (n.getRecipientEmail().equalsIgnoreCase(recipientEmail)
                    && n.getStatus() == ScheduledNotification.ScheduleStatus.PENDING) {
                n.markCancelled();
                count++;
            }
        }
        if (count > 0)
            LOGGER.info("[Scheduler] Cancelled " + count
                    + " pending notification(s) for: " + recipientEmail);
        return count;
    }

    // Inspection
    public List<ScheduledNotification> getQueue() {
        return Collections.unmodifiableList(queue);
    }

    public List<ScheduledNotification> getPendingNotifications() {
        return queue.stream()
                .filter(n -> n.getStatus() == ScheduledNotification.ScheduleStatus.PENDING)
                .collect(Collectors.toList());
    }

    public int queueSize() { return queue.size(); }
}
