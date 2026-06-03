package com.climbing;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Utility class for validating all user inputs.
 * Keeps validation logic separate from the UI (decomposition good practice).
 */
public class Validator {

    // V-scale valid range (V0 through V17)
    public static final int MIN_GRADE = 0;
    public static final int MAX_GRADE = 17;

    // Maximum allowed length for free-text fields
    public static final int MAX_LOCATION_LENGTH = 100;

    // Date format expected from user: dd/MM/yyyy
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Valid setting options
    public static final String[] SETTINGS = {"Indoors", "Outdoors"};

    // Common climb types (user can also type a custom one)
    public static final String[] CLIMB_TYPES =
            {"Overhang", "Slab", "Vertical", "Dyno", "Power", "Crimp", "Volume", "Other"};

    /**
     * Validates a V-scale grade string like "V4".
     * @return trimmed integer grade, or -1 if invalid
     */
    public static int validateGrade(String input) {
        if (input == null) return -1;
        String trimmed = input.trim().toUpperCase();
        // Accept both "V4" and "4"
        if (trimmed.startsWith("V")) trimmed = trimmed.substring(1);
        try {
            int grade = Integer.parseInt(trimmed);
            if (grade >= MIN_GRADE && grade <= MAX_GRADE) return grade;
        } catch (NumberFormatException ignored) {}
        return -1;
    }

    /**
     * Validates a date string in dd/MM/yyyy format.
     * Rejects future dates.
     * @return the LocalDate if valid, null otherwise
     */
    public static LocalDate validateDate(String input) {
        if (input == null || input.isBlank()) return null;
        try {
            LocalDate date = LocalDate.parse(input.trim(), DATE_FORMAT);
            if (date.isAfter(LocalDate.now())) return null; // reject future dates
            return date;
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Validates a location string.
     * Must be non-empty and within the character limit.
     * @return trimmed location, or null if invalid
     */
    public static String validateLocation(String input) {
        if (input == null || input.isBlank()) return null;
        String trimmed = input.trim();
        if (trimmed.length() > MAX_LOCATION_LENGTH) return null;
        return trimmed;
    }

    /**
     * Validates a setting value.
     * Must be exactly "Indoors" or "Outdoors".
     * @return the setting string, or null if invalid
     */
    public static String validateSetting(String input) {
        if (input == null) return null;
        for (String s : SETTINGS) {
            if (s.equalsIgnoreCase(input.trim())) return s;
        }
        return null;
    }

    /**
     * Validates a climb type string.
     * Must be non-empty (free text is allowed).
     * @return trimmed type, or null if blank
     */
    public static String validateClimbType(String input) {
        if (input == null || input.isBlank()) return null;
        return input.trim();
    }

    /**
     * Formats a LocalDate back to the display format dd/MM/yyyy.
     */
    public static String formatDate(LocalDate date) {
        return date.format(DATE_FORMAT);
    }
}
