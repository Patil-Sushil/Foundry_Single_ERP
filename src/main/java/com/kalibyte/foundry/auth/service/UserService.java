package com.kalibyte.foundry.auth.service;

import com.kalibyte.foundry.auth.entity.User;

public interface UserService {

    User getByEmail(String email);
}
