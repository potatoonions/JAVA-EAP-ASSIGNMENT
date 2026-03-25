package com.crs.security;
 
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public final class PasswordUtil {
    public static final int MIN_LENGTH = 8;

    public static final int MAX_LENGTH = 128;

    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL = "!@#$%^&*()-_=+[]";
    private static final String ALL = UPPER + LOWER + DIGITS + SPECIAL;

    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() {}

    /*Encryption */
    public static String encryptPassword(String password) {
        validateRawNotBlank(password);
 
        byte[] saltBytes = new byte[16];
        RANDOM.nextBytes(saltBytes);
        String salt = Base64.getEncoder().encodeToString(saltBytes);
 
        String hash = sha256Hex(salt + password);
        return salt + ":" + hash;
    }

    public static boolean verifyPassword(String inputPassword, String storedPassword) {
        if (inputPassword == null || storedPassword == null) return false;
 
        String[] parts = storedPassword.split(":", 2);
        if (parts.length != 2) {
            LoggerUtil.getInstance().logWarning(
                "PasswordUtil.verifyPassword — stored value has unexpected format.");
            return false;
        }
 
        String salt         = parts[0];
        String expectedHash = parts[1];
        String actualHash   = sha256Hex(salt + inputPassword);
 
        return constantTimeEquals(expectedHash, actualHash);
    }

    /*Password strength validation */
    public static String validateStrength(String rawPassword) {
        if (rawPassword == null || rawPassword.length() < MIN_LENGTH)
            return "Password must be at least " + MIN_LENGTH + " characters long.";
        if (!rawPassword.chars().anyMatch(Character::isUpperCase))
            return "Password must contain at least one uppercase letter.";
        if (!rawPassword.chars().anyMatch(Character::isLowerCase))
            return "Password must contain at least one lowercase letter.";
        if (!rawPassword.chars().anyMatch(Character::isDigit))
            return "Password must contain at least one digit.";
        if (rawPassword.chars().noneMatch(c -> SPECIAL.indexOf(c) >= 0))
            return "Password must include at least one special character ("
                + SPECIAL.replace("\\", "") + ").";
        return null;
    }

    public static boolean isStrongPassword(String rawPassword) {
        return validateStrength(rawPassword) == null;
    }

    /*Temp password generator */
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
            int j = RANDOM.nextInt(i + 1);
            char tmp = pw[i];
            pw[i] = pw[j];
            pw[j] = tmp;
        }

        return new String(pw);
    }

    public static String generateResetToken() {
        byte[] bytes = new byte[24];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /*Private helper methods */
    private static String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest)
                sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available on this JVM.", e);
        }
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) return false;
        if (a.length() != b.length()) return false;
        int diff = 0;
        for (int i = 0; i < a.length(); i++)
            diff |= a.charAt(i) ^ b.charAt(i);
        return diff == 0;
    }

    private static void validateRawNotBlank(String password) {
        if (password == null || password.isBlank())
            throw new IllegalArgumentException("Password must not be null or blank.");
    }
}