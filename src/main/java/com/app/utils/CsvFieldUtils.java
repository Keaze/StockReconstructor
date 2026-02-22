package com.app.utils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public final class CsvFieldUtils {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private CsvFieldUtils() {
    }

    public static Integer parseInt(String value) {
        if (value == null || value.trim().isEmpty() || value.trim().equals("_".repeat(20)) || value.trim().equals("_".repeat(10))) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException _) {
            throw new IllegalArgumentException("Invalid number format: " + value);
        }
    }

    public static Long parseLong(String value) {
        if (value == null || value.trim().isEmpty() || value.trim().equals("_".repeat(20)) || value.trim().equals("_".repeat(10))) {
            return null;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException _) {
            throw new IllegalArgumentException("Invalid number format: " + value);
        }
    }

    public static BigDecimal parseBigDecimal(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException _) {
            throw new IllegalArgumentException("Invalid decimal format: " + value);
        }
    }

    public static String parseString(String value) {
        if (value == null || value.trim().isEmpty() || value.trim().equals("_".repeat(20)) || value.trim().equals("_".repeat(10))) {
            return null;
        }
        return value.trim();
    }

    public static LocalDate parseDate(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim(), DATE_FORMATTER);
        } catch (Exception _) {
            throw new IllegalArgumentException("Invalid date format: " + value);
        }
    }

    public static String formatString(String value) {
        return value == null ? "" : value;
    }

    public static String formatInteger(Integer value) {
        return value == null ? "" : value.toString();
    }

    public static String formatLong(Long value) {
        return value == null ? "" : value.toString();
    }

    public static String formatBigDecimal(BigDecimal value) {
        return value == null ? "" : value.toPlainString();
    }

    public static String formatLocalDate(LocalDate value) {
        return value == null ? "" : value.toString();
    }
}
