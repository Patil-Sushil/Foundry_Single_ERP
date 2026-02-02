package com.kalibyte.foundry.superadmin.controller;

import com.kalibyte.foundry.superadmin.dto.FoundryRegistrationRequest;
import com.kalibyte.foundry.auth.service.AuthService;
import com.kalibyte.foundry.common.response.ApiResponse;
import com.kalibyte.foundry.superadmin.service.impl.SuperAdminServiceImpl;
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
@RequestMapping("/api")
@RequiredArgsConstructor
public class RegistrationController {

    private final SuperAdminServiceImpl superAdminService;

    @PostMapping("/register-foundry")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> registerFoundry(@Valid @RequestBody FoundryRegistrationRequest request) {
        superAdminService.registerFoundry(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Foundry registered successfully", null));
    }
}
