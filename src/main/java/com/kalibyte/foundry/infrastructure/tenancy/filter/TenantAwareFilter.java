package com.kalibyte.foundry.infrastructure.tenancy.filter;

import com.kalibyte.foundry.common.util.ContextUtil;
import com.kalibyte.foundry.auth.security.token.CustomUserDetails;
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
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            if (userDetails.getSchemaName() != null) {
                log.debug("Setting tenant context to: {} for user: {}", userDetails.getSchemaName(), userDetails.getEmail());
                ContextUtil.setTenant(userDetails.getSchemaName());
            }
        }

        try {
            chain.doFilter(request, response);
        } finally {
            ContextUtil.clear();
        }
    }
}
