package com.kalibyte.foundry.superadmin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
    private String ownerName;

    @NotBlank
    @Email
    private String ownerEmail;

    @NotBlank
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$")
    private String ownerPassword;

    @NotBlank
    private String ownerPhone;
}
