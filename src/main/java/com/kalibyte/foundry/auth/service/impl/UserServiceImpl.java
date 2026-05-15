package com.kalibyte.foundry.auth.service.impl;

import com.kalibyte.foundry.auth.entity.User;
import com.kalibyte.foundry.auth.repository.UserRepository;
import com.kalibyte.foundry.auth.service.UserService;
import com.kalibyte.foundry.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User not found"));
    }
}
