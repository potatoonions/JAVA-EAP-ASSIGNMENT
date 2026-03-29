package com.crs.security;

import com.crs.util.LoggerUtil;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SecurityConfig {

    // Role constants 

    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_INSTRUCTOR = "INSTRUCTOR";
    public static final String ROLE_STUDENT = "STUDENT";

    // Action constants

    public static final String ACTION_USER_CREATE = "USER_CREATE";
    public static final String ACTION_USER_UPDATE = "USER_UPDATE";
    public static final String ACTION_USER_DEACTIVATE = "USER_DEACTIVATE";
    public static final String ACTION_USER_DELETE = "USER_DELETE";
    public static final String ACTION_USER_VIEW_ALL = "USER_VIEW_ALL";
    public static final String ACTION_USER_VIEW_SELF = "USER_VIEW_SELF";
    public static final String ACTION_ROLE_ASSIGN = "ROLE_ASSIGN";
    public static final String ACTION_RECOVERY_PLAN_CREATE = "RECOVERY_PLAN_CREATE";
    public static final String ACTION_RECOVERY_PLAN_UPDATE = "RECOVERY_PLAN_UPDATE";
    public static final String ACTION_RECOVERY_PLAN_VIEW = "RECOVERY_PLAN_VIEW";
    public static final String ACTION_REPORT_VIEW = "REPORT_VIEW";
    public static final String ACTION_REPORT_GENERATE = "REPORT_GENERATE";
    public static final String ACTION_REPORT_EMAIL = "REPORT_EMAIL";
    public static final String ACTION_ELIGIBILITY_CHECK = "ELIGIBILITY_CHECK";
    public static final String ACTION_ENROLMENT_APPROVE = "ENROLMENT_APPROVE";
    public static final String ACTION_EMAIL_SEND = "EMAIL_NOTIFICATION_SEND";
    public static final String ACTION_SYSTEM_CONFIG = "SYSTEM_CONFIG";

    // Permission matrix

    private static final Map<String, Set<String>> PERMISSION_MATRIX;

    static {
        PERMISSION_MATRIX = new HashMap<>();

        PERMISSION_MATRIX.put(ROLE_ADMIN, Set.of(
            ACTION_USER_CREATE, ACTION_USER_UPDATE, ACTION_USER_DEACTIVATE,
            ACTION_USER_DELETE, ACTION_USER_VIEW_ALL, ACTION_USER_VIEW_SELF,
            ACTION_ROLE_ASSIGN, ACTION_RECOVERY_PLAN_CREATE,
            ACTION_RECOVERY_PLAN_UPDATE, ACTION_RECOVERY_PLAN_VIEW,
            ACTION_REPORT_VIEW, ACTION_REPORT_GENERATE, ACTION_REPORT_EMAIL,
            ACTION_ELIGIBILITY_CHECK, ACTION_ENROLMENT_APPROVE,
            ACTION_EMAIL_SEND, ACTION_SYSTEM_CONFIG
        ));

        PERMISSION_MATRIX.put(ROLE_INSTRUCTOR, Set.of(
            ACTION_USER_VIEW_SELF,
            ACTION_RECOVERY_PLAN_CREATE, ACTION_RECOVERY_PLAN_UPDATE,
            ACTION_RECOVERY_PLAN_VIEW,
            ACTION_REPORT_VIEW, ACTION_REPORT_GENERATE, ACTION_REPORT_EMAIL,
            ACTION_ELIGIBILITY_CHECK, ACTION_EMAIL_SEND
        ));

        PERMISSION_MATRIX.put(ROLE_STUDENT, Set.of(
            ACTION_USER_VIEW_SELF,
            ACTION_RECOVERY_PLAN_VIEW,
            ACTION_REPORT_VIEW
        ));
    }

    // Configuration constants

    public static final int SESSION_TIMEOUT_MINUTES = 30;

    public static final int MAX_FAILED_LOGIN_ATTEMPTS = 5;

    public static final int MIN_PASSWORD_LENGTH = 8;

    // Singleton 

    private static final SecurityConfig INSTANCE = new SecurityConfig();
    public  static SecurityConfig getInstance()  { return INSTANCE; }

    // Session store

    private final Map<String, Session> sessionStore = new ConcurrentHashMap<>();

    private final Map<Integer, String> userSessionIndex = new ConcurrentHashMap<>();

    // Session management
    
    public String loginUser(UserPrincipal principal) {
        if (principal == null)
            throw new IllegalArgumentException("UserPrincipal must not be null.");

        // Invalidate any existing session for this user
        logoutUser(principal.getUserId());

        String sessionId = generateSessionId();
        Session session  = new Session(sessionId, principal,
            LocalDateTime.now().plusMinutes(SESSION_TIMEOUT_MINUTES));

        sessionStore.put(sessionId, session);
        userSessionIndex.put(principal.getUserId(), sessionId);

        LoggerUtil.getInstance().logInfo(
            "SecurityConfig.loginUser — userId=" + principal.getUserId()
            + " role=" + principal.getRole() + " sessionId=" + mask(sessionId));

        return sessionId;
    }

    public void logoutUser(String sessionId) {
        Session session = sessionStore.remove(sessionId);
        if (session != null) {
            userSessionIndex.remove(session.getPrincipal().getUserId());
            LoggerUtil.getInstance().logInfo(
                "SecurityConfig.logoutUser — userId="
                + session.getPrincipal().getUserId() + " logged out.");
        }
    }

    public void logoutUser(int userId) {
        String sessionId = userSessionIndex.remove(userId);
        if (sessionId != null) sessionStore.remove(sessionId);
    }

    // Authentication

    public boolean isAuthenticated(String sessionId) {
        if (sessionId == null) return false;
        Session session = sessionStore.get(sessionId);
        if (session == null) return false;
        if (session.isExpired()) {
            sessionStore.remove(sessionId);
            userSessionIndex.remove(session.getPrincipal().getUserId());
            return false;
        }
        session.extendExpiry(SESSION_TIMEOUT_MINUTES);
        return true;
    }

    public UserPrincipal getCurrentUser(String sessionId) {
        if (!isAuthenticated(sessionId)) return null;
        return sessionStore.get(sessionId).getPrincipal();
    }

    // Authorisation

    public boolean checkAccess(String role, String action) {
        if (role == null || action == null) return false;
        Set<String> permitted = PERMISSION_MATRIX.getOrDefault(
            role.toUpperCase(), Collections.emptySet());
        boolean allowed = permitted.contains(action.toUpperCase());
        LoggerUtil.getInstance().logInfo(
            "SecurityConfig.checkAccess — role=" + role
            + " action=" + action + " → " + (allowed ? "GRANTED" : "DENIED"));
        return allowed;
    }

    public boolean checkAccess(String sessionId, String action) {
        UserPrincipal principal = getCurrentUser(sessionId);
        if (principal == null) {
            LoggerUtil.getInstance().logWarning(
                "SecurityConfig.checkAccess — unauthenticated access attempt: " + action);
            return false;
        }
        return checkAccess(principal.getRole(), action);
    }

    public Set<String> getPermittedActions(String role) {
        return Collections.unmodifiableSet(
            PERMISSION_MATRIX.getOrDefault(
                role != null ? role.toUpperCase() : "", Collections.emptySet()));
    }

    // Session housekeeping

    public int cleanExpiredSessions() {
        List<String> expired = sessionStore.entrySet().stream()
            .filter(e -> e.getValue().isExpired())
            .map(Map.Entry::getKey)
            .toList();
        expired.forEach(id -> {
            Session s = sessionStore.remove(id);
            if (s != null) userSessionIndex.remove(s.getPrincipal().getUserId());
        });
        if (!expired.isEmpty())
            LoggerUtil.getInstance().logInfo(
                "SecurityConfig.cleanExpiredSessions — removed " + expired.size() + " session(s).");
        return expired.size();
    }

    public int activeSessionCount() {
        cleanExpiredSessions();
        return sessionStore.size();
    }

    // Private helpers

    private String generateSessionId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /** Returns the first 8 characters of a session ID for safe log output. */
    private String mask(String id) {
        return id.length() > 8 ? id.substring(0, 8) + "…" : id;
    }

    // Inner value objects

    public static final class UserPrincipal {
        private final int userId;
        private final String name;
        private final String email;
        private final String role;

        public UserPrincipal(int userId, String name, String email, String role) {
            if (userId <= 0) throw new IllegalArgumentException("userId must be positive.");
            if (name == null || name.isBlank()) throw new IllegalArgumentException("name required.");
            if (email == null || email.isBlank()) throw new IllegalArgumentException("email required.");
            if (role == null || role.isBlank()) throw new IllegalArgumentException("role required.");
            this.userId = userId;
            this.name = name.trim();
            this.email = email.trim().toLowerCase();
            this.role = role.trim().toUpperCase();
        }

        public int getUserId() { return userId; }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getRole() { return role; }

        public boolean isAdmin() { return ROLE_ADMIN.equals(role); }
        public boolean isInstructor() { return ROLE_INSTRUCTOR.equals(role); }
        public boolean isStudent() { return ROLE_STUDENT.equals(role); }

        @Override
        public String toString() {
            return String.format("UserPrincipal{userId=%d, email='%s', role=%s}",
                userId, email, role);
        }
    }

    private static final class Session {
        private final String sessionId;
        private final UserPrincipal principal;
        private LocalDateTime expiresAt;

        Session(String sessionId, UserPrincipal principal, LocalDateTime expiresAt) {
            this.sessionId = sessionId;
            this.principal = principal;
            this.expiresAt = expiresAt;
        }

        UserPrincipal getPrincipal() { return principal; }

        boolean isExpired() { return LocalDateTime.now().isAfter(expiresAt); }

        void extendExpiry(int minutes) {
            this.expiresAt = LocalDateTime.now().plusMinutes(minutes);
        }
    }
}
