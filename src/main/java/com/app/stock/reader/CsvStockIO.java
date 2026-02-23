package com.app.stock.reader;

import com.app.stock.StockData;
import com.app.stock.model.StockRecord;
import com.app.stock.model.StockRecordFactory;
import com.app.utils.Result;
import com.app.utils.StockError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.app.utils.CsvFieldUtils.*;

public class CsvStockIO implements StockIO {
    private final String csvFile;
    private static final Logger LOGGER = LoggerFactory.getLogger(CsvStockIO.class);

    public CsvStockIO(String csvFile) {
        this.csvFile = csvFile;
    }

    private static void writeLine(java.io.Writer writer, String line) {
        try {
            writer.write(line);
            writer.write(System.lineSeparator());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write output", e);
        }
    }

    private static String csvEscape(String value) {
        if (value == null) {
            return "";
        }
        boolean needsQuotes = value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r");
        if (!needsQuotes) {
            return value;
        }
        String escaped = value.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }

    @Override
    public Result<StockData, StockError> readStocks() {
        LOGGER.info("Reading stock CSV: {}", csvFile);
        Path filePath = Path.of(csvFile);
        try {
            try (BufferedReader reader = Files.newBufferedReader(filePath)) {
                String firstLine = reader.readLine();
                if (firstLine == null) {
                    LOGGER.info("Stock CSV {} is empty", csvFile);
                    return Result.success(new StockData(Map.of()));
                }
                Stream<String> stream = reader.lines();
                if (!isHeaderLine(firstLine)) {
                    stream = Stream.concat(Stream.of(firstLine), stream);
                }
                final Map<Integer, StockRecord> stockRecordMap = stream
                        .map(StockRecordFactory::createFromCsv)
                        .filter(Result::isSuccessful)
                        .collect(Collectors.toMap(
                                result -> result.getOrThrow().getSequenceNumber(),
                                Result::getOrThrow,
                                (existing, replacement) -> existing
                        ));
                LOGGER.info("Loaded {} stock records from {}", stockRecordMap.size(), csvFile);
                return Result.success(new StockData(stockRecordMap));
            }
        } catch (IOException e) {
            LOGGER.error("Failed to read stock CSV: {}", csvFile, e);
            return Result.failure(StockError.parseError(csvFile, "Failed to read CSV file: " + e.getMessage()));
        } catch (Exception e) {
            LOGGER.error("Unexpected error reading stock CSV: {}", csvFile, e);
            return Result.failure(StockError.parseError(csvFile, "Unexpected error reading CSV file: " + e.getMessage()));
        }
    }

