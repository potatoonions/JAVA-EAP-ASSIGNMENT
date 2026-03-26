package com.crs.service;

import com.crs.entity.UserEntity;
import com.crs.exception.UserManagementExceptions.*;
import com.crs.repository.UserRepository;
import com.crs.security.PasswordUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserEntity createUserAccount(String name, String email,
                                        String rawPassword, UserEntity.Role role) {
        if (name == null || name.isBlank())
            throw new ValidationException("name", "Name must not be blank.");
        if (email == null || !email.contains("@"))
            throw new ValidationException("email", "Email format is invalid.");
        String strength = PasswordUtil.validateStrength(rawPassword);
        if (strength != null) throw new ValidationException("password", strength);
        if (userRepository.existsByEmailIgnoreCase(email)) throw new DuplicateEmailException(email);

        String uid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String[] parts = name.trim().split(" ", 2);
        UserEntity u = new UserEntity(uid, parts[0], parts.length > 1 ? parts[1] : parts[0],
            email.toLowerCase(), role != null ? role : UserEntity.Role.STUDENT);
        u.setAccountStatus(UserEntity.AccountStatus.ACTIVE);
        var saved = userRepository.save(u);
        log.info("UserService.createUserAccount — created {}", saved.getUserId());
        return saved;
    }

    public UserEntity createUserAccount(String name, String email, String rawPassword) {
        return createUserAccount(name, email, rawPassword, UserEntity.Role.STUDENT);
    }

    public UserEntity getUserById(String userId) {
        return userRepository.findById(userId.toUpperCase())
            .orElseThrow(() -> new UserNotFoundException("No user found with ID: " + userId));
    }

    public UserEntity getUserByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email)
            .orElseThrow(() -> new UserNotFoundException("No user found with email: " + email));
    }

    public List<UserEntity> getAllUsers() { return userRepository.findAll(); }
    public List<UserEntity> getUsersByRole(UserEntity.Role r) { return userRepository.findByRole(r); }
    public List<UserEntity> getUsersByStatus(UserEntity.AccountStatus s) { return userRepository.findByAccountStatus(s); }

    @Transactional
    public UserEntity updateUserAccount(String userId, String name, String email,
                                        UserEntity.Role role, UserEntity.AccountStatus status) {
        UserEntity u = getUserById(userId);
        if (name   != null) { String[] p = name.trim().split(" ",2); u.setFirstName(p[0]); if(p.length>1) u.setLastName(p[1]); }
        if (email  != null) u.setEmail(email.toLowerCase());
        if (role   != null) u.setRole(role);
        if (status != null) u.setAccountStatus(status);
        return userRepository.save(u);
    }

    @Transactional
    public void deactivateAccount(String userId) {
        UserEntity u = getUserById(userId);
        u.setAccountStatus(UserEntity.AccountStatus.INACTIVE);
        userRepository.save(u);
    }

    @Transactional
    public UserEntity recoverAccount(String email) {
        UserEntity u = getUserByEmail(email);
        if (u.getAccountStatus() == UserEntity.AccountStatus.INACTIVE) {
            u.setAccountStatus(UserEntity.AccountStatus.ACTIVE);
            return userRepository.save(u);
        }
        throw new IllegalStateException("Account cannot be recovered from status: " + u.getAccountStatus());
    }

    @Transactional
    public String resetPassword(String email) {
        getUserByEmail(email); // confirm exists
        String token = UUID.randomUUID().toString().replace("-", "");
        log.info("[PASSWORD RESET SIMULATED] for email={}", email);
        return token;
    }

    @Transactional
    public void completePasswordReset(String token, String newRawPassword) {
        String strength = PasswordUtil.validateStrength(newRawPassword);
        if (strength != null) throw new ValidationException("password", strength);
        log.info("[PASSWORD RESET COMPLETED] simulation mode.");
    }

    public boolean validateCredentials(String email, String rawPassword) {
        try { getUserByEmail(email); return rawPassword != null && rawPassword.length() >= 8; }
        catch (Exception e) { return false; }
    }
}
