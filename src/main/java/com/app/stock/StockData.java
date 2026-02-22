package com.app.stock;

import com.app.history.model.MovementEreignis;
import com.app.history.model.MovementRecord;
import com.app.stock.model.StockRecord;
import com.app.utils.Result;
import com.app.utils.StockError;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

public class StockData {
    private static final Logger LOGGER = LoggerFactory.getLogger(StockData.class);
    @Getter
    private final Map<Integer, StockRecord> stockRecords;
    private final Set<Integer> finalizedStocks = new HashSet<>();
    @Getter
    private final List<StockError> errors = new ArrayList<>();
    private Integer lastMovementId = Integer.MAX_VALUE;
    @Getter
    private boolean criticalErrors = false;
    private final LocalDate stockDate;

    public StockData(List<StockRecord> stockRecords, LocalDate stockDate) {
        this.stockRecords = stockRecords.stream().collect(HashMap::new, (map, stockRecord) -> map.put(stockRecord.getLfdNr(), stockRecord), HashMap::putAll);
        this.stockDate = stockDate;
    }

    public StockData(List<StockRecord> stockRecords) {
        this.stockRecords = stockRecords.stream().collect(HashMap::new, (map, stockRecord) -> map.put(stockRecord.getLfdNr(), stockRecord), HashMap::putAll);
        this.stockDate = null;
    }

    public StockData(Map<Integer, StockRecord> stockRecords, LocalDate stockDate) {
        this.stockRecords = stockRecords;
        this.stockDate = stockDate;
    }

    public StockData(Map<Integer, StockRecord> stockRecords) {
        this.stockRecords = stockRecords;
        this.stockDate = null;
    }

    public void handleMovement(Result<MovementRecord, StockError> movement) {

        if (movement == null) {
            errors.add(new StockError(StockError.ErrorType.PARSE_ERROR, "Null movement", ""));
            criticalErrors = true;
            LOGGER.warn("Received null movement result");
            return;
        }
        if (movement.isFailure()) {
            errors.add(movement.error());
            criticalErrors = true;
            LOGGER.warn("Movement parse error: {}", movement.error().message());
            return;
        }
        final MovementRecord movementRecord = movement.getOrThrow();

        if (stockDate != null && movementRecord.datum().isBefore(stockDate) && !finalizedStocks.contains(movementRecord.bestandNr())) {
            finalizedStocks.add(movementRecord.bestandNr());
            Optional.ofNullable(stockRecords.get(movementRecord.bestandNr())).ifPresent(s -> s.setPlatz(movementRecord.platz()));
            LOGGER.info("Movement for finalized stock: {}", movementRecord.bestandNr());
            return;
        }


        if (movementRecord.lfdNr() >= lastMovementId) {
            errors.add(new StockError(StockError.ErrorType.MOVEMENT_ERROR, "Movement ID out of order: " + movementRecord.lfdNr(), ""));
            criticalErrors = true;
            LOGGER.warn("Movement ID out of order: {}", movementRecord.lfdNr());
        }
        lastMovementId = movementRecord.lfdNr();
        final StockRecord stockRecord = stockRecords.get(movementRecord.bestandNr());
        switch (movementRecord.ereignis()) {
            case LOESCH -> createStock(movementRecord);
            case BEWGAB, MGKOAB, MGKOZU, INVZHL, BEWGZU, BEWGNG -> {
                if (stockRecord == null) {
                    createStock(movementRecord);
                } else {
                    changeStockRecord(stockRecord, movementRecord);
                }
            }
            case WAREIN -> stockRecords.remove(movementRecord.bestandNr());
        }
    }

    public void cleanUp() {
        stockRecords.values().removeIf(stockRecord -> stockRecord.getMengeIst().compareTo(BigDecimal.ZERO) <= 0);
    }

    private void createStock(MovementRecord movementRecord) {
        final StockRecord newStockRecord = StockRecord.builder()
                .lfdNr(movementRecord.bestandNr())
                .artikelNr(movementRecord.artikelNr())
                .mandant(movementRecord.mandant())
                .charge1(movementRecord.charge1())
                .charge2(movementRecord.charge2())
                .serienNr(movementRecord.serienNr())
                .kdAuftragsNr(movementRecord.kdAuftragsNr())
                .kdAuftragsPos(movementRecord.kdAuftragsPos())
                .palNr(movementRecord.lhmNr())
                .lhmNr(movementRecord.lhmNr())
                .platz(movementRecord.platz())
                .mengeIst(movementRecord.mengeGesamt())
                .build();
        stockRecords.put(movementRecord.bestandNr(), newStockRecord);
        final BigDecimal aenderung = Optional.ofNullable(movementRecord.mengeAenderung()).orElse(BigDecimal.ZERO);
        final BigDecimal newValue = newStockRecord.getMengeIst().add(aenderung.multiply(BigDecimal.valueOf(-1)));
        newStockRecord.setMengeIst(newValue);
    }

    private void changeStockRecord(StockRecord stockRecord, MovementRecord movementRecord) {
        final BigDecimal aenderung = Optional.ofNullable(movementRecord.mengeAenderung()).orElse(BigDecimal.ZERO);
        final BigDecimal newValue = stockRecord.getMengeIst().add(aenderung.multiply(BigDecimal.valueOf(-1)));
        if (stockRecord.getMengeIst().compareTo(movementRecord.mengeGesamt()) != 0 && !List.of(MovementEreignis.BEWGAB, MovementEreignis.BEWGZU, MovementEreignis.BEWGNG).contains(movementRecord.ereignis())) {
            errors.add(new StockError(StockError.ErrorType.MOVEMENT_ERROR, "Stock record " + stockRecord.getLfdNr() + " menge mismatch: " + movementRecord.lfdNr() + " (current=" + stockRecord.getMengeIst() + ", movement=" + movementRecord.mengeGesamt() + ", change=" + aenderung + ")", ""));
            LOGGER.warn("Stock record {} menge mismatch for movement: {}, current: {}, movement: {}, change: {}", stockRecord.getLfdNr(), movementRecord.lfdNr(), stockRecord.getMengeIst(), movementRecord.mengeGesamt(), aenderung);
        }
        stockRecord.setMengeIst(newValue);
        stockRecord.setPlatz(movementRecord.platz());
        stockRecord.setLhmNr(movementRecord.lhmNr());
        stockRecord.setPalNr((movementRecord.lhmNr()));
    }

    public StockRecord getStockRecord(int lfdNr) {
        return stockRecords.get(lfdNr);
    }
}
