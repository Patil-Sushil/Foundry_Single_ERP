package com.kalibyte.foundry.inventory.inward.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;

public record StartInwardRequest(
    @NotBlank(message = "Vehicle number is required")
    String vehicleNumber,
    
    @NotBlank(message = "Driver name is required")
    String driverName,
    
    String driverPhone,
    
    @NotBlank(message = "Vendor challan number is required")
    String vendorChallanNumber
) implements Serializable {}
