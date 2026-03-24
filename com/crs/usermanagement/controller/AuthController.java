package com.crs.usermanagement.controller;

import com.crs.usermanagement.dao.UserDAO;
import com.crs.usermanagement.dto.DTOs.*;
import com.crs.usermanagement.entity.User;
import com.crs.usermanagement.exception.UserManagementExceptions.*;
import com.crs.usermanagement.service.UserService;
import com.crs.usermanagement.util.SessionManager;

import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

public class AuthController {

    private static final Logger LOG = Logger.getLogger(AuthController.class.getName());

    /* Dependencies */
    private final UserService userService;
    private final SessionManager sessionManager;
    private final UserDAO userDAO; 

    /* Singleton instance */
    private static final AuthController INSTANCE = new AuthController(
        UserService.getInstance(),
        SessionManager.getInstance(),
        UserDAO.getInstance());

    public static AuthController getInstance() { return INSTANCE; }

    /** Dependency-injection constructor */
    public AuthController(UserService userService, SessionManager sessionManager, UserDAO userDAO) {
        this.userService = Objects.requireNonNull(userService, "UserService required.");
        this.sessionManager = Objects.requireNonNull(sessionManager, "SessionManager required.");
        this.userDAO = Objects.requireNonNull(userDAO, "UserDAO required.");
    }

    /* Login */
    public AuthResponse login(String email, String password) {
        LOG.info("AuthController.login — attempt for email=" + email);

        if (email == null || email.isBlank())
            return AuthResponse.fail("Email address must not be blank.");
        if (password == null || password.isBlank())
            return AuthResponse.fail("Password must not be blank.");

        User user;
        try {
            user = userService.getUserByEmail(email);
        } catch (UserNotFoundException e) {
            LOG.warning("AuthController.login — unknown email: " + email);
            return AuthResponse.fail("Invalid email or password.");
        }

        if (user.getStatus() == User.Status.DEACTIVATED) {
            LOG.warning("AuthController.login — login rejected: account DEACTIVATED userId=" + user.getUserId());
            return AuthResponse.fail(
                "This account has been permanently deactivated. " + "Please contact support.");
        }

        if (user.getStatus() == User.Status.INACTIVE) {
            int attempts = user.getFailedLoginAttempts();
            if (attempts >= SessionManager.MAX_FAILED_ATTEMPTS) {
                LOG.warning("AuthController.login — login rejected: account LOCKED userId=" + user.getUserId());
                return AuthResponse.fail(
                    "Account is locked after " + attempts + " failed attempt(s). " + "Please reset your password or contact support.");
            }
            LOG.warning("AuthController.login — login rejected: account INACTIVE userId=" + user.getUserId());
            return AuthResponse.fail(
                "Account is inactive. Please contact an administrator to restore access.");
        }

        if (user.getStatus() == User.Status.PENDING_VERIFICATION) {
            return AuthResponse.fail(
                "Account is pending email verification. " + "Please check your inbox.");
        }

        boolean credentialsValid = validateCredentials(email, password);

        if (!credentialsValid) {
            userDAO.recordFailedLogin(user.getUserId());
            int remaining = SessionManager.MAX_FAILED_ATTEMPTS
                - userDAO.getUserById(user.getUserId()).getFailedLoginAttempts();
            String msg = remaining > 0
                ? "Invalid email or password. " + remaining + " attempt(s) remaining."
                : "Account has been locked due to too many failed login attempts.";
            LOG.warning("AuthController.login — invalid credentials for userId=" + user.getUserId());
            return AuthResponse.fail(msg);
        }

        userDAO.recordSuccessfulLogin(user.getUserId());
        String token = sessionManager.createSession(user.getUserId());

        LOG.info("AuthController.login — SUCCESS userId=" + user.getUserId() + ", token=" + token.substring(0, 8) + "…");

        return AuthResponse.ok(token, new UserResponse(user));
    }

    /* Logout */
    public boolean logout(int userId) {
        boolean hadSession = sessionManager.isValid(
            sessionManager.activeSessionCount() > 0 ? "" : "");
        sessionManager.invalidateForUser(userId);
        LOG.info("AuthController.logout — userId=" + userId + " logged out.");
        return true;
    }


    public boolean logoutByToken(String sessionToken) {
        if (sessionToken == null || sessionToken.isBlank()) return false;
        Optional<Integer> userId = sessionManager.getUserIdForToken(sessionToken);
        sessionManager.invalidate(sessionToken);
        userId.ifPresent(id -> LOG.info("AuthController.logoutByToken — userId=" + id + " logged out."));
        return userId.isPresent();
    }

    /* Credential validation */
    public boolean validateCredentials(String email, String password) {
        return userService.validateCredentials(email, password);
    }

    /* Session util */
    public Optional<Integer> getUserIdFromSession(String sessionToken) {
        return sessionManager.getUserIdForToken(sessionToken);
    }


    public boolean isSessionValid(String sessionToken) {
        return sessionManager.isValid(sessionToken);
    }


    public boolean refreshSession(String sessionToken) {
        return sessionManager.refreshSession(sessionToken);
    }
}