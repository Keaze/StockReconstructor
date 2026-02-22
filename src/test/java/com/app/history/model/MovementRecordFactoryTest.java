package com.app.history.model;

import com.app.utils.Result;
import com.app.utils.StockError;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MovementRecordFactoryTest {

    @Test
    void shouldParseOriginalCsvLine() {
        String csvLine = "1710707,11005744,4000046303,001AK0100000,103098,,R65127,____________________,1.000,1.000,1.200,250,BEWGZU,25,2026-02-19,16:45:17,KAAC,N,ELU0002984,38,____________________,__________";

        Result<MovementRecord, StockError> result = MovementRecordFactory.createFromCsv(csvLine);

        assertThat(result.isSuccessful()).isTrue();
        assertThat(result.isFailure()).isFalse();

        MovementRecord movementRecord = result.getOrThrow();

        assertThat(movementRecord).isNotNull();
        assertThat(movementRecord.lfdNr()).isEqualTo(1710707);
        assertThat(movementRecord.bestandNr()).isEqualTo(11005744);
        assertThat(movementRecord.platz()).isEqualTo("001AK0100000");
        assertThat(movementRecord.charge1()).isEqualTo("R65127");
        assertThat(movementRecord.ereignis()).isEqualTo(MovementEreignis.BEWGZU);
    }

    @Test
    void shouldReturnFailureForInvalidFieldCount() {
        String csvLine = "1,2,3";

        Result<MovementRecord, StockError> result = MovementRecordFactory.createFromCsv(csvLine);

        assertThat(result.isSuccessful()).isFalse();
        assertThat(result.isFailure()).isTrue();
        assertThat(result.getOrElse(null)).isNull();

        StockError error = result.error();
        assertThat(error).isNotNull();
        assertThat(error.type()).isEqualTo(StockError.ErrorType.INVALID_FIELD_COUNT);
    }

    @Test
    void shouldParseEreignisCaseInsensitive() {
        String csvLine = "1710707,11005744,4000046303,001AK0100000,103098,,R65127,____________________,1.000,1.000,1.200,250,bewgzu,25,2026-02-19,16:45:17,KAAC,N,ELU0002984,38,____________________,__________";

        Result<MovementRecord, StockError> result = MovementRecordFactory.createFromCsv(csvLine);

        assertThat(result.isSuccessful()).isTrue();

        MovementRecord movementRecord = result.getOrThrow();
        assertThat(movementRecord.ereignis()).isEqualTo(MovementEreignis.BEWGZU);
    }

    @Test
    void shouldReturnFailureForInvalidEreignis() {
        String csvLine = "1710707,11005744,4000046303,001AK0100000,103098,,R65127,____________________,1.000,1.000,1.200,250,BADVAL,25,2026-02-19,16:45:17,KAAC,N,ELU0002984,38,____________________,__________";

        Result<MovementRecord, StockError> result = MovementRecordFactory.createFromCsv(csvLine);

        assertThat(result.isFailure()).isTrue();

        StockError error = result.error();
        assertThat(error).isNotNull();
        assertThat(error.type()).isEqualTo(StockError.ErrorType.INVALID_EREIGNIS);
    }
}
