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
    private Integer sequenceNumber;
    private String itemNumber;
    private Integer client;
    private String batch1;
    private String batch2;
    private String serialNumber;
    private String customerOrderNumber;
    private String customerOrderPosition;
    private String palletNumber;
    private String handlingUnitNumber;
    private String location;
    private Integer condition;
    private Integer lockIndicator;
    private Integer handlingUnitType;
    private BigDecimal weight;
    private BigDecimal quantityAdded;
    private BigDecimal quantityOnHand;
    private BigDecimal quantityReserved;
    private String orderNumber;
    private String orderPosition;
    private LocalDate strategyDate;
    private LocalDate inventoryDate;
    private String inventoryTime;
    private String inventoryUser;
    private LocalDate movementDate;
    private String movementTime;
    private String inventoryIndicator;
    private Integer positionOnPallet;
    private String bestBeforeDate;
    private String instabilityFlag;
    private Integer inboundStrategy;
    private LocalDate inboundDate;
    private String inboundNumber;
    private Integer inboundPositionNumber;
    private String openedIndicator;
    private String qualitySwapFlag;
    private BigDecimal qualitySwapDifference;
    private BigDecimal quantityDecimal;
    private Integer conversionNumerator;
    private Integer conversionDenominator;
    private BigDecimal netWeight;
    private BigDecimal grossWeight;
    private Integer referenceBme;
    private Integer referenceZeh;
    private Integer referenceLfe;
    private Integer referenceVke;
    private Integer referencePallet;
    private LocalDate createdDate;
    private String createdTime;
    private String createdUser;
    private LocalDate modifiedDate;
    private String modifiedTime;
    private String modifiedUser;
    private String labelUser;
    private String labelTime;
    private LocalDate labelDate;
    private Integer pickSequenceNumber;
    private String purchaseOrderNumber;
    private String purchaseOrderPosition;
    private LocalDate feedbackDate;
    private String feedbackTime;
    private Integer feedbackSequenceNumber;
    private String feedbackIndicator;
    private String plantNumber;
    private String miscText1;
    private String miscText2;
    private String qualitySwapChecked;
    private String bypassIndicator;
    private String inspectionFlag;
    private String aklKomIndicator;
    private String aklCapableIndicator;
    private BigDecimal aklWeightToleranceLot;
    private String storageLocation;
    private Integer referenceIu;
    private Integer referenceMu;
    private Integer feedbackSequenceNumberOriginal;
}
