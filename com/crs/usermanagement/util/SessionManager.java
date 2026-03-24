package com.crs.usermanagement.util;
 
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public final class SessionManager {
 
    private static final Logger LOG = Logger.getLogger(SessionManager.class.getName());

    public static final int SESSION_TTL_MINUTES = 30;

    public static final int MAX_FAILED_ATTEMPTS = 5;

    /* Singleton Instance */
    private static final SessionManager INSTANCE = new SessionManager();
    public static SessionManager getInstance() { return INSTANCE; }
    private SessionManager() {}

    /* State */
    private record SessionEntry(int userId, LocalDateTime expiresAt) {}

    private final Map<String, SessionEntry> sessions = new ConcurrentHashMap<>();

    private final Map<Integer, String> userTokenIndex = new ConcurrentHashMap<>();

    /* Public API */
    public String createSession(int userId) {
        invalidateForUser(userId);
 
        String token = PasswordUtil.generateResetToken();
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(SESSION_TTL_MINUTES);
        sessions.put(token, new SessionEntry(userId, expiry));
        userTokenIndex.put(userId, token);
        LOG.info("Session created for userId=" + userId);
        return token;
    }

    public Optional<Integer> getUserIdForToken(String token) {
        if (token == null) return Optional.empty();
        SessionEntry entry = sessions.get(token);
        if (entry == null) return Optional.empty();
        if (LocalDateTime.now().isAfter(entry.expiresAt())) {
            sessions.remove(token);
            userTokenIndex.remove(entry.userId());
            return Optional.empty();
        }
        return Optional.of(entry.userId());
    }

    public boolean refreshSession(String token) {
        SessionEntry existing = sessions.get(token);
        if (existing == null) return false;
        sessions.put(token, new SessionEntry(existing.userId(),LocalDateTime.now().plusMinutes(SESSION_TTL_MINUTES)));
        return true;
    }

    public void invalidate(String token) {
        SessionEntry entry = sessions.remove(token);
        if (entry != null) {
            userTokenIndex.remove(entry.userId());
            LOG.info("Session invalidated for userId=" + entry.userId());
        }
    }

    public void invalidateForUser(int userId) {
        String token = userTokenIndex.remove(userId);
        if (token != null) sessions.remove(token);
    }

    public boolean isValid(String token) {
        return getUserIdForToken(token).isPresent();
    }

     public int cleanExpired() {
        LocalDateTime now = LocalDateTime.now();
        int[] count = {0};
        sessions.entrySet().removeIf(e -> {
            if (now.isAfter(e.getValue().expiresAt())) {
                userTokenIndex.remove(e.getValue().userId());
                count[0]++;
                return true;
            }
            return false;
        });
        if (count[0] > 0)
            LOG.info("Cleaned " + count[0] + " expired session(s).");
        return count[0];
    }

    public int activeSessionCount() {
        cleanExpired();
        return sessions.size();
    }
}