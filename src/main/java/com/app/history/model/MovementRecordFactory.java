package com.app.history.model;

import com.app.utils.Result;
import com.app.utils.StockError;

import java.util.Locale;

import static com.app.utils.CsvFieldUtils.*;

public class MovementRecordFactory {
    private static final int EXPECTED_FIELD_COUNT = 22;

    private MovementRecordFactory() {
    }

    public static Result<MovementRecord, StockError> createFromCsv(String csvLine) {
        if (csvLine == null || csvLine.trim().isEmpty()) {
            return Result.failure(StockError.parseError(csvLine, "CSV line is null or empty"));
        }

        String[] fields = csvLine.split(",", -1);

        if (fields.length != EXPECTED_FIELD_COUNT) {
            return Result.failure(StockError.invalidFieldCount(csvLine, EXPECTED_FIELD_COUNT, fields.length));
        }

        try {
            MovementRecord movementRecord = MovementRecord.builder()
                    .lfdNr(parseInt(fields[0]))
                    .bestandNr(parseInt(fields[1]))
                    .lhmNr(parseString(fields[2]))
                    .platz(parseString(fields[3]))
                    .artikelNr(parseString(fields[4]))
                    .serienNr(parseString(fields[5]))
                    .charge1(parseString(fields[6]))
                    .charge2(parseString(fields[7]))
                    .mengeAenderung(parseBigDecimal(fields[8]))
                    .mengeGesamt(parseBigDecimal(fields[9]))
                    .gewichtAenderung(parseBigDecimal(fields[10]))
                    .mandant(parseInt(fields[11]))
                    .ereignis(parseEreignis(fields[12]))
                    .vgs(parseInt(fields[13]))
                    .datum(parseDate(fields[14]))
                    .zeit(parseString(fields[15]))
                    .usr(parseString(fields[16]))
                    .druckKnz(parseString(fields[17]))
                    .beleg1(parseString(fields[18]))
                    .beleg2(parseString(fields[19]))
                    .kdAuftragsNr(parseString(fields[20]))
                    .kdAuftragsPos(parseString(fields[21]))
                    .build();

            return Result.success(movementRecord);
        } catch (InvalidEreignisException e) {
            return Result.failure(StockError.invalidEreignis(csvLine, e.getMessage()));
        } catch (Exception e) {
            return Result.failure(StockError.parseError(csvLine, "Failed to parse MovementRecord: " + e.getMessage()));
        }
    }

    private static MovementEreignis parseEreignis(String value) {
        if (value == null || value.trim().isEmpty() || value.trim().equals("_".repeat(20)) || value.trim().equals("_".repeat(10))) {
            return null;
        }
        try {
            return MovementEreignis.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException _) {
            throw new InvalidEreignisException(value);
        }
    }

    private static class InvalidEreignisException extends RuntimeException {
        private InvalidEreignisException(String value) {
            super(value);
        }
    }
}
