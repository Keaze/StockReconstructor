package com.app.stock.model;

import com.app.utils.Result;
import com.app.utils.StockError;

import static com.app.utils.CsvFieldUtils.*;

public class StockRecordFactory {
    private static final int EXPECTED_FIELD_COUNT = 76;

    private StockRecordFactory() {
    }

    public static Result<StockRecord, StockError> createFromCsv(String csvLine) {
        if (csvLine == null || csvLine.trim().isEmpty()) {
            return Result.failure(StockError.parseError(csvLine, "CSV line is null or empty"));
        }

        String[] fields = csvLine.split(",", -1);

        if (fields.length != EXPECTED_FIELD_COUNT) {
            return Result.failure(StockError.invalidFieldCount(csvLine, EXPECTED_FIELD_COUNT, fields.length));
        }

        try {
            StockRecord stockRecord = StockRecord.builder()
                    .lfdNr(parseInt(fields[0]))
                    .artikelNr(parseString(fields[1]))
                    .mandant(parseInt(fields[2]))
                    .charge1(parseString(fields[3]))
                    .charge2(parseString(fields[4]))
                    .serienNr(parseString(fields[5]))
                    .kdAuftragsNr(parseString(fields[6]))
                    .kdAuftragsPos(parseString(fields[7]))
                    .palNr(parseString(fields[8]))
                    .lhmNr(parseString(fields[9]))
                    .platz(parseString(fields[10]))
                    .zustand(parseInt(fields[11]))
                    .sperrKnz(parseInt(fields[12]))
                    .lhmTyp(parseInt(fields[13]))
                    .gewicht(parseBigDecimal(fields[14]))
                    .mengeZu(parseBigDecimal(fields[15]))
                    .mengeIst(parseBigDecimal(fields[16]))
                    .mengeRes(parseBigDecimal(fields[17]))
                    .auftragsNr(parseString(fields[18]))
                    .auftragsPos(parseString(fields[19]))
                    .stratDatum(parseDate(fields[20]))
                    .invDatum(parseDate(fields[21]))
                    .invZeit(parseString(fields[22]))
                    .invUsr(parseString(fields[23]))
                    .bewDatum(parseDate(fields[24]))
                    .bewZeit(parseString(fields[25]))
                    .invKnz(parseString(fields[26]))
                    .posAufPal(parseInt(fields[27]))
                    .mhd(parseString(fields[28]))
                    .instabil(parseString(fields[29]))
                    .weStrat(parseInt(fields[30]))
                    .weDatum(parseDate(fields[31]))
                    .weNr(parseString(fields[32]))
                    .wePosNr(parseInt(fields[33]))
                    .anbruchKnz(parseString(fields[34]))
                    .qswaFlag(parseString(fields[35]))
                    .qswaDiff(parseBigDecimal(fields[36]))
                    .mengeZeh(parseBigDecimal(fields[37]))
                    .umrezZeh(parseInt(fields[38]))
                    .umrenZeh(parseInt(fields[39]))
                    .nettoGewicht(parseBigDecimal(fields[40]))
                    .bruttoGewicht(parseBigDecimal(fields[41]))
                    .refBme(parseInt(fields[42]))
                    .refZeh(parseInt(fields[43]))
                    .refLfe(parseInt(fields[44]))
                    .refVke(parseInt(fields[45]))
                    .refPal(parseInt(fields[46]))
                    .neuDatum(parseDate(fields[47]))
                    .neuZeit(parseString(fields[48]))
                    .neuUsr(parseString(fields[49]))
                    .aenderDatum(parseDate(fields[50]))
                    .aenderZeit(parseString(fields[51]))
                    .aenderUsr(parseString(fields[52]))
                    .auszeichUser(parseString(fields[53]))
                    .auszeichZeit(parseString(fields[54]))
                    .auszeichDatum(parseDate(fields[55]))
                    .pickLfdNr(parseInt(fields[56]))
                    .bestellNr(parseString(fields[57]))
                    .bestellPos(parseString(fields[58]))
                    .rueckmeldeDatum(parseDate(fields[59]))
                    .rueckmeldeZeit(parseString(fields[60]))
                    .rueckmeldeLfdNr(parseInt(fields[61]))
                    .rueckmeldeKnz(parseString(fields[62]))
                    .werknr(parseString(fields[63]))
                    .divText1(parseString(fields[64]))
                    .divText2(parseString(fields[65]))
                    .qswaKontrolliert(parseString(fields[66]))
                    .knzBypass(parseString(fields[67]))
                    .pruefFlag(parseString(fields[68]))
                    .knzAklKom(parseString(fields[69]))
                    .knzAklFaehig(parseString(fields[70]))
                    .aklGewichtstoleranzLot(parseBigDecimal(fields[71]))
                    .lagerort(parseString(fields[72]))
                    .refIu(parseInt(fields[73]))
                    .refMu(parseInt(fields[74]))
                    .rueckmeldeLfdNrOrg(parseInt(fields[75]))
                    .build();

            return Result.success(stockRecord);
        } catch (Exception e) {
            return Result.failure(StockError.parseError(csvLine, "Failed to parse StockRecord: " + e.getMessage()));
        }
    }

}
