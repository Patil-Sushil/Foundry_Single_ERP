package com.kalibyte.foundry.inventory.inward.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;
import java.time.LocalDate;

public record StartInwardRequest(
    String vehicleNumber,
    String driverName,
    String driverPhone,
    String vendorChallanNumber
) implements Serializable {}
