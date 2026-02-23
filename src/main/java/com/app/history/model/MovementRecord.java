package com.app.history.model;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record MovementRecord(
        Integer sequenceNumber,
        Integer stockNumber,
        String handlingUnitNumber,
        String location,
        String itemNumber,
        String serialNumber,
        String batch1,
        String batch2,
        BigDecimal quantityChange,
        BigDecimal quantityTotal,
        BigDecimal weightChange,
        Integer client,
        MovementEvent event,
        Integer statusCode,
        LocalDate date,
        String time,
        String user,
        String printIndicator,
        String document1,
        String document2,
        String customerOrderNumber,
        String customerOrderPosition
) {
}
