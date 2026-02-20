package com.kalibyte.foundry.auth.service.impl;

import com.kalibyte.foundry.auth.dto.*;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;
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

        if (!PasswordValidator.isValid(request.getPassword())) {
            throw new BusinessException(
                    "Password must be 8-20 characters long and include uppercase, lowercase, number and special character"
            );
        }

        var role = roleRepository.findByName(request.getRole())
                .orElseThrow(() -> new BusinessException("Invalid role"));

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEnabled(true);
        user.setRoles(Set.of(role));

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

    @Override
    public List<Role> getRoles() {
        return roleRepository.findAll();
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public UserResponse getUserById(UUID id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("User not found"));

        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .enabled(user.isEnabled())
                .roles(user.getRoles().stream()
                        .map(role -> role.getName().name())
                        .toList())
                .build();
    }

    // Prevent users from deleting their own accounts
    @Override
    public void deleteUser(UUID id) {

        CustomUserDetails currentUser =
                (CustomUserDetails) SecurityContextHolder.getContext()
                        .getAuthentication().getPrincipal();

        if (currentUser.getId().equals(id)) {
            throw new BusinessException("You cannot delete your own account");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("User not found"));

        userRepository.delete(user);
    }

    @Override
    public void disableUser(UUID id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("User not found"));

        user.setEnabled(false);
        userRepository.save(user);
    }

    @Override
    public void enableUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("User not found"));

        user.setEnabled(true);
        userRepository.save(user);
    }

    // Implement pagination for user listing
    @Override
    public Page<UserResponse> getAllUsers(int page, int size) {

        Page<User> users = userRepository.findAll(PageRequest.of(page, size));

        return users.map(user -> UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .enabled(user.isEnabled())
                .roles(user.getRoles().stream()
                        .map(role -> role.getName().name())
                        .toList())
                .build());
    }





}

