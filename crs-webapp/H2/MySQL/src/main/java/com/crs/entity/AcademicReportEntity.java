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
@Table(name = "academic_reports")
@Getter @Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class AcademicReportEntity {

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
