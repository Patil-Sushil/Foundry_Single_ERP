package com.kalibyte.foundry.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerResponse {
    private UUID id;
    private String name;
    private String email;
    private String phone;
    private String companyName;
    private String address;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private String gstNumber;
    private String paymentTerms;
    private BigDecimal creditLimit;
    private String status;            // ACTIVE, INACTIVE, BLOCKED
}
