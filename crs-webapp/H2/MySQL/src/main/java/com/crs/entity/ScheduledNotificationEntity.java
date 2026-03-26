package com.crs.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "scheduled_notifications")
@Getter @Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class ScheduledNotificationEntity {

    public enum Status { PENDING, SENT, CANCELLED }

    @Id
    @Column(name = "id", length = 36)
    private String id = UUID.randomUUID().toString();

    @NotBlank @Email @Size(max = 160)
    @Column(name = "recipient_email", nullable = false, length = 160)
    private String recipientEmail;

    @NotBlank @Size(max = 30)
    @Column(name = "email_type", nullable = false, length = 30)
    private String emailType;

    @NotBlank @Size(max = 250)
    @Column(name = "subject", nullable = false, length = 250)
    private String subject;

    @NotBlank
    @Column(name = "body", nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(name = "scheduled_for", nullable = false)
    private LocalDateTime scheduledFor;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 12)
    private Status status = Status.PENDING;

    @Size(max = 20)
    @Column(name = "created_by", length = 20)
    private String createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public boolean isDue() {
        return status == Status.PENDING
            && !LocalDateTime.now().isBefore(scheduledFor);
    }
}
