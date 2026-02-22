package com.app.stock.model;

import com.app.utils.Result;
import com.app.utils.StockError;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class StockRecordFactoryTest {

    @Test
    void shouldParseValidCsvLine() {
        String csvLine = "9737,100773,250,1908165,____________________,,____________________,__________,38280223,38280223,001020200807,401,0,0,58.100,0.000,5.000,0.000,____________________,__________,2023-08-31,2025-10-22,11:23:44,GuZo,2023-09-01,08:32:28,J,0,,N,3,2023-09-01,2023-09-01,0,J,N,0.000,5.000,1,1,290.500,290.500,1,4,1,2,3,2023-09-01,08:32:28,IMP,2024-04-30,10:30:00,plsu,,,,,,,,,,N,,,,N,N,,,,,0,,,";

        Result<StockRecord, StockError> result = StockRecordFactory.createFromCsv(csvLine);

        assertThat(result.isSuccessful()).isTrue();

        StockRecord stockRecord = result.getOrThrow();
        assertThat(stockRecord.getLfdNr()).isEqualTo(9737);
        assertThat(stockRecord.getArtikelNr()).isEqualTo("100773");
        assertThat(stockRecord.getMandant()).isEqualTo(250);
        assertThat(stockRecord.getPlatz()).isEqualTo("001020200807");
        assertThat(stockRecord.getMengeIst()).isEqualByComparingTo(BigDecimal.valueOf(5.000));
        assertThat(stockRecord.getInvDatum()).isEqualTo(LocalDate.parse("2025-10-22"));
        assertThat(stockRecord.getInvZeit()).isEqualTo("11:23:44");
        assertThat(stockRecord.getNeuUsr()).isEqualTo("IMP");
    }

    @Test
    void shouldReturnFailureForInvalidFieldCount() {
        String csvLine = "1,2,3";

        Result<StockRecord, StockError> result = StockRecordFactory.createFromCsv(csvLine);

        assertThat(result.isFailure()).isTrue();

        StockError error = result.error();
        assertThat(error).isNotNull();
        assertThat(error.type()).isEqualTo(StockError.ErrorType.INVALID_FIELD_COUNT);
    }

    @Test
    void shouldReturnFailureForInvalidNumberFormat() {
        String csvLine = "abc,100773,250,1908165,____________________,,____________________,__________,38280223,38280223,001020200807,401,0,0,58.100,0.000,5.000,0.000,____________________,__________,2023-08-31,2025-10-22,11:23:44,GuZo,2023-09-01,08:32:28,J,0,,N,3,2023-09-01,2023-09-01,0,J,N,0.000,5.000,1,1,290.500,290.500,1,4,1,2,3,2023-09-01,08:32:28,IMP,2024-04-30,10:30:00,plsu,,,,,,,,,,N,,,,N,N,,,,,0,,,";

        Result<StockRecord, StockError> result = StockRecordFactory.createFromCsv(csvLine);

        assertThat(result.isFailure()).isTrue();

        StockError error = result.error();
        assertThat(error).isNotNull();
        assertThat(error.type()).isEqualTo(StockError.ErrorType.PARSE_ERROR);
    }
}
