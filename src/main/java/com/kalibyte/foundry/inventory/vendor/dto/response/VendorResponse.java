package com.kalibyte.foundry.inventory.vendor.dto.response;

import java.time.LocalDateTime;

public record VendorResponse(
    Long id,
    String name,
    String phone,
    String gstNumber,
    String state,
    String address,
    String email,
    Boolean isActive,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
