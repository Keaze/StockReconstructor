package com.app.history.reader;

import com.app.utils.Result;
import com.app.utils.StockError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class CsvMovementReader implements MovementReader {
    private final String csvFile;
    private static final Logger LOGGER = LoggerFactory.getLogger(CsvMovementReader.class);

    public CsvMovementReader(String csvFile) {
        this.csvFile = csvFile;
    }

    @Override
    public Result<MovementStream, StockError> readMovements() {
        LOGGER.info("Reading movement CSV: {}", csvFile);
        Path filePath = Path.of(csvFile);
        try {
            BufferedReader reader = Files.newBufferedReader(filePath);
                String firstLine = reader.readLine();
                if (firstLine == null) {
                    reader.close();
                    LOGGER.info("Movement CSV {} is empty", csvFile);
                    return Result.success(new MovementStream(Stream.empty()));
                }
                Stream<String> lines = reader.lines();
                if (!isHeaderLine(firstLine)) {
                    lines = Stream.concat(Stream.of(firstLine), lines);
                }
                return Result.success(new MovementStream(lines));
        } catch (IOException e) {
            LOGGER.error("Failed to read movement CSV: {}", csvFile, e);
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
