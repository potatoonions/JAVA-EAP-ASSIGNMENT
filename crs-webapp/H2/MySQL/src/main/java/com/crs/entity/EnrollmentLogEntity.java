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
@Table(name = "enrollment_log")
@Getter @Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class EnrollmentLogEntity {

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
