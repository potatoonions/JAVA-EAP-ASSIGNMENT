package com.crs.usermanagement.util;
 
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.logging.Logger;

public final class PasswordUtil {
 
    private static final Logger LOG = Logger.getLogger(PasswordUtil.class.getName());

    private static final String UPPER   = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER   = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS  = "0123456789";
    private static final String SPECIAL = "!@#$%^&*";
    private static final String ALL     = UPPER + LOWER + DIGITS + SPECIAL;

    private static final SecureRandom RANDOM = new SecureRandom();
 
    private PasswordUtil() {}

    /* Hashing */
    public static String hash(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank())
            throw new IllegalArgumentException("Password to hash must not be blank.");
 
        byte[] saltBytes = new byte[16];
        RANDOM.nextBytes(saltBytes);
        String salt = Base64.getEncoder().encodeToString(saltBytes);
 
        String hash = sha256(salt + rawPassword);
        return salt + ":" + hash;
    }

    public static boolean matches(String rawPassword, String storedPassword) {
        if (rawPassword == null || storedPassword == null) return false;
        String[] parts = storedPassword.split(":", 2);
        if (parts.length != 2) return false;
        String salt     = parts[0];
        String expected = parts[1];
        String actual   = sha256(salt + rawPassword);
        return constantTimeEquals(expected, actual);
    }

    /* Strength Validation */
    public static String validateStrength(String rawPassword) {
        if (rawPassword == null || rawPassword.length() < 8)
            return "Password must be at least 8 characters long.";
        if (!rawPassword.chars().anyMatch(Character::isUpperCase))
            return "Password must contain at least one uppercase letter.";
        if (!rawPassword.chars().anyMatch(Character::isLowerCase))
            return "Password must contain at least one lowercase letter.";
        if (!rawPassword.chars().anyMatch(Character::isDigit))
            return "Password must contain at least one digit.";
        if (rawPassword.chars().noneMatch(c -> SPECIAL.indexOf(c) >= 0))
            return "Password must contain at least one special character (" + SPECIAL + ").";
        return null; 
    }

    /* Token and temporary password generation */
    public static String generateResetToken() {
        byte[] bytes = new byte[24];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static LocalDateTime resetTokenExpiry() {
        return LocalDateTime.now().plusHours(24);
    }

    public static String generateTemporaryPassword(int length) {
        int len = Math.max(length, 12);
        char[] pw = new char[len];
        pw[0] = UPPER.charAt(RANDOM.nextInt(UPPER.length()));
        pw[1] = LOWER.charAt(RANDOM.nextInt(LOWER.length()));
        pw[2] = DIGITS.charAt(RANDOM.nextInt(DIGITS.length()));
        pw[3] = SPECIAL.charAt(RANDOM.nextInt(SPECIAL.length()));
        for (int i = 4; i < len; i++)
            pw[i] = ALL.charAt(RANDOM.nextInt(ALL.length()));
        for (int i = len - 1; i > 0; i--) {
            int j   = RANDOM.nextInt(i + 1);
            char tmp = pw[i];
            pw[i]   = pw[j];
            pw[j]   = tmp;
        }
        return new String(pw);
    }

    /* Private Helper Methods */
    private static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available on this JVM.", e);
        }
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) return false;
        int diff = 0;
        for (int i = 0; i < a.length(); i++)
            diff |= a.charAt(i) ^ b.charAt(i);
        return diff == 0;
    }
}