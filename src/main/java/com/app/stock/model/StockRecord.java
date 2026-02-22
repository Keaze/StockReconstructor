package com.app.stock.model;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@AllArgsConstructor
@Builder
public class StockRecord {
    private Integer lfdNr;
    private String artikelNr;
    private Integer mandant;
    private String charge1;
    private String charge2;
    private String serienNr;
    private String kdAuftragsNr;
    private String kdAuftragsPos;
    private String palNr;
    private String lhmNr;
    private String platz;
    private Integer zustand;
    private Integer sperrKnz;
    private Integer lhmTyp;
    private BigDecimal gewicht;
    private BigDecimal mengeZu;
    private BigDecimal mengeIst;
    private BigDecimal mengeRes;
    private String auftragsNr;
    private String auftragsPos;
    private LocalDate stratDatum;
    private LocalDate invDatum;
    private String invZeit;
    private String invUsr;
    private LocalDate bewDatum;
    private String bewZeit;
    private String invKnz;
    private Integer posAufPal;
    private String mhd;
    private String instabil;
    private Integer weStrat;
    private LocalDate weDatum;
    private String weNr;
    private Integer wePosNr;
    private String anbruchKnz;
    private String qswaFlag;
    private BigDecimal qswaDiff;
    private BigDecimal mengeZeh;
    private Integer umrezZeh;
    private Integer umrenZeh;
    private BigDecimal nettoGewicht;
    private BigDecimal bruttoGewicht;
    private Integer refBme;
    private Integer refZeh;
    private Integer refLfe;
    private Integer refVke;
    private Integer refPal;
    private LocalDate neuDatum;
    private String neuZeit;
    private String neuUsr;
    private LocalDate aenderDatum;
    private String aenderZeit;
    private String aenderUsr;
    private String auszeichUser;
    private String auszeichZeit;
    private LocalDate auszeichDatum;
    private Integer pickLfdNr;
    private String bestellNr;
    private String bestellPos;
    private LocalDate rueckmeldeDatum;
    private String rueckmeldeZeit;
    private Integer rueckmeldeLfdNr;
    private String rueckmeldeKnz;
    private String werknr;
    private String divText1;
    private String divText2;
    private String qswaKontrolliert;
    private String knzBypass;
    private String pruefFlag;
    private String knzAklKom;
    private String knzAklFaehig;
    private BigDecimal aklGewichtstoleranzLot;
    private String lagerort;
    private Integer refIu;
    private Integer refMu;
    private Integer rueckmeldeLfdNrOrg;
}
