package com.kalibyte.foundry.inventory.vendor.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;

public record CreateVendorRequest(
    @NotBlank(message = "Name is required")
    String name,
    String phone,
    String gstNumber,
    String state,
    String address,
    @Email
    String email
) implements Serializable {}
