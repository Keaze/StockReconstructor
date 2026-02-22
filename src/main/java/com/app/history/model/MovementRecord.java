package com.app.history.model;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record MovementRecord(
        Integer lfdNr,
        Integer bestandNr,
        String lhmNr,
        String platz,
        String artikelNr,
        String serienNr,
        String charge1,
        String charge2,
        BigDecimal mengeAenderung,
        BigDecimal mengeGesamt,
        BigDecimal gewichtAenderung,
        Integer mandant,
        MovementEreignis ereignis,
        Integer vgs,
        LocalDate datum,
        String zeit,
        String usr,
        String druckKnz,
        String beleg1,
        String beleg2,
        String kdAuftragsNr,
        String kdAuftragsPos
) {
}
