package com.kalibyte.foundry.common.util;

import com.kalibyte.foundry.auth.security.token.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

public class SecurityUtils {

    private SecurityUtils() {
    }

    public static UUID getCurrentUserId() {
        return getCurrentUser().getId();
    }

    public static String getCurrentUsername() {
        return getCurrentUser().getUsername();
    }

    public static CustomUserDetails getCurrentUser() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails;
        }

        throw new IllegalStateException("No authenticated user found");

    }
}
