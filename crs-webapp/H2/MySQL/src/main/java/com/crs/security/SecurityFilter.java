package com.crs.security;
 
import java.util.Objects;
import java.util.logging.Logger;

public final class SecurityFilter {
 
    private static final Logger LOG = Logger.getLogger(SecurityFilter.class.getName());
 
    private final SecurityConfig securityConfig;
 
    /* Singleton */
    private static final SecurityFilter INSTANCE =
        new SecurityFilter(SecurityConfig.getInstance());
 
    public static SecurityFilter getInstance() { return INSTANCE; }

    /* Constructor */
    public SecurityFilter(SecurityConfig securityConfig) {
        this.securityConfig =
            Objects.requireNonNull(securityConfig, "SecurityConfig required.");
    }

    /*Guard methods */
    public FilterResult guard(String sessionId, String action) {
        if (!securityConfig.isAuthenticated(sessionId)) {
            LOG.warning("SecurityFilter.guard — unauthenticated: action=" + action);
            return FilterResult.denied("You must be logged in to perform this action.");
        }
 
        SecurityConfig.UserPrincipal principal =
            securityConfig.getCurrentUser(sessionId);
 
        if (!securityConfig.checkAccess(principal.getRole(), action)) {
            LOG.warning("SecurityFilter.guard — forbidden: userId="
                + principal.getUserId() + " role=" + principal.getRole()
                + " action=" + action);
            return FilterResult.denied(
                "Your role (" + principal.getRole() + ") is not permitted to: " + action);
        }
 
        LOG.fine("SecurityFilter.guard — allowed: userId=" + principal.getUserId()
            + " action=" + action);
        return FilterResult.allowed(principal);
    }

    public FilterResult requireAuthentication(String sessionId) {
        if (!securityConfig.isAuthenticated(sessionId))
            return FilterResult.denied("Authentication required.");
        return FilterResult.allowed(securityConfig.getCurrentUser(sessionId));
    }

    public FilterResult guardSelfOrAdmin(String sessionId, int targetUserId, String adminAction) {
        FilterResult auth = requireAuthentication(sessionId);
        if (!auth.isAllowed()) return auth;
 
        SecurityConfig.UserPrincipal principal = auth.getPrincipal();
        boolean isSelf  = principal.getUserId() == targetUserId;
        boolean isAdmin = securityConfig.checkAccess(principal.getRole(), adminAction);
 
        if (!isSelf && !isAdmin)
            return FilterResult.denied(
                "You may only access your own records unless you are an administrator.");
 
        return FilterResult.allowed(principal);
    }

    /* FilterResult value object */
    public static final class FilterResult {
        private final boolean allowed;
        private final String reason;
        private final SecurityConfig.UserPrincipal principal;
 
        private FilterResult(boolean allowed, String reason, SecurityConfig.UserPrincipal principal) {
            this.allowed = allowed;
            this.reason = reason;
            this.principal = principal;
        }
 
        static FilterResult allowed(SecurityConfig.UserPrincipal principal) {
            return new FilterResult(true, null, principal);
        }
 
        static FilterResult denied(String reason) {
            return new FilterResult(false, reason, null);
        }

        public boolean isAllowed() { return allowed; }

        public String getReason() { return reason; }

        public SecurityConfig.UserPrincipal getPrincipal() { return principal; }

        @Override
        public String toString() {
            return allowed
                ? "FilterResult{ALLOWED, userId=" + principal.getUserId() + "}"
                : "FilterResult{DENIED, reason='" + reason + "'}";
        }
    }
}