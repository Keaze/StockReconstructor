package com.app.stock;

import com.app.history.model.MovementEvent;
import com.app.history.model.MovementRecord;
import com.app.stock.model.StockRecord;
import com.app.utils.Result;
import com.app.utils.StockError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class StockDataTest {

    List<StockRecord> stockRecordList;

    @BeforeEach
    void setUp() {
        stockRecordList = List.of(
                StockRecord.builder()
                        .sequenceNumber(1)
                        .client(250)
                        .location("001020200807")
                        .palletNumber("38280223L")
                        .handlingUnitNumber("38280223L")
                        .quantityOnHand(BigDecimal.valueOf(5.000))
                        .build(),
                StockRecord.builder()
                        .sequenceNumber(2)
                        .client(250)
                        .location("001020200808")
                        .quantityOnHand(BigDecimal.valueOf(2.000))
                        .build(),
                StockRecord.builder()
                        .sequenceNumber(3)
                        .client(250)
                        .location("001020200809")
                        .quantityOnHand(BigDecimal.valueOf(1.000))
                        .build()
        );

    }

    @Test
    void testDeleteMovement() {
        Result<MovementRecord, StockError> movementRecord = Result.success(MovementRecord.builder()
                .sequenceNumber(4)
                .stockNumber(4)
                .quantityTotal(BigDecimal.valueOf(2.000))
                .handlingUnitNumber("38280223L")
                .location("001020200807")
                .client(250)
                .event(MovementEvent.DELETE)
                .build());
        StockData stockData = new StockData(stockRecordList);
        assertNull(stockData.getStockRecord(4));
        stockData.handleMovement(movementRecord);
        assertNotNull(stockData.getStockRecord(4));
        assertThat(stockData.getStockRecord(4).getQuantityOnHand()).isEqualTo(BigDecimal.valueOf(2.000));
        assertThat(stockData.getStockRecord(4).getHandlingUnitNumber()).isEqualTo("38280223L");
        assertThat(stockData.getStockRecord(4).getLocation()).isEqualTo("001020200807");

    }

    @Test
    void testMovementOut() {
        Result<MovementRecord, StockError> movementRecord = Result.success(MovementRecord.builder()
                .sequenceNumber(1)
                .stockNumber(1)
                .quantityTotal(BigDecimal.valueOf(2.000))
                .quantityChange(BigDecimal.TWO.multiply(BigDecimal.valueOf(-1)))
                .handlingUnitNumber("38280223L")
                .location("001020200807")
                .client(250)
                .event(MovementEvent.MOVEMENT_OUT)
                .build());
        StockData stockData = new StockData(stockRecordList);
        stockData.handleMovement(movementRecord);
        assertNotNull(stockData.getStockRecord(1));
        assertThat(stockData.getStockRecord(1).getQuantityOnHand()).isEqualByComparingTo(BigDecimal.valueOf(7.000));
        assertThat(stockData.getStockRecord(1).getHandlingUnitNumber()).isEqualTo("38280223L");
        assertThat(stockData.getStockRecord(1).getPalletNumber()).isEqualTo("38280223L");
        assertThat(stockData.getStockRecord(1).getLocation()).isEqualTo("001020200807");

    }

    @Test
    void testMovementIn() {
        Result<MovementRecord, StockError> movementRecord = Result.success(MovementRecord.builder()
                .sequenceNumber(1)
                .stockNumber(1)
                .quantityTotal(BigDecimal.valueOf(2.000))
                .quantityChange(BigDecimal.TWO)
                .handlingUnitNumber("38280223L")
                .location("001020200807")
                .client(250)
                .event(MovementEvent.MOVEMENT_OUT)
                .build());
        StockData stockData = new StockData(stockRecordList);
        stockData.handleMovement(movementRecord);
        assertNotNull(stockData.getStockRecord(1));
        assertThat(stockData.getStockRecord(1).getQuantityOnHand()).isEqualByComparingTo(BigDecimal.valueOf(3.000));
        assertThat(stockData.getStockRecord(1).getHandlingUnitNumber()).isEqualTo("38280223L");
        assertThat(stockData.getStockRecord(1).getPalletNumber()).isEqualTo("38280223L");
        assertThat(stockData.getStockRecord(1).getLocation()).isEqualTo("001020200807");
    }

    @Test
    void testMovementNeutral() {
        Result<MovementRecord, StockError> movementRecord = Result.success(MovementRecord.builder()
                .sequenceNumber(1)
                .stockNumber(1)
                .handlingUnitNumber("38280223L")
                .location("001020200809")
                .quantityTotal(BigDecimal.valueOf(5.000))
                .quantityChange(BigDecimal.ZERO)
                .client(250)
                .event(MovementEvent.MOVEMENT_NEUTRAL)
                .build());
        StockData stockData = new StockData(stockRecordList);
        stockData.handleMovement(movementRecord);
        assertNotNull(stockData.getStockRecord(1));
        assertThat(stockData.getStockRecord(1).getQuantityOnHand()).isEqualByComparingTo(BigDecimal.valueOf(5.000));
        assertThat(stockData.getStockRecord(1).getHandlingUnitNumber()).isEqualTo("38280223L");
        assertThat(stockData.getStockRecord(1).getPalletNumber()).isEqualTo("38280223L");
        assertThat(stockData.getStockRecord(1).getLocation()).isEqualTo("001020200809");
    }

    @Test
    void testInventoryCount() {
        Result<MovementRecord, StockError> movementRecord = Result.success(MovementRecord.builder()
                .sequenceNumber(1)
                .stockNumber(1)
                .handlingUnitNumber("38280223L")
                .location("001020200809")
                .quantityTotal(BigDecimal.valueOf(2.000))
                .quantityChange(BigDecimal.TWO)
                .client(250)
                .event(MovementEvent.INVENTORY_COUNT)
                .build());
        StockData stockData = new StockData(stockRecordList);
        stockData.handleMovement(movementRecord);
        assertNotNull(stockData.getStockRecord(1));
        assertThat(stockData.getStockRecord(1).getQuantityOnHand()).isEqualByComparingTo(BigDecimal.valueOf(3.000));
        assertThat(stockData.getStockRecord(1).getHandlingUnitNumber()).isEqualTo("38280223L");
        assertThat(stockData.getStockRecord(1).getPalletNumber()).isEqualTo("38280223L");
        assertThat(stockData.getStockRecord(1).getLocation()).isEqualTo("001020200809");
    }

    @Test
    void testGoodsReceipt() {
        Result<MovementRecord, StockError> movementRecord = Result.success(MovementRecord.builder()
                .sequenceNumber(1)
                .stockNumber(1)
                .handlingUnitNumber("38280223L")
                .location("001020200809")
                .client(250)
                .event(MovementEvent.GOODS_RECEIPT)
                .build());
        StockData stockData = new StockData(stockRecordList);
        assertNotNull(stockData.getStockRecord(1));
        stockData.handleMovement(movementRecord);
        assertNull(stockData.getStockRecord(1));
    }
}
