package com.crs.usermanagement.controller;
 
import com.crs.usermanagement.dto.DTOs.*;
import com.crs.usermanagement.entity.User;
import com.crs.usermanagement.exception.UserManagementExceptions.*;
import com.crs.usermanagement.security.RoleManager;
import com.crs.usermanagement.service.UserService;

import controller.ApiResponse;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

public class UserController {
 
    private static final Logger LOG = Logger.getLogger(UserController.class.getName());
 
    private final UserService userService;
    private final RoleManager roleManager;

    /* Singleton instance */
    private static final UserController INSTANCE = new UserController(UserService.getInstance(), RoleManager.getInstance());
 
    public static UserController getInstance() { return INSTANCE; }
 
    public UserController(UserService userService, RoleManager roleManager) {
        this.userService = Objects.requireNonNull(userService, "UserService required.");
        this.roleManager = Objects.requireNonNull(roleManager, "RoleManager required.");
    }

    /* POST /users - create account */
    public ApiResponse<UserResponse> createUser(CreateUserRequest request) {
        LOG.info("UserController.createUser — email=" + (request != null ? request.getEmail() : "null"));
        if (request == null) return ApiResponse.badRequest("Request body must not be null.");
        try {
            User.Role role = parseRole(request.getRole(), User.Role.STUDENT);
            User created = userService.createUserAccount(request.getName(), request.getEmail(), request.getPassword(), role);
            return ApiResponse.created("User account created successfully.",
                new UserResponse(created));
        } catch (ValidationException e) { return ApiResponse.badRequest(e.getMessage()); }
          catch (DuplicateEmailException e) { return ApiResponse.conflict(e.getMessage()); }
          catch (Exception e) {
              LOG.severe("UserController.createUser error: " + e.getMessage());
              return ApiResponse.error("An unexpected error occurred.");
          }
    }

    /* GET /users/{userId} - get user details */
    public ApiResponse<UserResponse> getUserById(User requestingUser, int targetUserId) {
        boolean isSelf = requestingUser != null && requestingUser.getUserId() == targetUserId;
        boolean canViewAll = roleManager.checkPermission(requestingUser, RoleManager.USER_VIEW_ALL);
 
        if (!isSelf && !canViewAll)
            return ApiResponse.forbidden(
                "You do not have permission to view this user's profile.");
 
        try {
            return ApiResponse.ok("User retrieved.", new UserResponse(userService.getUserById(targetUserId)));
        } catch (UserNotFoundException e) { return ApiResponse.notFound(e.getMessage()); }
    }

    /* GET /users/email/{email} - get user by email */
    public ApiResponse<UserResponse> getUserByEmail(User requestingUser, String email) {
        if (!roleManager.checkPermission(requestingUser, RoleManager.USER_VIEW_ALL))
            return ApiResponse.forbidden("Insufficient permissions.");
        try {
            return ApiResponse.ok("User retrieved.", new UserResponse(
                userService.getUserByEmail(email)));
        } catch (UserNotFoundException e) { return ApiResponse.notFound(e.getMessage()); }
    }

    /* GET /users - list all users */
    public ApiResponse<List<UserResponse>> getAllUsers(User requestingUser) {
        if (!roleManager.checkPermission(requestingUser, RoleManager.USER_VIEW_ALL))
            return ApiResponse.forbidden("Only administrators may list all users.");
        List<UserResponse> users = userService.getAllUsers().stream().map(UserResponse::new).toList();
        return ApiResponse.ok(users.size() + " user(s) found.", users);
    }

    /* PUT /users/{userId} - update user details */
    public ApiResponse<UserResponse> updateUser(User requestingUser, int targetUserId, UpdateUserRequest request) {
        boolean isSelf = requestingUser != null && requestingUser.getUserId() == targetUserId;
        boolean canAdmin = roleManager.checkPermission(requestingUser, RoleManager.USER_UPDATE);
 
        if (!isSelf && !canAdmin)
            return ApiResponse.forbidden("You may only update your own profile.");
 
        if (request.getRole() != null && !canAdmin)
            return ApiResponse.forbidden("Only administrators may change user roles.");
 
        try {
            User.Role newRole = parseRole(request.getRole(), null);
            User.Status newStatus = parseStatus(request.getStatus(), null);
            User updated = userService.updateUserAccount(targetUserId, request.getName(), request.getEmail(), newRole, newStatus);
            return ApiResponse.ok("User updated successfully.", new UserResponse(updated));
        } catch (ValidationException e) { return ApiResponse.badRequest(e.getMessage()); }
          catch (DuplicateEmailException e) { return ApiResponse.conflict(e.getMessage()); }
          catch (UserNotFoundException e) { return ApiResponse.notFound(e.getMessage()); }
          catch (Exception e) {
              LOG.severe("UserController.updateUser error: " + e.getMessage());
              return ApiResponse.error("An unexpected error occurred.");
          }
    }

