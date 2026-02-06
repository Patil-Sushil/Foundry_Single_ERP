package com.kalibyte.foundry.infrastructure.tenancy.filter;

import com.kalibyte.foundry.auth.security.token.CustomUserDetails;
import com.kalibyte.foundry.common.util.ContextUtil;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class TenantAwareFilter implements Filter {

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null
                && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {

            String schemaName = userDetails.getSchemaName();

            if (schemaName != null && !schemaName.isBlank()) {
                log.debug(
                        "Tenant context set to [{}] for user [{}]",
                        schemaName,
                        userDetails.getEmail()
                );
                ContextUtil.setTenant(schemaName);
            } else {
                log.warn(
                        "Authenticated user [{}] has no tenant schema assigned",
                        userDetails.getEmail()
                );
            }
        }

        try {
            chain.doFilter(request, response);
        } finally {
            ContextUtil.clear(); // 🔥 MUST clear
        }
    }
}
