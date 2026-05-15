package com.kalibyte.foundry.auth.service.impl;

import com.kalibyte.foundry.auth.dto.TokenRefreshRequest;
import com.kalibyte.foundry.auth.dto.TokenRefreshResponse;
import com.kalibyte.foundry.auth.entity.RefreshToken;
import com.kalibyte.foundry.auth.entity.User;
import com.kalibyte.foundry.auth.repository.RefreshTokenRepository;
import com.kalibyte.foundry.auth.repository.UserRepository;
import com.kalibyte.foundry.auth.security.token.CustomUserDetails;
import com.kalibyte.foundry.auth.security.token.JwtTokenProvider;
import com.kalibyte.foundry.auth.service.RefreshTokenService;
import com.kalibyte.foundry.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider tokenProvider;

    @Override
    @Transactional
    public RefreshToken createRefreshToken(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found"));

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(tokenProvider.getRefreshExpirationInMs()))
                .revoked(false)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    @Transactional
    public TokenRefreshResponse refreshAccessToken(TokenRefreshRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new BusinessException("Refresh token not found"));

        validateRefreshToken(refreshToken);

        // Rotation: Revoke current token and issue a new one
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        RefreshToken newRefreshToken = createRefreshToken(refreshToken.getUser().getId());

        User user = refreshToken.getUser();
        List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> (GrantedAuthority) () -> role.getName().name())
                .toList();

        CustomUserDetails userDetails = CustomUserDetails.builder()
                .id(user.getId())
                .email(user.getEmail())
                .authorities(authorities)
                .build();

        String accessToken = tokenProvider.generateToken(userDetails);

        return TokenRefreshResponse.builder()
                .accessToken(accessToken)
                .refreshToken(newRefreshToken.getToken())
                .build();
    }

    @Override
    @Transactional
    public void revokeRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new BusinessException("Refresh token not found"));
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
    }

    @Override
    @Transactional
    public void deleteByUser(User user) {
        refreshTokenRepository.deleteByUser(user);
    }

    private void validateRefreshToken(RefreshToken token) {
        if (token.isRevoked()) {
            throw new BusinessException("Refresh token has been revoked");
        }
        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new BusinessException("Refresh token has expired");
        }
    }
}
