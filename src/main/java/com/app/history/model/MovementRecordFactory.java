package com.app.history.model;

import com.app.utils.Result;
import com.app.utils.StockError;

import static com.app.utils.CsvFieldUtils.*;

public class MovementRecordFactory {
    private static final int EXPECTED_FIELD_COUNT = 22;

    private MovementRecordFactory() {
    }

    public static Result<MovementRecord, StockError> createFromCsv(String csvLine) {
        if (csvLine == null || csvLine.trim().isEmpty()) {
            return Result.failure(StockError.parseError(csvLine, "CSV line is null or empty"));
        }

        String[] fields = splitCsvLine(csvLine);

        if (fields.length != EXPECTED_FIELD_COUNT) {
            return Result.failure(StockError.invalidFieldCount(csvLine, EXPECTED_FIELD_COUNT, fields.length));
        }

        try {
            MovementRecord movementRecord = MovementRecord.builder()
                    .sequenceNumber(parseInt(fields[0]))
                    .stockNumber(parseInt(fields[1]))
                    .handlingUnitNumber(parseString(fields[2]))
                    .location(parseString(fields[3]))
                    .itemNumber(parseString(fields[4]))
                    .serialNumber(parseString(fields[5]))
                    .batch1(parseString(fields[6]))
                    .batch2(parseString(fields[7]))
                    .quantityChange(parseBigDecimal(fields[8]))
                    .quantityTotal(parseBigDecimal(fields[9]))
                    .weightChange(parseBigDecimal(fields[10]))
                    .client(parseInt(fields[11]))
                    .event(parseEvent(fields[12]))
                    .statusCode(parseInt(fields[13]))
                    .date(parseDate(fields[14]))
                    .time(parseString(fields[15]))
                    .user(parseString(fields[16]))
                    .printIndicator(parseString(fields[17]))
                    .document1(parseString(fields[18]))
                    .document2(parseString(fields[19]))
                    .customerOrderNumber(parseString(fields[20]))
                    .customerOrderPosition(parseString(fields[21]))
                    .build();

            return Result.success(movementRecord);
        } catch (InvalidEventException e) {
            return Result.failure(StockError.invalidEvent(csvLine, e.getMessage()));
        } catch (Exception e) {
            return Result.failure(StockError.parseError(csvLine, "Failed to parse MovementRecord: " + e.getMessage()));
        }
    }

    private static MovementEvent parseEvent(String value) {
        try {
            return MovementEvent.fromCode(value);
        } catch (IllegalArgumentException _) {
            throw new InvalidEventException(value);
        }
    }

    private static class InvalidEventException extends RuntimeException {
        private InvalidEventException(String value) {
            super(value);
        }
    }
}
