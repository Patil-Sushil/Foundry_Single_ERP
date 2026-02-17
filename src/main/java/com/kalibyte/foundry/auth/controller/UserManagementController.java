package com.kalibyte.foundry.auth.controller;

import com.kalibyte.foundry.auth.dto.UserRegistrationRequest;
import com.kalibyte.foundry.auth.service.AuthService;
import com.kalibyte.foundry.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class UserManagementController {

    private final AuthService authService;

    @PostMapping("/create-user")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> createUser(@Valid @RequestBody UserRegistrationRequest request) {
        authService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "User created successfully", null));
    }
}
