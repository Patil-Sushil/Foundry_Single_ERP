package com.kalibyte.foundry.customer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerRequest {
    @NotBlank
    private String name;              // Required
    
    @NotBlank
    @Email
    private String email;             // Required, unique per tenant
    
    private String phone;             // Optional
    private String companyName;       // Optional
    private String address;           // Optional
    private String city;              // Optional
    private String state;             // Optional
    private String postalCode;        // Optional
    private String country;           // Optional, default: India
    private String gstNumber;         // Optional, validated format
    private BigDecimal creditLimit;   // Optional
}
