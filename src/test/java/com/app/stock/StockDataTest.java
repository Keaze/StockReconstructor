package com.app.stock;

import com.app.history.model.MovementEreignis;
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
                        .lfdNr(1)
                        .mandant(250)
                        .platz("001020200807")
                        .palNr("38280223L")
                        .lhmNr("38280223L")
                        .mengeIst(BigDecimal.valueOf(5.000))
                        .build(),
                StockRecord.builder()
                        .lfdNr(2)
                        .mandant(250)
                        .platz("001020200808")
                        .mengeIst(BigDecimal.valueOf(2.000))
                        .build(),
                StockRecord.builder()
                        .lfdNr(3)
                        .mandant(250)
                        .platz("001020200809")
                        .mengeIst(BigDecimal.valueOf(1.000))
                        .build()
        );

    }

    @Test
    void test_delete_movement() {
        Result<MovementRecord, StockError> movementRecord = Result.success(MovementRecord.builder()
                .lfdNr(4)
                .bestandNr(4)
                .mengeGesamt(BigDecimal.valueOf(2.000))
                .lhmNr("38280223L")
                .platz("001020200807")
                .mandant(250)
                .ereignis(MovementEreignis.LOESCH)
                .build());
        StockData stockData = new StockData(stockRecordList);
        assertNull(stockData.getStockRecord(4));
        stockData.handleMovement(movementRecord);
        assertNotNull(stockData.getStockRecord(4));
        assertThat(stockData.getStockRecord(4).getMengeIst()).isEqualTo(BigDecimal.valueOf(2.000));
        assertThat(stockData.getStockRecord(4).getLhmNr()).isEqualTo("38280223L");
        assertThat(stockData.getStockRecord(4).getPlatz()).isEqualTo("001020200807");

    }

    @Test
    void test_bewab() {
        Result<MovementRecord, StockError> movementRecord = Result.success(MovementRecord.builder()
                .lfdNr(1)
                .bestandNr(1)
                .mengeGesamt(BigDecimal.valueOf(2.000))
                .mengeAenderung(BigDecimal.TWO.multiply(BigDecimal.valueOf(-1)))
                .lhmNr("38280223L")
                .platz("001020200807")
                .mandant(250)
                .ereignis(MovementEreignis.BEWGAB)
                .build());
        StockData stockData = new StockData(stockRecordList);
        stockData.handleMovement(movementRecord);
        assertNotNull(stockData.getStockRecord(1));
        assertThat(stockData.getStockRecord(1).getMengeIst()).isEqualByComparingTo(BigDecimal.valueOf(7.000));
        assertThat(stockData.getStockRecord(1).getLhmNr()).isEqualTo("38280223L");
        assertThat(stockData.getStockRecord(1).getPalNr()).isEqualTo("38280223L");
        assertThat(stockData.getStockRecord(1).getPlatz()).isEqualTo("001020200807");

    }

    @Test
    void test_bewzu() {
        Result<MovementRecord, StockError> movementRecord = Result.success(MovementRecord.builder()
                .lfdNr(1)
                .bestandNr(1)
                .mengeGesamt(BigDecimal.valueOf(2.000))
                .mengeAenderung(BigDecimal.TWO)
                .lhmNr("38280223L")
                .platz("001020200807")
                .mandant(250)
                .ereignis(MovementEreignis.BEWGAB)
                .build());
        StockData stockData = new StockData(stockRecordList);
        stockData.handleMovement(movementRecord);
        assertNotNull(stockData.getStockRecord(1));
        assertThat(stockData.getStockRecord(1).getMengeIst()).isEqualByComparingTo(BigDecimal.valueOf(3.000));
        assertThat(stockData.getStockRecord(1).getLhmNr()).isEqualTo("38280223L");
        assertThat(stockData.getStockRecord(1).getPalNr()).isEqualTo("38280223L");
        assertThat(stockData.getStockRecord(1).getPlatz()).isEqualTo("001020200807");
    }

    @Test
    void test_bew() {
        Result<MovementRecord, StockError> movementRecord = Result.success(MovementRecord.builder()
                .lfdNr(1)
                .bestandNr(1)
                .lhmNr("38280223L")
                .platz("001020200809")
                .mengeGesamt(BigDecimal.valueOf(5.000))
                .mengeAenderung(BigDecimal.ZERO)
                .mandant(250)
                .ereignis(MovementEreignis.BEWGNG)
                .build());
        StockData stockData = new StockData(stockRecordList);
        stockData.handleMovement(movementRecord);
        assertNotNull(stockData.getStockRecord(1));
        assertThat(stockData.getStockRecord(1).getMengeIst()).isEqualByComparingTo(BigDecimal.valueOf(5.000));
        assertThat(stockData.getStockRecord(1).getLhmNr()).isEqualTo("38280223L");
        assertThat(stockData.getStockRecord(1).getPalNr()).isEqualTo("38280223L");
        assertThat(stockData.getStockRecord(1).getPlatz()).isEqualTo("001020200809");
    }

    @Test
    void test_inv() {
        Result<MovementRecord, StockError> movementRecord = Result.success(MovementRecord.builder()
                .lfdNr(1)
                .bestandNr(1)
                .lhmNr("38280223L")
                .platz("001020200809")
                .mengeGesamt(BigDecimal.valueOf(2.000))
                .mengeAenderung(BigDecimal.TWO)
                .mandant(250)
                .ereignis(MovementEreignis.INVZHL)
                .build());
        StockData stockData = new StockData(stockRecordList);
        stockData.handleMovement(movementRecord);
        assertNotNull(stockData.getStockRecord(1));
        assertThat(stockData.getStockRecord(1).getMengeIst()).isEqualByComparingTo(BigDecimal.valueOf(3.000));
        assertThat(stockData.getStockRecord(1).getLhmNr()).isEqualTo("38280223L");
        assertThat(stockData.getStockRecord(1).getPalNr()).isEqualTo("38280223L");
        assertThat(stockData.getStockRecord(1).getPlatz()).isEqualTo("001020200809");
    }

    @Test
    void test_loesch() {
        Result<MovementRecord, StockError> movementRecord = Result.success(MovementRecord.builder()
                .lfdNr(1)
                .bestandNr(1)
                .lhmNr("38280223L")
                .platz("001020200809")
                .mandant(250)
                .ereignis(MovementEreignis.WAREIN)
                .build());
        StockData stockData = new StockData(stockRecordList);
        assertNotNull(stockData.getStockRecord(1));
        stockData.handleMovement(movementRecord);
        assertNull(stockData.getStockRecord(1));
    }
}
