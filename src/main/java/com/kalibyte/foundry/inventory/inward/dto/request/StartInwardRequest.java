package com.kalibyte.foundry.inventory.inward.dto.request;

import java.io.Serializable;

public record StartInwardRequest(
    String vehicleNumber,
    String driverName,
    String driverPhone,
    String vendorChallanNumber
) implements Serializable {}
