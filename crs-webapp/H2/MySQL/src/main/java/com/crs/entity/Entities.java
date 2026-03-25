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
@Table(name = "course_results",
       uniqueConstraints = @UniqueConstraint(
           name = "uq_cr_student_course_semester",
           columnNames = {"student_id","course_code","semester"}))
@Getter @Setter
@NoArgsConstructor
@ToString(exclude = "student")
@EqualsAndHashCode(of = "id")
class CourseResultEntity {
 
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentEntity student;
 
    @NotBlank @Size(max = 15)
    @Column(name = "course_code", nullable = false, length = 15)
    private String courseCode;
 
    @NotBlank @Size(max = 120)
    @Column(name = "course_title", nullable = false, length = 120)
    private String courseTitle;
 
    @Min(1)
    @Column(name = "credit_hours", nullable = false)
    private int creditHours;
 
    @NotBlank @Size(max = 4)
    @Column(name = "grade", nullable = false, length = 4)
    private String grade;
 
    @DecimalMin("0.0") @DecimalMax("4.0")
    @Column(name = "grade_point", nullable = false, precision = 3, scale = 1)
    private BigDecimal gradePoint;
 
    @NotBlank @Size(max = 30)
    @Column(name = "semester", nullable = false, length = 30)
    private String semester;
 
    @Column(name = "academic_year", nullable = false)
    private int academicYear;
 
    @CreationTimestamp
    @Column(name = "recorded_at", nullable = false, updatable = false)
    private LocalDateTime recordedAt;
 
    public CourseResultEntity(StudentEntity student, String courseCode, String courseTitle, int creditHours, String grade, BigDecimal gradePoint, String semester, int academicYear) {
        this.student = student;
        this.courseCode = courseCode;
        this.courseTitle = courseTitle;
        this.creditHours = creditHours;
        this.grade = grade;
        this.gradePoint = gradePoint;
        this.semester = semester;
        this.academicYear = academicYear;
    }

        public double getWeightedPoints() {
        return gradePoint.doubleValue() * creditHours;
    }
}

/* AcademicReportEntity */
@Entity
@Table(name = "academic_reports")
@Getter @Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
class AcademicReportEntity {
 
    public enum ReportType { SEMESTER, YEARLY }
 
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentEntity student;
 
    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false, length = 10)
    private ReportType reportType = ReportType.SEMESTER;
 
    @NotBlank @Size(max = 30)
    @Column(name = "period", nullable = false, length = 30)
    private String period;
 
    @DecimalMin("0.0") @DecimalMax("4.0")
    @Column(name = "cgpa", nullable = false, precision = 4, scale = 4)
    private BigDecimal cgpa;
 
    @Column(name = "total_credit_hours", nullable = false)
    private int totalCreditHours;
 
    @Column(name = "total_grade_points", nullable = false, precision = 6, scale = 2)
    private BigDecimal totalGradePoints;
 
    @CreationTimestamp
    @Column(name = "generated_at", nullable = false, updatable = false)
    private LocalDateTime generatedAt;
}

/* EnrollmentLogEntity */
@Entity
@Table(name = "enrollment_log")
@Getter @Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
class EnrollmentLogEntity {
 
    public enum Decision { ENROLLED, INELIGIBLE, PENDING }
 
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentEntity student;
 
    @Enumerated(EnumType.STRING)
    @Column(name = "decision", nullable = false, length = 12)
    private Decision decision;
 
    @Column(name = "cgpa_at_check", precision = 4, scale = 4)
    private BigDecimal cgpaAtCheck;
 
    @Column(name = "failed_courses")
    private Integer failedCourses;
 
    @Column(name = "new_level")
    private Integer newLevel;
 
    @Column(name = "new_semester")
    private Integer newSemester;
 
    @CreationTimestamp
    @Column(name = "decided_at", nullable = false, updatable = false)
    private LocalDateTime decidedAt;
 
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}

/* EmailRecordEntity */
@Entity
@Table(name = "email_records")
@Getter @Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
class EmailRecordEntity {
 
    public enum EmailType {
        ACCOUNT_CREATED, ACCOUNT_UPDATED, ACCOUNT_DEACTIVATED, PASSWORD_RESET, RECOVERY_PLAN, PROGRESS_REMINDER, PERFORMANCE_REPORT
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

/* ScheduleNotificationEntity */
@Entity
@Table(name = "scheduled_notifications")
@Getter @Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
class ScheduledNotificationEntity {
 
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

/* StudentMilestoneEntity */
@Entity
@Table(name = "student_milestones")
@Getter @Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
class StudentMilestoneEntity {
 
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentEntity student;
 
    @NotBlank @Size(max = 250)
    @Column(name = "milestone", nullable = false, length = 250)
    private String milestone;
 
    @Column(name = "due_date")
    private LocalDate dueDate;
 
    @Column(name = "completed", nullable = false)
    private boolean completed = false;
 
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
