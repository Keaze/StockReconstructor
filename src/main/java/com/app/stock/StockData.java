package com.app.stock;

import com.app.history.model.MovementEreignis;
import com.app.history.model.MovementRecord;
import com.app.stock.model.StockRecord;
import com.app.utils.Result;
import com.app.utils.StockError;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StockData {
    @Getter
    private final Map<Integer, StockRecord> stockRecords;
    @Getter
    private final List<StockError> errors = new ArrayList<>();
    private Integer lastMovementId = Integer.MAX_VALUE;
    @Getter
    private boolean criticalErrors = false;

    public StockData(List<StockRecord> stockRecords) {
        this.stockRecords = stockRecords.stream().collect(HashMap::new, (map, stockRecord) -> map.put(stockRecord.getLfdNr(), stockRecord), HashMap::putAll);
    }

    public StockData(Map<Integer, StockRecord> stockRecords) {
        this.stockRecords = stockRecords;
    }

    public void handleMovement(Result<MovementRecord, StockError> movement) {
        if (movement == null) {
            errors.add(new StockError(StockError.ErrorType.PARSE_ERROR, "Null movement", ""));
            criticalErrors = true;
            return;
        }
        if (movement.isFailure()) {
            errors.add(movement.error());
            criticalErrors = true;
            return;
        }
        final MovementRecord movementRecord = movement.getOrThrow();
        if (movementRecord.lfdNr() >= lastMovementId) {
            errors.add(new StockError(StockError.ErrorType.MOVEMENT_ERROR, "Movement ID out of order: " + movementRecord.lfdNr(), ""));
            criticalErrors = true;
        }
        lastMovementId = movementRecord.lfdNr();
        if (!stockRecords.containsKey(movementRecord.bestandNr()) && !List.of(MovementEreignis.LOESCH, MovementEreignis.BEWGAB, MovementEreignis.MGKOAB).contains(movementRecord.ereignis())) {
            errors.add(new StockError(StockError.ErrorType.MOVEMENT_ERROR, "Stock record not found: " + movementRecord.bestandNr(), ""));
            criticalErrors = true;
            return;
        }
        final StockRecord stockRecord = stockRecords.get(movementRecord.bestandNr());
        switch (movementRecord.ereignis()) {
            case LOESCH -> createStock(movementRecord);
            case BEWGAB, MGKOAB -> {
                if (stockRecord == null) {
                    createStock(movementRecord);
                } else {
                    changeStockRecord(stockRecord, movementRecord);
                }
            }
            case MGKOZU, INVZH, BEWGZU -> changeStockRecord(stockRecord, movementRecord);
            case BEWGNG -> stockRecord.setPlatz(movementRecord.platz());
            case WAREIN -> stockRecords.remove(movementRecord.bestandNr());
        }
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
    }

    private void changeStockRecord(StockRecord stockRecord, MovementRecord movementRecord) {
        final BigDecimal newValue = stockRecord.getMengeIst().add(movementRecord.mengeAenderung().multiply(BigDecimal.valueOf(-1)));
        if (!stockRecord.getMengeIst().equals(movementRecord.mengeGesamt())) {
            errors.add(new StockError(StockError.ErrorType.MOVEMENT_ERROR, "Stock record menge mismatch: " + movementRecord.lfdNr(), ""));
        }
        if (newValue.compareTo(BigDecimal.ZERO) <= 0) {
            stockRecords.remove(movementRecord.bestandNr());
            return;
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
