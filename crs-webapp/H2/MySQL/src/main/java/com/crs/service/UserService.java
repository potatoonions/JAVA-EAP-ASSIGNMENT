package com.crs.service;
 
import com.crs.dao.UserDAO;
import com.crs.entity.User;
import com.crs.exception.*;
import com.crs.RoleManager;
import com.crs.util.PasswordUtil;
import com.crs.util.SessionManager;
 
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
 
public class UserService {
 
    private static final Logger LOG = Logger.getLogger(UserService.class.getName());
 
    // Dependencies
    private final UserDAO userDAO;
    private final RoleManager roleManager;
 
    // Singleton 
    private static final UserService INSTANCE =
        new UserService(UserDAO.getInstance(), RoleManager.getInstance());
 
    public static UserService getInstance() { return INSTANCE; }
 
    public UserService(UserDAO userDAO, RoleManager roleManager) {
        this.userDAO = Objects.requireNonNull(userDAO, "UserDAO must not be null.");
        this.roleManager = Objects.requireNonNull(roleManager, "RoleManager must not be null.");
    }
 
    // Account creation
    public User createUserAccount(String name, String email, String rawPassword, User.Role role) {
        // Input validation
        if (name == null || name.isBlank())
            throw new ValidationException("name", "Name must not be blank.");
        if (!User.isValidEmail(email))
            throw new ValidationException("email", "Email address format is invalid: " + email);
 
        String strengthError = PasswordUtil.validateStrength(rawPassword);
        if (strengthError != null)
            throw new ValidationException("password", strengthError);
 
        if (userDAO.existsByEmail(email))
            throw new DuplicateEmailException(email);
 
        // Build & hash
        User.Role resolvedRole = (role != null) ? role : User.Role.STUDENT;
        String hashedPassword = PasswordUtil.hash(rawPassword);
 
        User user = new User(0, name, email, hashedPassword,
            resolvedRole, User.Status.ACTIVE);
 
        userDAO.addUser(user);
        LOG.info("UserService.createUserAccount — created userId=" + user.getUserId());
        return user;
    }
 
    public User createUserAccount(String name, String email, String rawPassword) {
        return createUserAccount(name, email, rawPassword, User.Role.STUDENT);
    }
 
    // Account updates
    public User updateUserAccount(int userId, String newName, String newEmail, User.Role newRole, User.Status newStatus) {
        User user = userDAO.getUserById(userId);
 
        if (newName != null) {
            if (newName.isBlank())
                throw new ValidationException("name", "Name must not be blank.");
            user.setName(newName);
        }
        if (newEmail != null) {
            if (!User.isValidEmail(newEmail))
                throw new ValidationException("email", "Email format is invalid: " + newEmail);
            user.setEmail(newEmail);
        }
        if (newRole != null) user.setRole(newRole);
        if (newStatus != null) user.setStatus(newStatus);
 
        userDAO.updateUser(user);
        LOG.info("UserService.updateUserAccount — updated userId=" + userId);
        return user;
    }
 
    // Account deactivation & recovery
    public void deactivateAccount(int userId) {
        userDAO.deactivateUser(userId);
        SessionManager.getInstance().invalidateForUser(userId);
        LOG.info("UserService.deactivateAccount — userId=" + userId + " deactivated.");
    }
 
    public void permanentlyDeactivateAccount(int userId) {
        userDAO.permanentlyDeactivateUser(userId);
        SessionManager.getInstance().invalidateForUser(userId);
        LOG.warning("UserService.permanentlyDeactivateAccount — userId=" + userId
            + " permanently deactivated.");
    }
 
    public User recoverAccount(String email) {
        User user = userDAO.getUserByEmail(email);
 
        if (user.getStatus() == User.Status.DEACTIVATED)
            throw new DeactivatedAccountException(email);
        if (user.getStatus() == User.Status.ACTIVE)
            throw new IllegalStateException(
                "Account is already active: " + email);
 
        user.clearFailedAttempts();
        userDAO.activateUser(user.getUserId());
        LOG.info("UserService.recoverAccount — userId=" + user.getUserId()
            + " restored to ACTIVE.");
        return userDAO.getUserById(user.getUserId());
    }
 
    // Password management
    public String resetPassword(String email) {
        User user = userDAO.getUserByEmail(email);
 
        String token = PasswordUtil.generateResetToken();
        LocalDateTime expiry = PasswordUtil.resetTokenExpiry();
 
        userDAO.setResetToken(user.getUserId(), token, expiry);
 
        // Simulate email dispatch
        System.out.printf("[EMAIL SIMULATION] Password reset link sent to %s%n"
            + "  Link: https://crs.university.edu/reset-password?token=%s%n"
            + "  Expires: %s%n", email, token, expiry);
 
        LOG.info("UserService.resetPassword — reset token issued for userId=" + user.getUserId());
        return token;
    }
 
    public void completePasswordReset(String token, String newRawPassword) {
        User user = userDAO.getAllUsers().stream()
            .filter(u -> token.equals(u.getPasswordResetToken()))
            .findFirst()
            .orElseThrow(() -> new InvalidTokenException("token not found"));
 
        if (user.getPasswordResetExpiry() == null
                || LocalDateTime.now().isAfter(user.getPasswordResetExpiry()))
            throw new InvalidTokenException("token has expired");
 
        String strengthError = PasswordUtil.validateStrength(newRawPassword);
        if (strengthError != null)
            throw new ValidationException("password", strengthError);
 
        userDAO.updatePassword(user.getUserId(), PasswordUtil.hash(newRawPassword));
        LOG.info("UserService.completePasswordReset — password updated for userId="
            + user.getUserId());
    }
 
    public String generateTemporaryPassword(int userId) {
        User user = userDAO.getUserById(userId);
        String tempPassword = PasswordUtil.generateTemporaryPassword(12);
        userDAO.updatePassword(userId, PasswordUtil.hash(tempPassword));
        LOG.info("UserService.generateTemporaryPassword — issued for userId=" + userId);
        return tempPassword;
    }
 
    // Role management
    public void assignRole(User requestingUser, int targetUserId, String newRole) {
        roleManager.requirePermission(requestingUser, RoleManager.ROLE_ASSIGN);
        User target = userDAO.getUserById(targetUserId);
        roleManager.assignRole(target, newRole);
        userDAO.updateUser(target);
        LOG.info(String.format(
            "UserService.assignRole — admin %d set userId=%d to role=%s",
            requestingUser.getUserId(), targetUserId, newRole));
    }
 
    // Read operations
 
    public User getUserById(int userId) {
        return userDAO.getUserById(userId);
    }
 
    public User getUserByEmail(String email) {
        return userDAO.getUserByEmail(email);
    }
 
    public List<User> getAllUsers() {
        return userDAO.getAllUsers();
    }
 
    public List<User> getUsersByRole(User.Role role) {
        return userDAO.getUsersByRole(role);
    }
 
    public List<User> getUsersByStatus(User.Status status) {
        return userDAO.getUsersByStatus(status);
    }
 
    // Credential validation (used by AuthController)
    public boolean validateCredentials(String email, String rawPassword) {
        try {
            User user = userDAO.getUserByEmail(email);
            return PasswordUtil.matches(rawPassword, user.getPassword());
        } catch (UserNotFoundException e) {
            return false; 
        }
    }
}