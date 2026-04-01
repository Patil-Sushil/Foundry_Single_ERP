package com.kalibyte.foundry.common.util;

import com.kalibyte.foundry.auth.security.token.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

public class SecurityUtils {

    private static final String SYSTEM_USER = "SYSTEM";

    private SecurityUtils() {
    }

    public static UUID getCurrentUserId() {
        CustomUserDetails user = getCurrentUserOrNull();
        return user != null ? user.getId() : null;
    }

    public static String getCurrentUsername() {
        CustomUserDetails user = getCurrentUserOrNull();
        return user != null ? user.getUsername() : SYSTEM_USER;
    }

    public static CustomUserDetails getCurrentUser() {

        CustomUserDetails user = getCurrentUserOrNull();

        if (user == null) {
            throw new IllegalStateException("No authenticated user found");
        }

        return user;
    }

    private static CustomUserDetails getCurrentUserOrNull() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null &&
                auth.isAuthenticated() &&
                auth.getPrincipal() instanceof CustomUserDetails userDetails) {

            return userDetails;
        }

        return null;
    }
}