package com.crs.usermanagement.security;
 
import com.crs.usermanagement.entity.User;
import com.crs.usermanagement.exception.UserManagementExceptions.*;

import java.nio.file.AccessDeniedException;
import java.util.*;
import java.util.logging.Logger;

public class RoleManager {
 
    private static final Logger LOG = Logger.getLogger(RoleManager.class.getName());

    /* Action constants */
    public static final String USER_CREATE = "USER_CREATE";
    public static final String USER_UPDATE = "USER_UPDATE";
    public static final String USER_DEACTIVATE = "USER_DEACTIVATE";
    public static final String USER_DELETE = "USER_DELETE";
    public static final String USER_VIEW_ALL = "USER_VIEW_ALL";
    public static final String USER_VIEW_SELF = "USER_VIEW_SELF";
    public static final String ROLE_ASSIGN = "ROLE_ASSIGN";
    public static final String RECOVERY_PLAN_CREATE = "RECOVERY_PLAN_CREATE";
    public static final String RECOVERY_PLAN_UPDATE = "RECOVERY_PLAN_UPDATE";
    public static final String RECOVERY_PLAN_VIEW = "RECOVERY_PLAN_VIEW";
    public static final String REPORT_VIEW = "REPORT_VIEW";
    public static final String REPORT_GENERATE = "REPORT_GENERATE";
    public static final String REPORT_EMAIL = "REPORT_EMAIL";
    public static final String ELIGIBILITY_CHECK = "ELIGIBILITY_CHECK";
    public static final String ENROLMENT_APPROVE = "ENROLMENT_APPROVE";
    public static final String EMAIL_NOTIFICATION_SEND = "EMAIL_NOTIFICATION_SEND";
    public static final String SYSTEM_CONFIG = "SYSTEM_CONFIG";

    /* Perms matrix */
    private static final Map<User.Role, Set<String>> PERMISSIONS = new EnumMap<>(User.Role.class);
 
    static {
        PERMISSIONS.put(User.Role.ADMIN, Set.of(
            USER_CREATE, USER_UPDATE, USER_DEACTIVATE, USER_DELETE,
            USER_VIEW_ALL, USER_VIEW_SELF, ROLE_ASSIGN,
            RECOVERY_PLAN_CREATE, RECOVERY_PLAN_UPDATE, RECOVERY_PLAN_VIEW,
            REPORT_VIEW, REPORT_GENERATE, REPORT_EMAIL,
            ELIGIBILITY_CHECK, ENROLMENT_APPROVE,
            EMAIL_NOTIFICATION_SEND, SYSTEM_CONFIG
        ));
 
        PERMISSIONS.put(User.Role.INSTRUCTOR, Set.of(
            USER_VIEW_SELF,
            RECOVERY_PLAN_CREATE, RECOVERY_PLAN_UPDATE, RECOVERY_PLAN_VIEW,
            REPORT_VIEW, REPORT_GENERATE, REPORT_EMAIL,
            ELIGIBILITY_CHECK,
            EMAIL_NOTIFICATION_SEND
        ));
 
        PERMISSIONS.put(User.Role.STUDENT, Set.of(
            USER_VIEW_SELF,
            RECOVERY_PLAN_VIEW,
            REPORT_VIEW
        ));
    }

    /* Singleton */
    private static final RoleManager INSTANCE = new RoleManager();
    public  static RoleManager getInstance() { return INSTANCE; }
    public  RoleManager() {} 

    /* Role assignment */
    public void assignRole(User user, String newRole) {
        Objects.requireNonNull(user,    "User must not be null.");
        Objects.requireNonNull(newRole, "New role must not be null.");
        try {
            User.Role role = User.Role.valueOf(newRole.toUpperCase());
            User.Role oldRole = user.getRole();
            user.setRole(role);
            LOG.info(String.format("RoleManager.assignRole — userId=%d %s → %s", user.getUserId(), oldRole, role));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                "Unknown role: '" + newRole + "'. Valid roles: " + Arrays.toString(User.Role.values()));
        }
    }

    public void assignRole(User user, User.Role newRole) {
        Objects.requireNonNull(user,    "User must not be null.");
        Objects.requireNonNull(newRole, "New role must not be null.");
        User.Role oldRole = user.getRole();
        user.setRole(newRole);
        LOG.info(String.format("RoleManager.assignRole — userId=%d %s → %s", user.getUserId(), oldRole, newRole));
    }

    /* Permission check */
    public boolean checkPermission(User user, String action) {
        if (user == null || action == null) return false;
        Set<String> allowed = PERMISSIONS.getOrDefault(user.getRole(), Set.of());
        boolean permitted = allowed.contains(action.toUpperCase());
        LOG.fine(String.format("RoleManager.checkPermission — userId=%d role=%s action=%s → %b", user.getUserId(), user.getRole(), action, permitted));
        return permitted;
    }

    public void requirePermission(User user, String action) {
        if (!checkPermission(user, action))
            throw new AccessDeniedException(action, user != null ? user.getRole().name() : "null");
    }

    public Set<String> getPermissionsForRole(User.Role role) {
        return PERMISSIONS.getOrDefault(role, Set.of());
    }

    public Set<User.Role> getRolesForAction(String action) {
        Set<User.Role> roles = new HashSet<>();
        for (Map.Entry<User.Role, Set<String>> e : PERMISSIONS.entrySet()) {
            if (e.getValue().contains(action)) roles.add(e.getKey());
        }
        return Collections.unmodifiableSet(roles);
    }

    public boolean isAdmin(User user) {
        return user != null && user.getRole() == User.Role.ADMIN;
    }

    public boolean isInstructor(User user) {
        return user != null && user.getRole() == User.Role.INSTRUCTOR;
    }

    public boolean isStudent(User user) {
        return user != null && user.getRole() == User.Role.STUDENT;
    }
}