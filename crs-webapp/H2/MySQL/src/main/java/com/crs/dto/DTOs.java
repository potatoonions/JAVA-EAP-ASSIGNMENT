package com.crs.usermanagement.dto;

import com.crs.usermanagement.entity.User;

import java.time.LocalDateTime;

public final class DTOs {

    private DTOs() {}

    // CreateUserRequest – inbound payload for POST /users

    public static class CreateUserRequest {
        private String name;
        private String email;
        private String password;
        private String role;

        public CreateUserRequest() {}
        public CreateUserRequest(String name, String email, String password, String role) {
            this.name = name;
            this.email = email;
            this.password = password;
            this.role = role;
        }

        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getPassword() { return password; }
        public String getRole() { return role; }

        public void setName(String name) { this.name = name; }
        public void setEmail(String email) { this.email = email; }
        public void setPassword(String password) { this.password = password; }
        public void setRole(String role) { this.role = role; }
    }

    // UpdateUserRequest – inbound payload for PUT /users/{id}

    public static class UpdateUserRequest {
        private String name;
        private String email;
        private String role;
        private String status;

        public UpdateUserRequest() {}

        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getRole() { return role; }
        public String getStatus() { return status; }

        public void setName(String name) { this.name = name; }
        public void setEmail(String email) { this.email = email; }
        public void setRole(String role) { this.role = role; }
        public void setStatus(String status) { this.status = status; }
    }

    // UserResponse – outbound user payload (no password hash)

    public static class UserResponse {
        private final int userId;
        private final String name;
        private final String email;
        private final String role;
        private final String status;
        private final LocalDateTime createdAt;
        private final LocalDateTime lastLoginAt;

        public UserResponse(User user) {
            this.userId = user.getUserId();
            this.name = user.getName();
            this.email = user.getEmail();
            this.role = user.getRole().name();
            this.status = user.getStatus().name();
            this.createdAt = user.getCreatedAt();
            this.lastLoginAt = user.getLastLoginAt();
        }

        public int getUserId() { return userId; }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getRole() { return role; }
        public String getStatus() { return status; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public LocalDateTime getLastLoginAt(){ return lastLoginAt; }

        @Override
        public String toString() {
            return String.format(
                "UserResponse{userId=%d, name='%s', email='%s', role=%s, status=%s}",
                userId, name, email, role, status);
        }
    }

    // LoginRequest – inbound payload for POST /auth/login

    public static class LoginRequest {
        private String email;
        private String password;

        public LoginRequest() {}
        public LoginRequest(String email, String password) {
            this.email = email;
            this.password = password;
        }

        public String getEmail() { return email; }
        public String getPassword() { return password; }
        public void setEmail(String email) { this.email = email; }
        public void setPassword(String password) { this.password = password; }
    }

    // AuthResponse – outbound payload after login / logout
    public static class AuthResponse {
        private final boolean success;
        private final String message;
        private final String sessionToken;
        private final UserResponse user;

        public AuthResponse(boolean success, String message, String sessionToken, UserResponse user) {
            this.success = success;
            this.message = message;
            this.sessionToken = sessionToken;
            this.user = user;
        }

        /** successful login */
        public static AuthResponse ok(String sessionToken, UserResponse user) {
            return new AuthResponse(true, "Login successful.", sessionToken, user);
        }

        /** failed login */
        public static AuthResponse fail(String reason) {
            return new AuthResponse(false, reason, null, null);
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public String getSessionToken() { return sessionToken; }
        public UserResponse getUser() { return user; }

        @Override
        public String toString() {
            return String.format("AuthResponse{success=%b, message='%s'}", success, message);
        }
    }

    //  ApiResponse<T>

    public static class ApiResponse<T> {
        private final int statusCode;
        private final String message;
        private final T data;

        public ApiResponse(int statusCode, String message, T data) {
            this.statusCode = statusCode;
            this.message = message;
            this.data = data;
        }

        public static <T> ApiResponse<T> ok(String msg, T data) { return new ApiResponse<>(200, msg, data); }
        public static <T> ApiResponse<T> created(String msg, T data) { return new ApiResponse<>(201, msg, data); }
        public static <T> ApiResponse<T> badRequest(String msg) { return new ApiResponse<>(400, msg, null); }
        public static <T> ApiResponse<T> unauthorized(String msg) { return new ApiResponse<>(401, msg, null); }
        public static <T> ApiResponse<T> forbidden(String msg) { return new ApiResponse<>(403, msg, null); }
        public static <T> ApiResponse<T> notFound(String msg) { return new ApiResponse<>(404, msg, null); }
        public static <T> ApiResponse<T> conflict(String msg) { return new ApiResponse<>(409, msg, null); }
        public static <T> ApiResponse<T> error(String msg) { return new ApiResponse<>(500, msg, null); }

        public int getStatusCode() { return statusCode; }
        public String getMessage() { return message; }
        public T getData() { return data; }

        @Override
        public String toString() {
            return String.format("ApiResponse{status=%d, message='%s'}", statusCode, message);
        }
    }
}