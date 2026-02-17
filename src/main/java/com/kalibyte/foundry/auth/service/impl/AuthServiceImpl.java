package com.kalibyte.foundry.auth.service.impl;

import com.kalibyte.foundry.auth.dto.ChangePasswordRequest;
import com.kalibyte.foundry.auth.dto.LoginRequest;
import com.kalibyte.foundry.auth.dto.LoginResponse;
import com.kalibyte.foundry.auth.dto.UserRegistrationRequest;
import com.kalibyte.foundry.auth.entity.Role;
import com.kalibyte.foundry.auth.entity.User;
import com.kalibyte.foundry.auth.repository.RoleRepository;
import com.kalibyte.foundry.auth.repository.UserRepository;
import com.kalibyte.foundry.auth.security.token.CustomUserDetails;
import com.kalibyte.foundry.auth.security.token.JwtTokenProvider;
import com.kalibyte.foundry.auth.service.AuthService;
import com.kalibyte.foundry.common.exception.BusinessException;
import com.kalibyte.foundry.common.util.PasswordValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public LoginResponse login(LoginRequest request) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.generateToken(authentication);

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        List<String> roles = userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return LoginResponse.builder()
                .token(jwt)
                .id(userDetails.getId())
                .email(userDetails.getEmail())
                .roles(roles)
                .build();
    }

    @Override
    public void createUser(UserRegistrationRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already exists");
        }


        User user = new User();
        user.setEmail(request.getEmail());

        if (!PasswordValidator.isValid(request.getPassword())) {
            throw new BusinessException(
                    "Password must be 8-20 characters long and include uppercase, lowercase, number and special character"
            );
        }

        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEnabled(true);

        userRepository.save(user);
    }

    @Override
    public void changePassword(ChangePasswordRequest request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            throw new BusinessException("User not authenticated");
        }

        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new BusinessException("User not found"));

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BusinessException("Current password is incorrect");
        }

        // Prevent same password reuse
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new BusinessException("New password cannot be same as current password");
        }

        // Validate new password strength
        if (!PasswordValidator.isValid(request.getNewPassword())) {
            throw new BusinessException(
                    "Password must be 8-20 characters long and include uppercase, lowercase, number and special character"
            );
        }


        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

}
