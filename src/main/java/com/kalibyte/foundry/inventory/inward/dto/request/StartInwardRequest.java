package com.kalibyte.foundry.inventory.inward.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;
import java.time.LocalDate;

public record StartInwardRequest(
    String vehicleNumber,
    String driverName,
    String driverPhone,
    @NotBlank(message = "Vendor Challan Number is required")
    String vendorChallanNumber,
    String vendorInvoiceNumber,
    LocalDate vendorInvoiceDate
) implements Serializable {}
