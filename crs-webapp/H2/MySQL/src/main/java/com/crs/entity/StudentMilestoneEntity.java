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
@Table(name = "student_milestones")
@Getter @Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class StudentMilestoneEntity {

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
