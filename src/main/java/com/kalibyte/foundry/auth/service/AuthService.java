package com.kalibyte.foundry.auth.service;

import com.kalibyte.foundry.auth.dto.FoundryRegistrationRequest;
import com.kalibyte.foundry.auth.dto.LoginRequest;
import com.kalibyte.foundry.auth.dto.LoginResponse;
import com.kalibyte.foundry.auth.dto.UserRegistrationRequest;
import com.kalibyte.foundry.auth.entity.Role;
import com.kalibyte.foundry.auth.entity.User;
import com.kalibyte.foundry.auth.repository.RoleRepository;
import com.kalibyte.foundry.auth.repository.UserRepository;
import com.kalibyte.foundry.common.exception.BusinessException;
import com.kalibyte.foundry.common.util.ContextUtil;
import com.kalibyte.foundry.auth.security.token.CustomUserDetails;
import com.kalibyte.foundry.auth.security.token.JwtTokenProvider;
import com.kalibyte.foundry.auth.security.util.SecurityUtils;
import com.kalibyte.foundry.tenant.account.entity.TenantEntity;
import com.kalibyte.foundry.tenant.account.service.TenantService;
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
    private final UserService userService;

    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return LoginResponse.builder()
                .token(jwt)
                .id(userDetails.getId())
                .email(userDetails.getEmail())
                .roles(roles)
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

    @Transactional
    public void createUser(UserRegistrationRequest request) {
        // Get current admin user to identify tenant
        String email = SecurityUtils.getCurrentUserEmail();
        User adminUser = userService.getByEmail(email);
        Long tenantId = adminUser.getTenantId();

        if (tenantId == null) {
             throw new BusinessException("Current user is not associated with any tenant.");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already in use.");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setTenantId(tenantId);
        user.setEnabled(true);

        Role role = roleRepository.findByName(request.getRole())
                .orElseThrow(() -> new BusinessException("Role " + request.getRole() + " not found."));
        user.setRoles(new HashSet<>(Collections.singletonList(role)));

        userRepository.save(user);
    }
}