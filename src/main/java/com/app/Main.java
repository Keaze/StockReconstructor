package com.app;

import com.app.history.reader.CsvMovementReader;
import com.app.history.reader.MovementReader;
import com.app.history.reader.MovementStream;
import com.app.stock.StockData;
import com.app.stock.reader.CsvStockIO;
import com.app.stock.reader.StockIO;
import com.app.utils.Result;
import com.app.utils.StockError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
        ensureLogDirectory();
        Logger logger = LoggerFactory.getLogger(Main.class);
        final String stockCsvFile = "PLSTORE_ES_BESTAND_EOD.csv";
        final String movementCsvFile = "PLSTORE_ES_BESTJOUR_EOD.csv";

        logger.info("Starting stock reconstruction");
        logger.info("Reading stock file: {}", stockCsvFile);
        logger.info("Reading movement file: {}", movementCsvFile);

        StockIO stockReader = new CsvStockIO(stockCsvFile);
        MovementReader movementReader = new CsvMovementReader(movementCsvFile);

        final Result<StockData, StockError> stockData = stockReader.readStocks();
        stockData.ifSuccessfulOrElse(sd -> {
            logger.info("Loaded {} stock records", sd.getStockRecords().size());
            final Result<MovementStream, StockError> movementRecords = movementReader.readMovements();
            movementRecords.ifSuccessfulOrElse(ms -> {
                try (ms) {
                    ms.stream().forEach(sd::handleMovement);
                }
                logger.info("Processed movements. Errors: {}, critical: {}", sd.getErrors().size(), sd.isCriticalErrors());
            }, () -> logger.error("Failed to read movements: {}", movementRecords.error().message()));

            final int sizeBeforeCleanUp = sd.getStockRecords().size();
            sd.cleanUp();
            final int sizeAfterCleanUp = sd.getStockRecords().size();
            logger.info("Cleaned up stock records. Before: {}, After: {}", sizeBeforeCleanUp, sizeAfterCleanUp);

            Result<Void, StockError> writeResult = stockReader.writeStocks(sd, "/results/");
            writeResult.ifSuccessfulOrElse(
                    ignored -> logger.info("Wrote stock output to ./results/"),
                    () -> logger.error("Failed to write stock output: {}", writeResult.error().message())
            );
        }, () -> logger.error("Failed to read stocks: {}", stockData.error().message()));

        logger.info("Stock reconstruction complete");
    }

    private static void ensureLogDirectory() {
        try {
            Files.createDirectories(Path.of("log"));
        } catch (IOException e) {
            System.err.println("Failed to create log directory: " + e.getMessage());
        }
    }
}
