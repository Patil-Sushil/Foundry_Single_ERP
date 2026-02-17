package com.kalibyte.foundry.auth.controller;

import com.kalibyte.foundry.auth.dto.ChangePasswordRequest;
import com.kalibyte.foundry.auth.dto.LoginRequest;
import com.kalibyte.foundry.auth.dto.LoginResponse;
import com.kalibyte.foundry.auth.dto.TokenRefreshRequest;
import com.kalibyte.foundry.auth.service.AuthService;
import com.kalibyte.foundry.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(new ApiResponse<>(true, "Login successful", response));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {

        authService.changePassword(request);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Password changed successfully", null)
        );
    }

}
