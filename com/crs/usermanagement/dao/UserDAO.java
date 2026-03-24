package com.crs.usermanagement.dao;
 
import com.crs.usermanagement.entity.User;
import com.crs.usermanagement.exception.UserManagementExceptions.*;
 
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class UserDAO {
 
    private static final Logger LOG = Logger.getLogger(UserDAO.class.getName());

    private final Map<Integer, User> store = new ConcurrentHashMap<>();

    private final AtomicInteger idSequence = new AtomicInteger(1000);

    /* Singleton */
    private static final UserDAO INSTANCE = new UserDAO();
    public  static UserDAO getInstance() { return INSTANCE; }
    private UserDAO() {}

    /* CREATE */
    public void addUser(User user) {
        Objects.requireNonNull(user, "User must not be null.");
 
        if (findByEmailInternal(user.getEmail()).isPresent())
            throw new DuplicateEmailException(user.getEmail());
 
        if (user.getUserId() == 0)
            user.setUserId(idSequence.getAndIncrement());
 
        store.put(user.getUserId(), user);
        LOG.info("UserDAO.addUser — saved userId=" + user.getUserId());
    }

    /* READ */
    public User getUserById(int userId) {
        User user = store.get(userId);
        if (user == null)
            throw new UserNotFoundException(userId);
        return user;
    }

    public User getUserByEmail(String email) {
        if (email == null || email.isBlank())
            throw new IllegalArgumentException("Email must not be blank.");
        return findByEmailInternal(email)
            .orElseThrow(() -> new UserNotFoundException(email));
    }

    public boolean existsByEmail(String email) {
        return findByEmailInternal(email).isPresent();
    }

    public List<User> getAllUsers() {
        return Collections.unmodifiableList(new ArrayList<>(store.values()));
    }

    public List<User> getUsersByRole(User.Role role) {
        Objects.requireNonNull(role, "Role must not be null.");
        return store.values().stream()
            .filter(u -> u.getRole() == role)
            .collect(Collectors.toUnmodifiableList());
    }

    public List<User> getUsersByStatus(User.Status status) {
        Objects.requireNonNull(status, "Status must not be null.");
        return store.values().stream()
            .filter(u -> u.getStatus() == status)
            .collect(Collectors.toUnmodifiableList());
    }

    /* UPDATE */
    public void updateUser(User user) {
        Objects.requireNonNull(user, "User must not be null.");
 
        if (!store.containsKey(user.getUserId()))
            throw new UserNotFoundException(user.getUserId());

        User existing = store.get(user.getUserId());
        if (!existing.getEmail().equalsIgnoreCase(user.getEmail())) {
            if (findByEmailInternal(user.getEmail()).isPresent())
                throw new DuplicateEmailException(user.getEmail());
        }
 
        store.put(user.getUserId(), user);
        LOG.info("UserDAO.updateUser — updated userId=" + user.getUserId());
    }

    public void updatePassword(int userId, String hashedPassword) {
        User user = getUserById(userId);
        user.setPassword(hashedPassword);
        user.setPasswordResetToken(null);
        user.setPasswordResetExpiry(null);
        store.put(userId, user);
        LOG.info("UserDAO.updatePassword — password updated for userId=" + userId);
    }

    public void setResetToken(int userId, String token,
                              java.time.LocalDateTime expiry) {
        User user = getUserById(userId);
        user.setPasswordResetToken(token);
        user.setPasswordResetExpiry(expiry);
        store.put(userId, user);
    }

    /* CHANGE STATUS */
    public void deactivateUser(int userId) {
        User user = getUserById(userId);
        user.setStatus(User.Status.INACTIVE);
        store.put(userId, user);
        LOG.info("UserDAO.deactivateUser — userId=" + userId + " → INACTIVE");
    }

    public void permanentlyDeactivateUser(int userId) {
        User user = getUserById(userId);
        user.setStatus(User.Status.DEACTIVATED);
        store.put(userId, user);
        LOG.info("UserDAO.permanentlyDeactivateUser — userId=" + userId + " → DEACTIVATED");
    }

    public void activateUser(int userId) {
        User user = getUserById(userId);
        user.setStatus(User.Status.ACTIVE);
        store.put(userId, user);
        LOG.info("UserDAO.activateUser — userId=" + userId + " → ACTIVE");
    }

    public void recordFailedLogin(int userId) {
        User user = getUserById(userId);
        user.incrementFailedAttempts();
        if (user.getFailedLoginAttempts()
                >= com.crs.usermanagement.util.SessionManager.MAX_FAILED_ATTEMPTS) {
            user.setStatus(User.Status.INACTIVE);
            LOG.warning("UserDAO.recordFailedLogin — account locked for userId=" + userId);
        }
        store.put(userId, user);
    }

    public void recordSuccessfulLogin(int userId) {
        User user = getUserById(userId);
        user.clearFailedAttempts();
        user.setLastLoginAt(java.time.LocalDateTime.now());
        store.put(userId, user);
    }

    /* DELETE */
    public void deleteUser(int userId) {
        if (!store.containsKey(userId))
            throw new UserNotFoundException(userId);
        store.remove(userId);
        LOG.warning("UserDAO.deleteUser — permanently removed userId=" + userId);
    }

    public int count() { return store.size(); }

    /* Private helper methods */
    private Optional<User> findByEmailInternal(String email) {
        String normalised = email.trim().toLowerCase();
        return store.values().stream()
            .filter(u -> u.getEmail().equalsIgnoreCase(normalised))
            .findFirst();
    }
}