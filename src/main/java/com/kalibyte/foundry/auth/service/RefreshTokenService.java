package com.kalibyte.foundry.auth.service;

import com.kalibyte.foundry.auth.dto.TokenRefreshRequest;
import com.kalibyte.foundry.auth.dto.TokenRefreshResponse;
import com.kalibyte.foundry.auth.entity.RefreshToken;
import com.kalibyte.foundry.auth.entity.User;

import java.util.UUID;

public interface RefreshTokenService {
    RefreshToken createRefreshToken(UUID userId);
    TokenRefreshResponse refreshAccessToken(TokenRefreshRequest request);
    void revokeRefreshToken(String token);
    void deleteByUser(User user);
}
