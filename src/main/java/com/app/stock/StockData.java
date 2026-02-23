package com.app.stock;

import com.app.history.model.MovementEvent;
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
    private Integer lastMovementSequenceNumber = Integer.MAX_VALUE;
    @Getter
    private boolean criticalErrors = false;
    private final LocalDate stockDate;

    public StockData(List<StockRecord> stockRecords, LocalDate stockDate) {
        this.stockRecords = stockRecords.stream().collect(HashMap::new, (map, stockRecord) -> map.put(stockRecord.getSequenceNumber(), stockRecord), HashMap::putAll);
        this.stockDate = stockDate;
    }

    public StockData(List<StockRecord> stockRecords) {
        this.stockRecords = stockRecords.stream().collect(HashMap::new, (map, stockRecord) -> map.put(stockRecord.getSequenceNumber(), stockRecord), HashMap::putAll);
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

        if (stockDate != null && movementRecord.date().isBefore(stockDate) && !finalizedStocks.contains(movementRecord.stockNumber())) {
            finalizedStocks.add(movementRecord.stockNumber());
            Optional.ofNullable(stockRecords.get(movementRecord.stockNumber())).ifPresent(s -> s.setLocation(movementRecord.location()));
            LOGGER.info("Movement for finalized stock: {}", movementRecord.stockNumber());
            return;
        }


        if (movementRecord.sequenceNumber() >= lastMovementSequenceNumber) {
            errors.add(new StockError(StockError.ErrorType.MOVEMENT_ERROR, "Movement ID out of order: " + movementRecord.sequenceNumber(), ""));
            criticalErrors = true;
            LOGGER.warn("Movement ID out of order: {}", movementRecord.sequenceNumber());
        }
        lastMovementSequenceNumber = movementRecord.sequenceNumber();
        final StockRecord stockRecord = stockRecords.get(movementRecord.stockNumber());
        switch (movementRecord.event()) {
            case DELETE -> createStock(movementRecord);
            case MOVEMENT_OUT, BATCH_CORRECTION_OUT, BATCH_CORRECTION_IN, INVENTORY_COUNT, MOVEMENT_IN,
                 MOVEMENT_NEUTRAL -> {
                if (stockRecord == null) {
                    createStock(movementRecord);
                } else {
                    changeStockRecord(stockRecord, movementRecord);
                }
            }
            case GOODS_RECEIPT -> stockRecords.remove(movementRecord.stockNumber());
        }
    }

    public void cleanUp() {
        stockRecords.values().removeIf(stockRecord -> stockRecord.getQuantityOnHand().compareTo(BigDecimal.ZERO) <= 0);
    }

    private void createStock(MovementRecord movementRecord) {
        final StockRecord newStockRecord = StockRecord.builder()
                .sequenceNumber(movementRecord.stockNumber())
                .itemNumber(movementRecord.itemNumber())
                .client(movementRecord.client())
                .batch1(movementRecord.batch1())
                .batch2(movementRecord.batch2())
                .serialNumber(movementRecord.serialNumber())
                .customerOrderNumber(movementRecord.customerOrderNumber())
                .customerOrderPosition(movementRecord.customerOrderPosition())
                .palletNumber(movementRecord.handlingUnitNumber())
                .handlingUnitNumber(movementRecord.handlingUnitNumber())
                .location(movementRecord.location())
                .quantityOnHand(movementRecord.quantityTotal())
                .build();
        stockRecords.put(movementRecord.stockNumber(), newStockRecord);
        final BigDecimal change = Optional.ofNullable(movementRecord.quantityChange()).orElse(BigDecimal.ZERO);
        final BigDecimal newValue = newStockRecord.getQuantityOnHand().add(change.multiply(BigDecimal.valueOf(-1)));
        newStockRecord.setQuantityOnHand(newValue);
    }

    private void changeStockRecord(StockRecord stockRecord, MovementRecord movementRecord) {
        final BigDecimal change = Optional.ofNullable(movementRecord.quantityChange()).orElse(BigDecimal.ZERO);
        final BigDecimal newValue = stockRecord.getQuantityOnHand().add(change.multiply(BigDecimal.valueOf(-1)));
        if (stockRecord.getQuantityOnHand().compareTo(movementRecord.quantityTotal()) != 0
                && !List.of(MovementEvent.MOVEMENT_OUT, MovementEvent.MOVEMENT_IN, MovementEvent.MOVEMENT_NEUTRAL).contains(movementRecord.event())) {
            errors.add(new StockError(StockError.ErrorType.MOVEMENT_ERROR,
                    "Stock record " + stockRecord.getSequenceNumber() + " quantity mismatch: " + movementRecord.sequenceNumber()
                            + " (current=" + stockRecord.getQuantityOnHand() + ", movement=" + movementRecord.quantityTotal()
                            + ", change=" + change + ")", ""));
            LOGGER.warn("Stock record {} quantity mismatch for movement: {}, current: {}, movement: {}, change: {}",
                    stockRecord.getSequenceNumber(), movementRecord.sequenceNumber(), stockRecord.getQuantityOnHand(), movementRecord.quantityTotal(), change);
        }
        stockRecord.setQuantityOnHand(newValue);
        stockRecord.setLocation(movementRecord.location());
        stockRecord.setHandlingUnitNumber(movementRecord.handlingUnitNumber());
        stockRecord.setPalletNumber(movementRecord.handlingUnitNumber());
    }

    public StockRecord getStockRecord(int sequenceNumber) {
        return stockRecords.get(sequenceNumber);
    }
}
