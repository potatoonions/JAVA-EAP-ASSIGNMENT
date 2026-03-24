package com.crs.usermanagement.entity;
 
import java.time.LocalDateTime;
import java.util.regex.Pattern;

public class User {
    /* Inner numerations */
    public enum Role {
        STUDENT,
        INSTRUCTOR,
        ADMIN
    }

    public enum Status {
        ACTIVE,
        INACTIVE,
        DEACTIVATED,
        PENDING_VERIFICATION  
    }

    /* Validation Helpers */
    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public static final int MIN_PASSWORD_LENGTH = 8;

    /* User Attributes */
    private int userId;
    private String name;
    private String email;
    private String password;
    private Role role;
    private Status status;
    private String passwordResetToken;
    private LocalDateTime passwordResetExpiry;
    private int failedLoginAttempts;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /* Constructors */
    public User() {
        this.status    = Status.PENDING_VERIFICATION;
        this.role      = Role.STUDENT;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public User(int userId, String name, String email,
                String password, Role role, Status status) {
        this();
        setUserId(userId);
        setName(name);
        setEmail(email);
        setPassword(password);
        setRole(role);
        setStatus(status);
    }

    public User(int userId, String name, String email, String password) {
        this(userId, name, email, password, Role.STUDENT, Status.PENDING_VERIFICATION);
    }

    /* Getters */
    public int getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public Role getRole() { return role; }
    public Status getStatus() { return status; }
    public String getPasswordResetToken() { return passwordResetToken; }
    public LocalDateTime getPasswordResetExpiry() { return passwordResetExpiry; }
    public int getFailedLoginAttempts() { return failedLoginAttempts; }
    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    /* Setters */
    public void setUserId(int userId) {
        if (userId < 0)
            throw new IllegalArgumentException("User ID must not be negative.");
        this.userId = userId;
        this.updatedAt = LocalDateTime.now();
    }
 
    public void setName(String name) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Name must not be blank.");
        if (name.trim().length() > 120)
            throw new IllegalArgumentException("Name must not exceed 120 characters.");
        this.name = name.trim();
        this.updatedAt = LocalDateTime.now();
    }
 
    public void setEmail(String email) {
        if (email == null || email.isBlank())
            throw new IllegalArgumentException("Email must not be blank.");
        if (!EMAIL_PATTERN.matcher(email.trim()).matches())
            throw new IllegalArgumentException("Email format is invalid: " + email);
        this.email = email.trim().toLowerCase();
        this.updatedAt = LocalDateTime.now();
    }
 
    public void setPassword(String password) {
        if (password == null || password.isBlank())
            throw new IllegalArgumentException("Password must not be blank.");
        this.password = password;
        this.updatedAt = LocalDateTime.now();
    }
 
    public void setRole(Role role) {
        if (role == null)
            throw new IllegalArgumentException("Role must not be null.");
        this.role = role;
        this.updatedAt = LocalDateTime.now();
    }
 
    public void setStatus(Status status) {
        if (status == null)
            throw new IllegalArgumentException("Status must not be null.");
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }
 
    public void setPasswordResetToken(String token) { this.passwordResetToken  = token; }
    public void setPasswordResetExpiry(LocalDateTime expiry) { this.passwordResetExpiry = expiry; }
    public void setFailedLoginAttempts(int attempts) { this.failedLoginAttempts = attempts; }
    public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt   = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt   = updatedAt; }

    /* Convenience Helpers */
    public boolean isActive() { return status == Status.ACTIVE; }

    public boolean isAdmin() { return role == Role.ADMIN; }

    public void incrementFailedAttempts() { this.failedLoginAttempts++; }

    public void clearFailedAttempts() { this.failedLoginAttempts = 0; }

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidPassword(String rawPassword) {
        return rawPassword != null && rawPassword.length() >= MIN_PASSWORD_LENGTH;
    }

    /* Object Overrides */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User u)) return false;
        return userId == u.userId;
    }
 
    @Override
    public int hashCode() { return Integer.hashCode(userId); }
 
    @Override
    public String toString() {
        return String.format(
            "User{userId=%d, name='%s', email='%s', role=%s, status=%s}",
            userId, name, email, role, status);
    }
}