package com.kalibyte.foundry.auth.security.filter;

import com.kalibyte.foundry.auth.security.token.CustomUserDetails;
import com.kalibyte.foundry.auth.security.token.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                // Stateless: Extract user details directly from token claims
                String email = tokenProvider.getUsernameFromToken(jwt);
                Long userId = tokenProvider.getUserIdFromToken(jwt);
                Long tenantId = tokenProvider.getTenantIdFromToken(jwt);
                String tenantCode = tokenProvider.getTenantCodeFromToken(jwt);
                String schemaName = tokenProvider.getTenantSchemaFromToken(jwt);
                List<String> roles = tokenProvider.getRolesFromToken(jwt);

                List<SimpleGrantedAuthority> authorities = roles.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

                // Reconstruct CustomUserDetails
                CustomUserDetails userDetails = new CustomUserDetails(
                        userId,
                        email,
                        null, // Password is not needed for token auth
                        authorities,
                        tenantId,
                        tenantCode,
                        schemaName
                );

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.trace("Authenticated user: {} for tenant: {}", email, tenantCode);
            }
        } catch (Exception ex) {
            log.error("JWT authentication failed", ex);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired JWT");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
