package com.kalibyte.foundry.inventory.inward.dto.response;

import com.kalibyte.foundry.inventory.inward.entity.enums.InwardStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record InwardResponse(
    Long id,
    String inwardNumber,
    InwardStatus status,
    String poNumber,
    String vendorName,
    String vehicleNumber,
    String driverName,
    String driverPhone,
    String vendorChallanNumber,
    LocalDate inwardDate,
    List<ReceivedItemDetail> receivedItems,
    BigDecimal totalTaxableAmount,
    BigDecimal totalTaxAmount,
    BigDecimal grandTotal,
    LocalDateTime confirmedAt,
    LocalDateTime createdAt
) {}
