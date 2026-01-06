package com.gym.app.util;

import java.util.regex.Pattern;

public class ValidationUtils {

    // Regex pattern for validating email addresses (simple/common pattern)
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    /**
     * Checks if the provided email string is valid.
     * 
     * @param email The email address to check.
     * @return true if valid, false otherwise.
     */
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Checks if the provided phone number is a valid 10-digit integer.
     * 
     * @param phone The phone string to check.
     * @return true if exactly 10 digits, false otherwise.
     */
    public static boolean isValidPhone(String phone) {
        // Phone number should be an int of 10 digits
        if (phone == null)
            return false;
        return phone.matches("\\d{10}");
    }

    /**
     * Checks if the time string is in valid HH:mm 24-hour format.
     * 
     * @param time The time string.
     * @return true if valid, false otherwise.
     */
    public static boolean isValidTime(String time) {
        // Format HH:mm (24-hour)
        if (time == null)
            return false;
        return Pattern.matches("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$", time);
    }
}