    @Override
    public Result<Void, StockError> writeStocks(StockData stockData, String path) {
        if (stockData == null) {
            return Result.failure(StockError.writingError(path, "Stock data is null"));
        }
        try {
            LOGGER.info("Writing stock output to {}", path);
            Path directory = Path.of(path);
            Files.createDirectories(directory);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            Path stockFile = directory.resolve("stocks_" + timestamp + ".csv");
            Path errorFile = directory.resolve("errors_" + timestamp + ".csv");

            try (var stockWriter = Files.newBufferedWriter(stockFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                stockData.getStockRecords().values().stream()
                        .sorted(Comparator.comparing(StockRecord::getSequenceNumber, Comparator.nullsLast(Integer::compareTo)))
                        .map(this::stockRecordToCsv)
                        .forEach(line -> writeLine(stockWriter, line));
            }

            try (var errorWriter = Files.newBufferedWriter(errorFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                for (StockError error : stockData.getErrors()) {
                    String line = String.join(",",
                            csvEscape(error.type().name()),
                            csvEscape(error.message()),
                            csvEscape(error.csvLine())
                    );
                    writeLine(errorWriter, line);
                }
            }

            LOGGER.info("Wrote {} stock records to {}", stockData.getStockRecords().size(), stockFile);
            if (!stockData.getErrors().isEmpty()) {
                LOGGER.warn("Wrote {} error records to {}", stockData.getErrors().size(), errorFile);
            }
            return Result.success(null);
        } catch (IOException e) {
            LOGGER.error("Failed to write stock output to {}", path, e);
            return Result.failure(StockError.writingError(path, "Failed to write output files: " + e.getMessage()));
        } catch (Exception e) {
            LOGGER.error("Unexpected error writing stock output to {}", path, e);
            return Result.failure(StockError.writingError(path, "Unexpected error writing output files: " + e.getMessage()));
        }
    }

    private String stockRecordToCsv(StockRecord stockRecord) {
        List<String> fields = new ArrayList<>();
        fields.add(formatInteger(stockRecord.getSequenceNumber()));
        fields.add(formatString(stockRecord.getItemNumber()));
        fields.add(formatInteger(stockRecord.getClient()));
        fields.add(formatString(stockRecord.getBatch1()));
        fields.add(formatString(stockRecord.getBatch2()));
        fields.add(formatString(stockRecord.getSerialNumber()));
        fields.add(formatString(stockRecord.getCustomerOrderNumber()));
        fields.add(formatString(stockRecord.getCustomerOrderPosition()));
        fields.add(formatString(stockRecord.getPalletNumber()));
        fields.add(formatString(stockRecord.getHandlingUnitNumber()));
        fields.add(formatString(stockRecord.getLocation()));
        fields.add(formatInteger(stockRecord.getCondition()));
        fields.add(formatInteger(stockRecord.getLockIndicator()));
        fields.add(formatInteger(stockRecord.getHandlingUnitType()));
        fields.add(formatBigDecimal(stockRecord.getWeight()));
        fields.add(formatBigDecimal(stockRecord.getQuantityAdded()));
        fields.add(formatBigDecimal(stockRecord.getQuantityOnHand()));
        fields.add(formatBigDecimal(stockRecord.getQuantityReserved()));
        fields.add(formatString(stockRecord.getOrderNumber()));
        fields.add(formatString(stockRecord.getOrderPosition()));
        fields.add(formatLocalDate(stockRecord.getStrategyDate()));
        fields.add(formatLocalDate(stockRecord.getInventoryDate()));
        fields.add(formatString(stockRecord.getInventoryTime()));
        fields.add(formatString(stockRecord.getInventoryUser()));
        fields.add(formatLocalDate(stockRecord.getMovementDate()));
        fields.add(formatString(stockRecord.getMovementTime()));
        fields.add(formatString(stockRecord.getInventoryIndicator()));
        fields.add(formatInteger(stockRecord.getPositionOnPallet()));
        fields.add(formatString(stockRecord.getBestBeforeDate()));
        fields.add(formatString(stockRecord.getInstabilityFlag()));
        fields.add(formatInteger(stockRecord.getInboundStrategy()));
        fields.add(formatLocalDate(stockRecord.getInboundDate()));
        fields.add(formatString(stockRecord.getInboundNumber()));
        fields.add(formatInteger(stockRecord.getInboundPositionNumber()));
        fields.add(formatString(stockRecord.getOpenedIndicator()));
        fields.add(formatString(stockRecord.getQualitySwapFlag()));
        fields.add(formatBigDecimal(stockRecord.getQualitySwapDifference()));
        fields.add(formatBigDecimal(stockRecord.getQuantityDecimal()));
        fields.add(formatInteger(stockRecord.getConversionNumerator()));
        fields.add(formatInteger(stockRecord.getConversionDenominator()));
        fields.add(formatBigDecimal(stockRecord.getNetWeight()));
        fields.add(formatBigDecimal(stockRecord.getGrossWeight()));
        fields.add(formatInteger(stockRecord.getReferenceBme()));
        fields.add(formatInteger(stockRecord.getReferenceZeh()));
        fields.add(formatInteger(stockRecord.getReferenceLfe()));
        fields.add(formatInteger(stockRecord.getReferenceVke()));
        fields.add(formatInteger(stockRecord.getReferencePallet()));
        fields.add(formatLocalDate(stockRecord.getCreatedDate()));
        fields.add(formatString(stockRecord.getCreatedTime()));
        fields.add(formatString(stockRecord.getCreatedUser()));
        fields.add(formatLocalDate(stockRecord.getModifiedDate()));
        fields.add(formatString(stockRecord.getModifiedTime()));
        fields.add(formatString(stockRecord.getModifiedUser()));
        fields.add(formatString(stockRecord.getLabelUser()));
        fields.add(formatString(stockRecord.getLabelTime()));
        fields.add(formatLocalDate(stockRecord.getLabelDate()));
        fields.add(formatInteger(stockRecord.getPickSequenceNumber()));
        fields.add(formatString(stockRecord.getPurchaseOrderNumber()));
        fields.add(formatString(stockRecord.getPurchaseOrderPosition()));
        fields.add(formatLocalDate(stockRecord.getFeedbackDate()));
        fields.add(formatString(stockRecord.getFeedbackTime()));
        fields.add(formatInteger(stockRecord.getFeedbackSequenceNumber()));
        fields.add(formatString(stockRecord.getFeedbackIndicator()));
        fields.add(formatString(stockRecord.getPlantNumber()));
        fields.add(formatString(stockRecord.getMiscText1()));
        fields.add(formatString(stockRecord.getMiscText2()));
        fields.add(formatString(stockRecord.getQualitySwapChecked()));
        fields.add(formatString(stockRecord.getBypassIndicator()));
        fields.add(formatString(stockRecord.getInspectionFlag()));
        fields.add(formatString(stockRecord.getAklKomIndicator()));
        fields.add(formatString(stockRecord.getAklCapableIndicator()));
        fields.add(formatBigDecimal(stockRecord.getAklWeightToleranceLot()));
        fields.add(formatString(stockRecord.getStorageLocation()));
        fields.add(formatInteger(stockRecord.getReferenceIu()));
        fields.add(formatInteger(stockRecord.getReferenceMu()));
        fields.add(formatInteger(stockRecord.getFeedbackSequenceNumberOriginal()));
        return String.join(",", fields);
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
