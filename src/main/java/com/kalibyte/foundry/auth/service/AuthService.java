package com.kalibyte.foundry.auth.service;

import com.kalibyte.foundry.auth.dto.*;
import com.kalibyte.foundry.auth.entity.Role;
import com.kalibyte.foundry.auth.entity.User;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface AuthService {

    LoginResponse login(LoginRequest request);
    TokenRefreshResponse refreshToken(TokenRefreshRequest request);
    void logout(String refreshToken);
    void createUser(UserRegistrationRequest request);

    void changePassword(ChangePasswordRequest request);

    List<Role> getRoles();

    List<User> getAllUsers();

    UserResponse getUserById(UUID id);

    void deleteUser(UUID id);

    void disableUser(UUID id);

    void enableUser(UUID id);

    Page<UserResponse> getAllUsers(int page, int size);
}
