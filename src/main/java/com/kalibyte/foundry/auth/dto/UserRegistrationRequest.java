package com.kalibyte.foundry.auth.dto;

import com.kalibyte.foundry.auth.entity.ENUMS.RoleName;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserRegistrationRequest {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;

    private RoleName role;

    private String name;

    private String phone;
}
