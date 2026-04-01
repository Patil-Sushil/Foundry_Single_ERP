package com.kalibyte.foundry.common.security;

import java.io.Serializable;
import java.util.UUID;

public record UserPrincipal(
    UUID userId,
    String email
) implements Serializable {
    public UUID getUserId() {
        return userId;
    }
}
