package com.kalibyte.foundry.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class LoginResponse {

    private String token;
    private UUID id;
    private String email;
    private List<String> roles;
}