    /* POST /users/{userId}/deactivate */
    public ApiResponse<Void> deactivateUser(User requestingUser, int targetUserId) {
        if (!roleManager.checkPermission(requestingUser, RoleManager.USER_DEACTIVATE))
            return ApiResponse.forbidden("Only administrators may deactivate accounts.");
        try {
            userService.deactivateAccount(targetUserId);
            return ApiResponse.ok("Account deactivated (userId=" + targetUserId + ").", null);
        } catch (UserNotFoundException e) { return ApiResponse.notFound(e.getMessage()); }
    }

    /* POST /users/{userId}/recover */
    public ApiResponse<UserResponse> recoverAccount(String email) {
        try {
            User recovered = userService.recoverAccount(email);
            return ApiResponse.ok("Account recovered successfully.", new UserResponse(recovered));
        } catch (UserNotFoundException e) { return ApiResponse.notFound(e.getMessage()); }
          catch (DeactivatedAccountException e) { return ApiResponse.forbidden(e.getMessage()); }
          catch (IllegalStateException e) { return ApiResponse.badRequest(e.getMessage()); }
    }

    /* POST /auth/reset-password */
    public ApiResponse<String> resetPassword(String email) {
        if (email == null || email.isBlank())
            return ApiResponse.badRequest("Email must not be blank.");
        try {
            String token = userService.resetPassword(email);
            return ApiResponse.ok("Password reset email dispatched [SIMULATED].", token);
        } catch (UserNotFoundException e) {
            return ApiResponse.ok("If that email exists, a reset link has been sent.", null);
        }
    }

    /* POST /auth/reset-password/complete */
    public ApiResponse<Void> completePasswordReset(String token, String newRawPassword) {
        try {
            userService.completePasswordReset(token, newRawPassword);
            return ApiResponse.ok("Password has been reset successfully.", null);
        } catch (InvalidTokenException e) { return ApiResponse.badRequest(e.getMessage()); }
          catch (ValidationException e) { return ApiResponse.badRequest(e.getMessage()); }
    }

    /* POST /users/{userId}/role */
    public ApiResponse<Void> assignRole(User requestingUser, int targetUserId, String newRole) {
        try {
            userService.assignRole(requestingUser, targetUserId, newRole);
            return ApiResponse.ok("Role '" + newRole + "' assigned to userId=" + targetUserId + ".", null);
        } catch (AccessDeniedException e) { return ApiResponse.forbidden(e.getMessage()); }
          catch (UserNotFoundException e) { return ApiResponse.notFound(e.getMessage()); }
          catch (IllegalArgumentException e){ return ApiResponse.badRequest(e.getMessage()); }
    }

    /* DELETE /users/{userId} - hard delete */
    public ApiResponse<Void> deleteUser(User requestingUser, int targetUserId) {
        if (!roleManager.checkPermission(requestingUser, RoleManager.USER_DELETE))
            return ApiResponse.forbidden("Only administrators may delete user records.");
        try {
            userService.permanentlyDeactivateAccount(targetUserId);
            return ApiResponse.ok("User permanently deactivated (userId=" + targetUserId + ").", null);
        } catch (UserNotFoundException e) { return ApiResponse.notFound(e.getMessage()); }
    }

    /* GET /users/{userId}/permissions/{action} */
    public ApiResponse<Boolean> checkPermission(User requestingUser, String action) {
        boolean permitted = roleManager.checkPermission(requestingUser, action);
        String msg = permitted
            ? "Permission GRANTED for action: " + action
            : "Permission DENIED for action: "  + action;
        return ApiResponse.ok(msg, permitted);
    }

    /* Private helper methods */
    private User.Role parseRole(String roleStr, User.Role defaultValue) {
        if (roleStr == null || roleStr.isBlank()) return defaultValue;
        try {
            return User.Role.valueOf(roleStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("role", "Unknown role: '" + roleStr + "'.");
        }
    }
 
    private User.Status parseStatus(String statusStr, User.Status defaultValue) {
        if (statusStr == null || statusStr.isBlank()) return defaultValue;
        try {
            return User.Status.valueOf(statusStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("status", "Unknown status: '" + statusStr + "'.");
        }
    }
}