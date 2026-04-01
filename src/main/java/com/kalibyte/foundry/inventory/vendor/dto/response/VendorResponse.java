package com.kalibyte.foundry.inventory.vendor.dto.response;

import java.time.LocalDateTime;

public record VendorResponse(
    Long id,
    String name,
    String phone,
    String gstNumber,
    String address,
    Boolean isActive,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
