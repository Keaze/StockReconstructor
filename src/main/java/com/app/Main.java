package com.app;

import com.app.history.reader.CsvMovementReader;
import com.app.history.reader.MovementReader;
import com.app.history.reader.MovementStream;
import com.app.stock.StockData;
import com.app.stock.reader.CsvStockIO;
import com.app.stock.reader.StockIO;
import com.app.utils.Result;
import com.app.utils.StockError;

public class Main {
    public static void main(String[] args) {
        final String stockCsvFile = "PLSTORE_ES_BESTAND_EOD.csv";
        final String movementCsvFile = "PLSTORE_ES_BESTJOUR_EOD.csv";

        StockIO stockReader = new CsvStockIO(stockCsvFile);
        MovementReader movementReader = new CsvMovementReader(movementCsvFile);

        final Result<StockData, StockError> stockData = stockReader.readStocks();
        stockData.ifSuccessful(sd -> {
            final Result<MovementStream, StockError> movementRecords = movementReader.readMovements();
            movementRecords.ifSuccessful(ms -> {
                try (ms) {
                    ms.stream().forEach(sd::handleMovement);
                }
            });
            stockReader.writeStocks(sd, "/results/");
        });
    }
}
