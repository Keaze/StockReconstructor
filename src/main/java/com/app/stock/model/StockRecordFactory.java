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

        String[] fields = splitCsvLine(csvLine);

        if (fields.length != EXPECTED_FIELD_COUNT) {
            return Result.failure(StockError.invalidFieldCount(csvLine, EXPECTED_FIELD_COUNT, fields.length));
        }

        try {
            StockRecord stockRecord = StockRecord.builder()
                    .sequenceNumber(parseInt(fields[0]))
                    .itemNumber(parseString(fields[1]))
                    .client(parseInt(fields[2]))
                    .batch1(parseString(fields[3]))
                    .batch2(parseString(fields[4]))
                    .serialNumber(parseString(fields[5]))
                    .customerOrderNumber(parseString(fields[6]))
                    .customerOrderPosition(parseString(fields[7]))
                    .palletNumber(parseString(fields[8]))
                    .handlingUnitNumber(parseString(fields[9]))
                    .location(parseString(fields[10]))
                    .condition(parseInt(fields[11]))
                    .lockIndicator(parseInt(fields[12]))
                    .handlingUnitType(parseInt(fields[13]))
                    .weight(parseBigDecimal(fields[14]))
                    .quantityAdded(parseBigDecimal(fields[15]))
                    .quantityOnHand(parseBigDecimal(fields[16]))
                    .quantityReserved(parseBigDecimal(fields[17]))
                    .orderNumber(parseString(fields[18]))
                    .orderPosition(parseString(fields[19]))
                    .strategyDate(parseDate(fields[20]))
                    .inventoryDate(parseDate(fields[21]))
                    .inventoryTime(parseString(fields[22]))
                    .inventoryUser(parseString(fields[23]))
                    .movementDate(parseDate(fields[24]))
                    .movementTime(parseString(fields[25]))
                    .inventoryIndicator(parseString(fields[26]))
                    .positionOnPallet(parseInt(fields[27]))
                    .bestBeforeDate(parseString(fields[28]))
                    .instabilityFlag(parseString(fields[29]))
                    .inboundStrategy(parseInt(fields[30]))
                    .inboundDate(parseDate(fields[31]))
                    .inboundNumber(parseString(fields[32]))
                    .inboundPositionNumber(parseInt(fields[33]))
                    .openedIndicator(parseString(fields[34]))
                    .qualitySwapFlag(parseString(fields[35]))
                    .qualitySwapDifference(parseBigDecimal(fields[36]))
                    .quantityDecimal(parseBigDecimal(fields[37]))
                    .conversionNumerator(parseInt(fields[38]))
                    .conversionDenominator(parseInt(fields[39]))
                    .netWeight(parseBigDecimal(fields[40]))
                    .grossWeight(parseBigDecimal(fields[41]))
                    .referenceBme(parseInt(fields[42]))
                    .referenceZeh(parseInt(fields[43]))
                    .referenceLfe(parseInt(fields[44]))
                    .referenceVke(parseInt(fields[45]))
                    .referencePallet(parseInt(fields[46]))
                    .createdDate(parseDate(fields[47]))
                    .createdTime(parseString(fields[48]))
                    .createdUser(parseString(fields[49]))
                    .modifiedDate(parseDate(fields[50]))
                    .modifiedTime(parseString(fields[51]))
                    .modifiedUser(parseString(fields[52]))
                    .labelUser(parseString(fields[53]))
                    .labelTime(parseString(fields[54]))
                    .labelDate(parseDate(fields[55]))
                    .pickSequenceNumber(parseInt(fields[56]))
                    .purchaseOrderNumber(parseString(fields[57]))
                    .purchaseOrderPosition(parseString(fields[58]))
                    .feedbackDate(parseDate(fields[59]))
                    .feedbackTime(parseString(fields[60]))
                    .feedbackSequenceNumber(parseInt(fields[61]))
                    .feedbackIndicator(parseString(fields[62]))
                    .plantNumber(parseString(fields[63]))
                    .miscText1(parseString(fields[64]))
                    .miscText2(parseString(fields[65]))
                    .qualitySwapChecked(parseString(fields[66]))
                    .bypassIndicator(parseString(fields[67]))
                    .inspectionFlag(parseString(fields[68]))
                    .aklKomIndicator(parseString(fields[69]))
                    .aklCapableIndicator(parseString(fields[70]))
                    .aklWeightToleranceLot(parseBigDecimal(fields[71]))
                    .storageLocation(parseString(fields[72]))
                    .referenceIu(parseInt(fields[73]))
                    .referenceMu(parseInt(fields[74]))
                    .feedbackSequenceNumberOriginal(parseInt(fields[75]))
                    .build();

            return Result.success(stockRecord);
        } catch (Exception e) {
            return Result.failure(StockError.parseError(csvLine, "Failed to parse StockRecord: " + e.getMessage()));
        }
    }

}
