package com.app.utils;

import java.util.Objects;

public record StockError(
        ErrorType type,
        String message,
        String csvLine
) {

    public static StockError invalidFieldCount(String csvLine, int expected, int actual) {
        return new StockError(
                ErrorType.INVALID_FIELD_COUNT,
                "Expected " + expected + " fields but got " + actual,
                csvLine
        );
    }

    public static StockError movementIdOutOfOrder(Integer id, String csvLine) {
        return new StockError(
                ErrorType.MOVEMENT_ID_OUT_OF_ORDER,
                "Movement ID %s out of order".formatted(id),
                csvLine
        );
    }

    public static StockError invalidEvent(String csvLine, String value) {
        return new StockError(
                ErrorType.INVALID_EVENT,
                "Invalid event value: " + value,
                csvLine
        );
    }

    public static StockError invalidNumberFormat(String csvLine, String fieldName, String value) {
        return new StockError(
                ErrorType.INVALID_NUMBER_FORMAT,
                "Invalid number format for field '" + fieldName + "': " + value,
                csvLine
        );
    }

    public static StockError invalidDateFormat(String csvLine, String fieldName, String value) {
        return new StockError(
                ErrorType.INVALID_DATE_FORMAT,
                "Invalid date format for field '" + fieldName + "': " + value,
                csvLine
        );
    }

    public static StockError parseError(String csvLine, String message) {
        return new StockError(
                ErrorType.PARSE_ERROR,
                message,
                csvLine
        );
    }

    public static StockError writingError(String path, String message) {
        return new StockError(
                ErrorType.WRITE_ERROR,
                message,
                path
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StockError stockError = (StockError) o;
        return type == stockError.type &&
                Objects.equals(message, stockError.message) &&
                Objects.equals(csvLine, stockError.csvLine);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, message, csvLine);
    }

    @Override
    public String toString() {
        return "StockError[" + type + ": " + message + "]";
    }

    public enum ErrorType {
        INVALID_FIELD_COUNT,
        INVALID_EVENT,
        INVALID_NUMBER_FORMAT,
        INVALID_DATE_FORMAT,
        PARSE_ERROR,
        WRITE_ERROR,
        MOVEMENT_ERROR,
        MOVEMENT_ID_OUT_OF_ORDER,
        ;
    }
}
