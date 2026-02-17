package com.kalibyte.foundry.auth.service;

import com.kalibyte.foundry.auth.dto.ChangePasswordRequest;
import com.kalibyte.foundry.auth.dto.LoginRequest;
import com.kalibyte.foundry.auth.dto.LoginResponse;
import com.kalibyte.foundry.auth.dto.UserRegistrationRequest;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    void createUser(UserRegistrationRequest request);

    void changePassword(ChangePasswordRequest request);
}
