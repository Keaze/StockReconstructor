package com.app;

import com.app.history.reader.CsvMovementReader;
import com.app.history.reader.MovementReader;
import com.app.history.reader.MovementStream;
import com.app.stock.StockData;
import com.app.stock.reader.CsvStockIO;
import com.app.stock.reader.StockIO;
import com.app.tui.StockReconstructorTui;
import com.app.utils.Result;
import com.app.utils.StockError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {
        ensureLogDirectory();
        if (args.length > 0 && "--batch".equalsIgnoreCase(args[0])) {
            Logger logger = LoggerFactory.getLogger(Main.class);
            final String stockCsvFile = "PLSTORE_ES_BESTAND_EOD.csv";
            final String movementCsvFile = "PLSTORE_ES_BESTJOUR_EOD.csv";

            logger.info("Starting stock reconstruction");
            logger.info("Reading stock file: {}", stockCsvFile);
            logger.info("Reading movement file: {}", movementCsvFile);

            runBatch(logger, stockCsvFile, movementCsvFile, null);
            logger.info("Stock reconstruction complete");
            return;
        }

        new StockReconstructorTui().start();
    }

    private static void runBatch(Logger logger, String stockCsvFile, String movementCsvFile, LocalDate stockDate) {
        StockIO stockReader = new CsvStockIO(stockCsvFile);
        MovementReader movementReader = new CsvMovementReader(movementCsvFile);

        final Result<StockData, StockError> stockData = stockReader.readStocks();
        stockData.ifSuccessfulOrElse(sd -> {
            StockData data = stockDate == null ? sd : new StockData(sd.getStockRecords(), stockDate);
            logger.info("Loaded {} stock records", data.getStockRecords().size());
            final Result<MovementStream, StockError> movementRecords = movementReader.readMovements();
            movementRecords.ifSuccessfulOrElse(ms -> {
                try (ms) {
                    ms.stream().forEach(data::handleMovement);
                }
                logger.info("Processed movements. Errors: {}, critical: {}", data.getErrors().size(), data.isCriticalErrors());
            }, () -> logger.error("Failed to read movements: {}", movementRecords.error().message()));

            final int sizeBeforeCleanUp = data.getStockRecords().size();
            data.cleanUp();
            final int sizeAfterCleanUp = data.getStockRecords().size();
            logger.info("Cleaned up stock records. Before: {}, After: {}", sizeBeforeCleanUp, sizeAfterCleanUp);

            Result<Void, StockError> writeResult = stockReader.writeStocks(data, "/results/");
            writeResult.ifSuccessfulOrElse(
                    ignored -> logger.info("Wrote stock output to ./results/"),
                    () -> logger.error("Failed to write stock output: {}", writeResult.error().message())
            );
        }, () -> logger.error("Failed to read stocks: {}", stockData.error().message()));
    }

    private static void ensureLogDirectory() {
        try {
            Files.createDirectories(Path.of("log"));
        } catch (IOException e) {
            System.err.println("Failed to create log directory: " + e.getMessage());
        }
    }
}
