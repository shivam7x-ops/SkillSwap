package com.skillswap.util;

import java.util.regex.Pattern;

/**
 * Pure-function input validators used across the application.
 *
 * <p>Every method returns a human-readable error message on failure,
 * or {@code null} when the input is valid.
 */
public final class ValidationUtil {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final int MIN_PASSWORD_LENGTH = 6;

    private ValidationUtil() { /* utility class */ }

    /** Validate a non-blank display name. */
    public static String validateName(String name) {
        if (name == null || name.isBlank()) {
            return "Name cannot be empty.";
        }
        if (name.length() < 2) {
            return "Name must be at least 2 characters.";
        }
        return null;
    }

    /** Validate an email address (basic regex). */
    public static String validateEmail(String email) {
        if (email == null || email.isBlank()) {
            return "Email cannot be empty.";
        }
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            return "Please enter a valid email address.";
        }
        return null;
    }

    /** Validate a password (Firebase requires >= 6 characters). */
    public static String validatePassword(String password) {
        if (password == null || password.isBlank()) {
            return "Password cannot be empty.";
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            return "Password must be at least " + MIN_PASSWORD_LENGTH + " characters.";
        }
        return null;
    }

    /** Validate that confirm password matches original password. */
    public static String validatePasswordConfirmation(String password, String confirmPassword) {
        if (confirmPassword == null || confirmPassword.isBlank()) {
            return "Please confirm your password.";
        }
        if (!confirmPassword.equals(password)) {
            return "Passwords do not match.";
        }
        return null;
    }

    /**
     * Validates all registration fields at once.
     *
     * @return error message if any field is invalid, or null if all are valid
     */
    public static String validateRegistration(String name, String email, String password,
                                              String confirmPassword, String college) {
        String error = validateName(name);
        if (error != null) return error;

        error = validateEmail(email);
        if (error != null) return error;

        error = validateCollege(college);
        if (error != null) return error;

        error = validatePassword(password);
        if (error != null) return error;

        error = validatePasswordConfirmation(password, confirmPassword);
        if (error != null) return error;

        return null;
    }

    /**
     * Validates all login fields at once.
     *
     * @return error message if any field is invalid, or null if all are valid
     */
    public static String validateLogin(String email, String password) {
        String error = validateEmail(email);
        if (error != null) return error;

        if (password == null || password.isBlank()) {
            return "Password cannot be empty.";
        }
        return null;
    }

    /** Validate a college name. */
    public static String validateCollege(String college) {
        if (college == null || college.isBlank()) {
            return "College cannot be empty.";
        }
        return null;
    }

    /** Validate a skill string. */
    public static String validateSkill(String skill) {
        if (skill == null || skill.isBlank()) {
            return "Skill cannot be empty.";
        }
        if (skill.length() < 2) {
            return "Skill name must be at least 2 characters.";
        }
        return null;
    }
}
