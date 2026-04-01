package com.kalibyte.foundry.production.dto.response.entry;

import com.kalibyte.foundry.production.entity.enums.ProductionShift;
import com.kalibyte.foundry.production.entity.enums.ProductionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductionEntryListItem {

    private UUID id;
    private String entryNumber;

    private UUID orderId;
    private String orderNumber;
    private String customerName;

    private LocalDate reportDate;
    private ProductionShift shift;
    private ProductionStatus status;
    private String operatorName;

    private int totalReadyCores;
    private int totalPouredMoulds;
    private int totalShotBlasting;
    private int totalFettling;
    private int totalDispatched;

    private LocalDateTime createdAt;
}