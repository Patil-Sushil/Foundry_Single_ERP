package com.kalibyte.foundry.auth.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
public class UserResponse {

    private UUID id;
    private String name;
    private String email;
    private String phone;
    private boolean enabled;
    private List<String> roles;
}

