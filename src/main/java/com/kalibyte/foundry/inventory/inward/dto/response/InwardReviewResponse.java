package com.kalibyte.foundry.inventory.inward.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record InwardReviewResponse(
    Long inwardId,
    String inwardNumber,
    String purchaseOrderNumber,
    String vendorName,
    String vehicleNumber,
    String vendorChallanNumber,
    LocalDate inwardDate,
    List<ReceivedItemComparison> receivedItems,
    BigDecimal totalAmount,
    boolean hasShortage,
    boolean hasExcess
) {}
