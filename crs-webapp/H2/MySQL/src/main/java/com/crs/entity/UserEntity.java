package com.crs.model;
 
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;
 
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter @Setter
@NoArgsConstructor
@ToString(exclude = "student")
@EqualsAndHashCode(of = "userId")
public class UserEntity {
    /* Role */
    public enum Role { STUDENT, INSTRUCTOR, ADMINISTRATOR }

    /* Acc Status */
    public enum AccountStatus {
        ACTIVE, INACTIVE, SUSPENDED, PENDING_VERIFICATION
    }

    /* Columns */
    @Id
    @Column(name = "user_id", length = 20, nullable = false)
    private String userId;
 
    @NotBlank(message = "First name is required.")
    @Size(max = 80)
    @Column(name = "first_name", nullable = false)
    private String firstName;
 
    @NotBlank(message = "Last name is required.")
    @Size(max = 80)
    @Column(name = "last_name", nullable = false)
    private String lastName;
 
    @NotBlank(message = "Email is required.")
    @Email(message = "Email must be a valid address.")
    @Column(name = "email", nullable = false, unique = true, length = 160)
    private String email;
 
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private Role role = Role.STUDENT;
 
    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false, length = 30)
    private AccountStatus accountStatus = AccountStatus.PENDING_VERIFICATION;
 
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
 
    @UpdateTimestamp
    @Column(name = "last_modified_at", nullable = false)
    private LocalDateTime lastModifiedAt;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private StudentEntity student;

    public String getFullName() {
        return firstName + " " + lastName;
    }
 
    public UserEntity(String userId, String firstName, String lastName,
                      String email, Role role) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.role = role;
    } 
}
