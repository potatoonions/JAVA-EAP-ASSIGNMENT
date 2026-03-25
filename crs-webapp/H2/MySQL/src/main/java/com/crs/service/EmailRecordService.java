package com.crs.service;
 
import com.crs.config.EmailConfig;
import com.crs.entity.EmailRecord;
import com.crs.entity.Entities;
import com.crs.repository.Repositories;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
 
import java.time.LocalDateTime;
import java.util.List;
 
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailRecordService {
 
    private final EmailRecordRepository emailRecordRepository;
    private final ScheduledNotificationRepository scheduledRepo;
 
    // Core record-keeping
    @Transactional
    public EmailRecordEntity record(String recipientEmail, EmailRecordEntity.EmailType type, String subject, EmailRecordEntity.DeliveryStatus status) {
        EmailRecordEntity r = new EmailRecordEntity();
        r.setRecipientEmail(recipientEmail.toLowerCase());
        r.setEmailType(type);
        r.setSubject(subject);
        r.setDeliveryStatus(status);
        EmailRecordEntity saved = emailRecordRepository.save(r);
        log.info("Email record saved — id={}, to={}, type={}, status={}",
            saved.getId(), recipientEmail, type, status);
        return saved;
    }
 
    @Transactional
    public void markFailed(String recordId, String failureReason) {
        emailRecordRepository.findById(recordId).ifPresent(r -> {
            r.setDeliveryStatus(EmailRecordEntity.DeliveryStatus.FAILED);
            r.setFailureReason(failureReason);
            emailRecordRepository.save(r);
            log.warn("Email record {} marked FAILED: {}", recordId, failureReason);
        });
    }
 
    // Duplicate suppression
    public boolean isDuplicate(String recipientEmail, EmailRecordEntity.EmailType type) {
        LocalDateTime since = LocalDateTime.now()
            .minusMinutes(EmailConfig.DUPLICATE_SUPPRESSION_MINUTES);
        boolean dup = emailRecordRepository
            .existsByRecipientEmailIgnoreCaseAndEmailTypeAndSentAtAfter(
                recipientEmail, type, since);
        if (dup) log.warn("Duplicate suppressed — type={}, to={}", type, recipientEmail);
        return dup;
    }
 
    // Audit queries
    public List<EmailRecordEntity> getAuditLog() {
        return emailRecordRepository.findAll(
            org.springframework.data.domain.Sort.by("sentAt").descending());
    }
 
    public List<EmailRecordEntity> getLogForRecipient(String email) {
        return emailRecordRepository
            .findByRecipientEmailIgnoreCaseOrderBySentAtDesc(email);
    }
 
    public List<EmailRecordEntity> getByStatus(
            EmailRecordEntity.DeliveryStatus status) {
        return emailRecordRepository.findByDeliveryStatus(status);
    }
 
    // Scheduled notification queue
    @Transactional
    public ScheduledNotificationEntity scheduleNotification(
            String recipientEmail,
            String emailType,
            String subject,
            String body,
            LocalDateTime scheduledFor,
            String createdBy) {
 
        ScheduledNotificationEntity n = new ScheduledNotificationEntity();
        n.setRecipientEmail(recipientEmail.toLowerCase());
        n.setEmailType(emailType);
        n.setSubject(subject);
        n.setBody(body);
        n.setScheduledFor(scheduledFor);
        n.setCreatedBy(createdBy);
        ScheduledNotificationEntity saved = scheduledRepo.save(n);
        log.info("Notification scheduled — id={}, to={}, at={}",
            saved.getId(), recipientEmail, scheduledFor);
        return saved;
    }
 
    @Transactional
    @Scheduled(fixedDelay = 60_000)
    public int processDueNotifications() {
        List<ScheduledNotificationEntity> due =
            scheduledRepo.findDueNotifications(LocalDateTime.now());
 
        int dispatched = 0;
        for (ScheduledNotificationEntity n : due) {
            try {
                log.info("[SCHEDULED SEND] to={}, type={}", n.getRecipientEmail(), n.getEmailType());
                record(n.getRecipientEmail(),
                    EmailRecordEntity.EmailType.valueOf(n.getEmailType()),
                    n.getSubject(),
                    EmailRecordEntity.DeliveryStatus.SENT);
 
                n.setStatus(ScheduledNotificationEntity.Status.SENT);
                scheduledRepo.save(n);
                dispatched++;
            } catch (Exception ex) {
                log.error("Failed to process scheduled notification {}: {}",
                    n.getId(), ex.getMessage());
            }
        }
        if (dispatched > 0)
            log.info("Scheduler cycle: dispatched {} notification(s).", dispatched);
        return dispatched;
    }
 
    @Transactional
    public int cancelScheduledForRecipient(String recipientEmail) {
        int count = scheduledRepo.cancelAllPendingForRecipient(
            recipientEmail.toLowerCase());
        if (count > 0)
            log.info("Cancelled {} pending notification(s) for {}", count, recipientEmail);
        return count;
    }
}
 