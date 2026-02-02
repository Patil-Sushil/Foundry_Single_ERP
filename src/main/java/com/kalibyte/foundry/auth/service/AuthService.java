package com.kalibyte.foundry.auth.service;

import com.kalibyte.foundry.auth.dto.LoginRequest;
import com.kalibyte.foundry.auth.dto.LoginResponse;
import com.kalibyte.foundry.auth.dto.TokenRefreshRequest;
import com.kalibyte.foundry.auth.entity.Role;
import com.kalibyte.foundry.auth.entity.User;
import com.kalibyte.foundry.auth.repository.RoleRepository;
import com.kalibyte.foundry.auth.security.token.CustomUserDetails;
import com.kalibyte.foundry.auth.security.token.CustomUserDetailsService;
import com.kalibyte.foundry.auth.security.token.JwtTokenProvider;
import com.kalibyte.foundry.common.exception.BusinessException;
import com.kalibyte.foundry.superadmin.dto.FoundryRegistrationRequest;
import com.kalibyte.foundry.tenant.account.entity.TenantEntity;
import com.kalibyte.foundry.tenant.account.service.TenantService;
import com.kalibyte.foundry.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final TenantService tenantService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomUserDetailsService customUserDetailsService;

    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(authentication);

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return LoginResponse.builder()
                .token(jwt)
                .refreshToken(refreshToken)
                .id(userDetails.getId())
                .email(userDetails.getEmail())
                .roles(roles)
                .tenantCode(userDetails.getTenantCode())
                .tenantSchema(userDetails.getSchemaName())
                .build();
    }

    public LoginResponse refreshToken(TokenRefreshRequest request) {
        String refreshToken = request.getRefreshToken();
        
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new BusinessException("Invalid or expired refresh token");
        }

        String username = tokenProvider.getUsernameFromToken(refreshToken);
        
        // Load user details to ensure user is still active and permissions are up to date
        // cast to CustomUserDetails because we know that's what we load
        CustomUserDetails userDetails = (CustomUserDetails) customUserDetailsService.loadUserByUsername(username);
        
        // Generate new access token
        String newAccessToken = tokenProvider.generateAccessToken(userDetails);
        
        return LoginResponse.builder()
                .token(newAccessToken)
                .refreshToken(refreshToken) // Return the same refresh token
                .id(userDetails.getId())
                .email(userDetails.getEmail())
                .roles(userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
                .tenantCode(userDetails.getTenantCode())
                .tenantSchema(userDetails.getSchemaName())
                .build();
    }

    @Transactional
    public TenantEntity registerFoundry(FoundryRegistrationRequest request) {
        if (userRepository.existsByEmail(request.getOwnerEmail())) {
            throw new BusinessException("Email already in use.");
        }
        // 1-3. Create Tenant & Schema (Delegated to TenantService)
        TenantEntity tenant = tenantService.createTenant(
                request.getFoundryName(),
                request.getAddress(),
                request.getGstNumber()
        );
        // 4. Create Owner User
        User user = new User();
        user.setEmail(request.getOwnerEmail());
        user.setName(request.getOwnerName());
        user.setPhone(request.getOwnerPhone());
        user.setPassword(passwordEncoder.encode(request.getOwnerPassword()));
        user.setTenantId(tenant.getId());
        user.setEnabled(true);

        // 5. Assign ADMIN role
        Role ownerRole = roleRepository.findByName("ADMIN")
                .orElseThrow(() -> new BusinessException("Role ADMIN not found."));
        user.setRoles(new HashSet<>(Collections.singletonList(ownerRole)));

        userRepository.save(user);

        return tenant;
    }
}