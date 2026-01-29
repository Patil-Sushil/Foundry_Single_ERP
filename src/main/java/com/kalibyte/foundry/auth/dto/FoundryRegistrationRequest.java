package com.kalibyte.foundry.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FoundryRegistrationRequest {
    @NotBlank
    private String foundryName;
    
    @NotBlank
    private String address;
    
    @NotBlank
    private String gstNumber;

    @NotBlank
    @Email
    private String ownerEmail;

    @NotBlank
    private String ownerPassword;
}
