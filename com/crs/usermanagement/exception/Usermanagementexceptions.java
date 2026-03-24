package com.crs.usermanagement.exception;

public final class UserManagementExceptions {
 
    private UserManagementExceptions() {}

    /* 400 Bad Request */
    public static class ValidationException extends RuntimeException {
        private final String field;
        public ValidationException(String field, String message) {
            super(message);
            this.field = field;
        }
        public ValidationException(String message) {
            super(message);
            this.field = null;
        }
        public String getField() { return field; }
    }

    /* 401 Unauthorized */
    public static class AuthenticationException extends RuntimeException {
        public AuthenticationException(String message) { super(message); }
    }

    public static class AccountLockedException extends AuthenticationException {
        private final int failedAttempts;
        public AccountLockedException(int failedAttempts) {
            super("Account is temporarily locked after " + failedAttempts + " failed login attempts.");
            this.failedAttempts = failedAttempts;
        }
        public int getFailedAttempts() { return failedAttempts; }
    }

    /* 403 Forbidden */
    public static class AccessDeniedException extends RuntimeException {
        private final String action;
        private final String role;
        public AccessDeniedException(String action, String role) {
            super("Role " + role + " is not permitted to perform: " + action);
            this.action = action;
            this.role   = role;
        }
        public String getAction() { return action; }
        public String getRole() { return role; }
    }

    /* 404 Not Found */
    public static class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(int userId) {
            super("No user found with ID: " + userId);
        }
        public UserNotFoundException(String email) {
            super("No user found with email: " + email);
        }
    }

    /* 409 Conflict */
    public static class DuplicateEmailException extends RuntimeException {
        private final String email;
        public DuplicateEmailException(String email) {
            super("Email address is already registered: " + email);
            this.email = email;
        }
        public String getEmail() { return email; }
    }

    /* Acc lifecycle */
    public static class InactiveAccountException extends RuntimeException {
        public InactiveAccountException(String email) {
            super("Account is not active: " + email);
        }
    }

    public static class DeactivatedAccountException extends RuntimeException {
        public DeactivatedAccountException(String email) {
            super("Account has been permanently deactivated and cannot be recovered automatically: " + email + ". Please contact an administrator.");
        }
    }

    public static class InvalidTokenException extends RuntimeException {
        public InvalidTokenException(String reason) {
            super("Password reset token is invalid: " + reason);
        }
    }
}