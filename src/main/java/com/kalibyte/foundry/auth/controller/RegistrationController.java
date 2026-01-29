package com.kalibyte.foundry.auth.controller;

import com.kalibyte.foundry.auth.dto.FoundryRegistrationRequest;
import com.kalibyte.foundry.auth.service.AuthService;
import com.kalibyte.foundry.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class RegistrationController {

    private final AuthService authService;

    @PostMapping("/register-foundry")
    public ResponseEntity<ApiResponse<Void>> registerFoundry(@Valid @RequestBody FoundryRegistrationRequest request) {
        authService.registerFoundry(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Foundry registered successfully", null));
    }
}
