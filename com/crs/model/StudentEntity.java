package com.crs.model;
 
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
 
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "students")
@Getter @Setter
@NoArgsConstructor
@ToString(exclude = {"user", "courseResults", "milestones"})
@EqualsAndHashCode(of = "userId")
public class StudentEntity {
 
    public enum RecoveryStatus {
        NOT_STARTED, IN_PROGRESS, MILESTONE_DUE, COMPLETED, WITHDRAWN
    }

    /* Primary Key */
    @Id
    @Column(name = "user_id", length = 20)
    private String userId;
 
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private UserEntity user;

    /* Columns */
    @NotBlank(message = "Program is required.")
    @Size(max = 120)
    @Column(name = "program", nullable = false)
    private String program;
 
    @Column(name = "current_level", nullable = false)
    private int currentLevel = 100;
 
    @NotBlank(message = "Current semester is required.")
    @Size(max = 30)
    @Column(name = "current_semester", nullable = false, length = 30)
    private String currentSemester;
 
    @DecimalMin("0.0") @DecimalMax("4.0")
    @Column(name = "cgpa", nullable = false, precision = 4, scale = 4)
    private BigDecimal cgpa = BigDecimal.ZERO;
 
    @Enumerated(EnumType.STRING)
    @Column(name = "recovery_status", nullable = false, length = 20)
    private RecoveryStatus recoveryStatus = RecoveryStatus.NOT_STARTED;
 
    @Column(name = "recovery_plan_detail", columnDefinition = "TEXT")
    private String recoveryPlanDetail;
 
    @Column(name = "advisor_email", length = 160)
    private String advisorEmail;

    /* Relationships */
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("semester ASC, courseCode ASC")
    private List<CourseResultEntity> courseResults = new ArrayList<>();
 
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<StudentMilestoneEntity> milestones = new ArrayList<>();

    /* Helper Methods */
    public void addCourseResult(CourseResultEntity result) {
        result.setStudent(this);
        courseResults.add(result);
    }
 
    public void addMilestone(StudentMilestoneEntity milestone) {
        milestone.setStudent(this);
        milestones.add(milestone);
    }

    public String getEmail() {
        return user != null ? user.getEmail() : null;
    }

    public String getFullName() {
        return user != null ? user.getFullName() : userId;
    }
}