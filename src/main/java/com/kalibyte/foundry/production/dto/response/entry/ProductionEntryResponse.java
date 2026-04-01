package com.kalibyte.foundry.production.dto.response.entry;

import com.kalibyte.foundry.production.entity.enums.ProductionShift;
import com.kalibyte.foundry.production.entity.enums.ProductionStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class ProductionEntryResponse {

    private UUID id;
    private String entryNumber;

    private UUID orderId;
    private String orderNumber;

    private LocalDate reportDate;
    private ProductionShift shift;
    private ProductionStatus status;

    private String operatorName;
    private String remarks;

    //------------------------------------------------
    // TOTALS
    //------------------------------------------------
    private Integer totalReadyCores;
    private Integer totalPouredMoulds;
    private Integer totalShotBlastingQuantity;
    private Integer totalFettlingQuantity;
    private Integer totalDispatchedQuantity;

    private List<ProductionItemResponse> items;
}
