package com.app.stock.reader;

import com.app.stock.StockData;
import com.app.stock.model.StockRecord;
import com.app.stock.model.StockRecordFactory;
import com.app.utils.Result;
import com.app.utils.StockError;

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
        Path filePath = Path.of(csvFile);
        try {
            try (BufferedReader reader = Files.newBufferedReader(filePath)) {
                String firstLine = reader.readLine();
                if (firstLine == null) {
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
                                result -> result.getOrThrow().getLfdNr(),
                                Result::getOrThrow,
                                (existing, replacement) -> existing
                        ));
                return Result.success(new StockData(stockRecordMap));
            }
        } catch (IOException e) {
            return Result.failure(StockError.parseError(csvFile, "Failed to read CSV file: " + e.getMessage()));
        } catch (Exception e) {
            return Result.failure(StockError.parseError(csvFile, "Unexpected error reading CSV file: " + e.getMessage()));
        }
    }

    @Override
    public Result<Void, StockError> writeStocks(StockData stockData, String path) {
        if (stockData == null) {
            return Result.failure(StockError.writingError(path, "Stock data is null"));
        }
        try {
            Path directory = Path.of(path);
            Files.createDirectories(directory);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            Path stockFile = directory.resolve("stocks_" + timestamp + ".csv");
            Path errorFile = directory.resolve("errors_" + timestamp + ".csv");

            try (var stockWriter = Files.newBufferedWriter(stockFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                stockData.getStockRecords().values().stream()
                        .sorted(Comparator.comparing(StockRecord::getLfdNr, Comparator.nullsLast(Integer::compareTo)))
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

            return Result.success(null);
        } catch (IOException e) {
            return Result.failure(StockError.writingError(path, "Failed to write output files: " + e.getMessage()));
        } catch (Exception e) {
            return Result.failure(StockError.writingError(path, "Unexpected error writing output files: " + e.getMessage()));
        }
    }

    private String stockRecordToCsv(StockRecord stockRecord) {
        List<String> fields = new ArrayList<>();
        fields.add(formatInteger(stockRecord.getLfdNr()));
        fields.add(formatString(stockRecord.getArtikelNr()));
        fields.add(formatInteger(stockRecord.getMandant()));
        fields.add(formatString(stockRecord.getCharge1()));
        fields.add(formatString(stockRecord.getCharge2()));
        fields.add(formatString(stockRecord.getSerienNr()));
        fields.add(formatString(stockRecord.getKdAuftragsNr()));
        fields.add(formatString(stockRecord.getKdAuftragsPos()));
        fields.add(formatString(stockRecord.getPalNr()));
        fields.add(formatString(stockRecord.getLhmNr()));
        fields.add(formatString(stockRecord.getPlatz()));
        fields.add(formatInteger(stockRecord.getZustand()));
        fields.add(formatInteger(stockRecord.getSperrKnz()));
        fields.add(formatInteger(stockRecord.getLhmTyp()));
        fields.add(formatBigDecimal(stockRecord.getGewicht()));
        fields.add(formatBigDecimal(stockRecord.getMengeZu()));
        fields.add(formatBigDecimal(stockRecord.getMengeIst()));
        fields.add(formatBigDecimal(stockRecord.getMengeRes()));
        fields.add(formatString(stockRecord.getAuftragsNr()));
        fields.add(formatString(stockRecord.getAuftragsPos()));
        fields.add(formatLocalDate(stockRecord.getStratDatum()));
        fields.add(formatLocalDate(stockRecord.getInvDatum()));
        fields.add(formatString(stockRecord.getInvZeit()));
        fields.add(formatString(stockRecord.getInvUsr()));
        fields.add(formatLocalDate(stockRecord.getBewDatum()));
        fields.add(formatString(stockRecord.getBewZeit()));
        fields.add(formatString(stockRecord.getInvKnz()));
        fields.add(formatInteger(stockRecord.getPosAufPal()));
        fields.add(formatString(stockRecord.getMhd()));
        fields.add(formatString(stockRecord.getInstabil()));
        fields.add(formatInteger(stockRecord.getWeStrat()));
        fields.add(formatLocalDate(stockRecord.getWeDatum()));
        fields.add(formatString(stockRecord.getWeNr()));
        fields.add(formatInteger(stockRecord.getWePosNr()));
        fields.add(formatString(stockRecord.getAnbruchKnz()));
        fields.add(formatString(stockRecord.getQswaFlag()));
        fields.add(formatBigDecimal(stockRecord.getQswaDiff()));
        fields.add(formatBigDecimal(stockRecord.getMengeZeh()));
        fields.add(formatInteger(stockRecord.getUmrezZeh()));
        fields.add(formatInteger(stockRecord.getUmrenZeh()));
        fields.add(formatBigDecimal(stockRecord.getNettoGewicht()));
        fields.add(formatBigDecimal(stockRecord.getBruttoGewicht()));
        fields.add(formatInteger(stockRecord.getRefBme()));
        fields.add(formatInteger(stockRecord.getRefZeh()));
        fields.add(formatInteger(stockRecord.getRefLfe()));
        fields.add(formatInteger(stockRecord.getRefVke()));
        fields.add(formatInteger(stockRecord.getRefPal()));
        fields.add(formatLocalDate(stockRecord.getNeuDatum()));
        fields.add(formatString(stockRecord.getNeuZeit()));
        fields.add(formatString(stockRecord.getNeuUsr()));
        fields.add(formatLocalDate(stockRecord.getAenderDatum()));
        fields.add(formatString(stockRecord.getAenderZeit()));
        fields.add(formatString(stockRecord.getAenderUsr()));
        fields.add(formatString(stockRecord.getAuszeichUser()));
        fields.add(formatString(stockRecord.getAuszeichZeit()));
        fields.add(formatLocalDate(stockRecord.getAuszeichDatum()));
        fields.add(formatInteger(stockRecord.getPickLfdNr()));
        fields.add(formatString(stockRecord.getBestellNr()));
        fields.add(formatString(stockRecord.getBestellPos()));
        fields.add(formatLocalDate(stockRecord.getRueckmeldeDatum()));
        fields.add(formatString(stockRecord.getRueckmeldeZeit()));
        fields.add(formatInteger(stockRecord.getRueckmeldeLfdNr()));
        fields.add(formatString(stockRecord.getRueckmeldeKnz()));
        fields.add(formatString(stockRecord.getWerknr()));
        fields.add(formatString(stockRecord.getDivText1()));
        fields.add(formatString(stockRecord.getDivText2()));
        fields.add(formatString(stockRecord.getQswaKontrolliert()));
        fields.add(formatString(stockRecord.getKnzBypass()));
        fields.add(formatString(stockRecord.getPruefFlag()));
        fields.add(formatString(stockRecord.getKnzAklKom()));
        fields.add(formatString(stockRecord.getKnzAklFaehig()));
        fields.add(formatBigDecimal(stockRecord.getAklGewichtstoleranzLot()));
        fields.add(formatString(stockRecord.getLagerort()));
        fields.add(formatInteger(stockRecord.getRefIu()));
        fields.add(formatInteger(stockRecord.getRefMu()));
        fields.add(formatInteger(stockRecord.getRueckmeldeLfdNrOrg()));
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
