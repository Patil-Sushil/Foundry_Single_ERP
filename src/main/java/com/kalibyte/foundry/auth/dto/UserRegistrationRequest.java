package com.kalibyte.foundry.auth.dto;

import com.kalibyte.foundry.auth.entity.ENUMS.RoleName;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UserRegistrationRequest {

    @NotBlank
    @Email
    private String email;

    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,20}$",
            message = "Password must contain at least one digit, one lowercase, one uppercase, one special character, and no whitespace.")
    private String password;

    private RoleName role;

    private String name;

    private String phone;
}
