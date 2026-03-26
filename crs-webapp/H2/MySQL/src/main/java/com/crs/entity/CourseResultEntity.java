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
public class CourseResultEntity {

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

    /** Weighted quality points: gradePoint × creditHours */
    public double getWeightedPoints() {
        return gradePoint.doubleValue() * creditHours;
    }
}
