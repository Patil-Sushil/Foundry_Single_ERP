package com.kalibyte.foundry.customer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
    
    @Pattern(regexp = "^\\d{10}$", message = "Phone number must be 10 digits")
    private String phone;             // Optional
    private String companyName;       // Optional
    private String address;           // Optional
    private String city;              // Optional
    private String state;             // Optional
    private String postalCode;        // Optional
    private String country;           // Optional, default: India
    
    @Pattern(regexp = "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$", message = "Invalid GST format")
    private String gstNumber;         // Optional, validated format
    private BigDecimal creditLimit;   // Optional
}
