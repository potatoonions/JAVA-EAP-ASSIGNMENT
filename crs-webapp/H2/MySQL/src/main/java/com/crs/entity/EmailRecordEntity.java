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
@Table(name = "email_records")
@Getter @Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class EmailRecordEntity {

    public enum EmailType {
        ACCOUNT_CREATED, ACCOUNT_UPDATED, ACCOUNT_DEACTIVATED,
        PASSWORD_RESET, RECOVERY_PLAN, PROGRESS_REMINDER, PERFORMANCE_REPORT
    }

    public enum DeliveryStatus { SENT, FAILED, PENDING }

    @Id
    @Column(name = "id", length = 36)
    private String id = UUID.randomUUID().toString();

    @NotBlank @Email @Size(max = 160)
    @Column(name = "recipient_email", nullable = false, length = 160)
    private String recipientEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "email_type", nullable = false, length = 30)
    private EmailType emailType;

    @NotBlank @Size(max = 250)
    @Column(name = "subject", nullable = false, length = 250)
    private String subject;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_status", nullable = false, length = 10)
    private DeliveryStatus deliveryStatus = DeliveryStatus.PENDING;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @CreationTimestamp
    @Column(name = "sent_at", nullable = false, updatable = false)
    private LocalDateTime sentAt;
}
