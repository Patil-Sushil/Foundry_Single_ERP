package com.kalibyte.foundry.auth.security.token.impl;

import com.kalibyte.foundry.auth.entity.User;
import com.kalibyte.foundry.auth.repository.UserRepository;
import com.kalibyte.foundry.auth.security.token.CustomUserDetails;
import com.kalibyte.foundry.auth.security.token.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsServiceImpl implements CustomUserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found with email: " + email)
                );

        return CustomUserDetails.create(user);
    }
}
