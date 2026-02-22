package com.app.history.reader;

import com.app.utils.Result;
import com.app.utils.StockError;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class CsvMovementReader implements MovementReader {
    private final String csvFile;

    public CsvMovementReader(String csvFile) {
        this.csvFile = csvFile;
    }

    @Override
    public Result<MovementStream, StockError> readMovements() {
        Path filePath = Path.of(csvFile);
        try {
            try (BufferedReader reader = Files.newBufferedReader(filePath)) {
                String firstLine = reader.readLine();
                if (firstLine == null) {
                    return Result.success(new MovementStream(Stream.empty()));
                }
                Stream<String> lines = reader.lines();
                if (!isHeaderLine(firstLine)) {
                    lines = Stream.concat(Stream.of(firstLine), lines);
                }
                return Result.success(new MovementStream(lines));
            }
        } catch (IOException e) {
            return Result.failure(StockError.parseError(csvFile, "Failed to read CSV file: " + e.getMessage()));
        }
    }

    private boolean isHeaderLine(String line) {
        String[] fields = line.split(",", -1);
        if (fields.length == 0) {
            return false;
        }
        String first = fields[0].trim();
        if (first.isEmpty()) {
            return false;
        }
        if (first.equalsIgnoreCase("LFDNR")) {
            return true;
        }
        try {
            Integer.parseInt(first);
            return false;
        } catch (NumberFormatException _) {
            return true;
        }
    }
}
