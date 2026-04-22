package com.kalibyte.foundry.auth.security.filter;

import com.kalibyte.foundry.auth.security.token.CustomUserDetails;
import com.kalibyte.foundry.auth.security.token.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final HandlerExceptionResolver handlerExceptionResolver;

    @Override
    protected boolean shouldNotFilter(@org.jspecify.annotations.NonNull HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/api/auth/") ||
                path.startsWith("/v3/api-docs/") ||
                path.startsWith("/swagger-ui/") ||
                path.equals("/swagger-ui.html") ||
                path.startsWith("/webjars/") ||
                path.equals("/error") ||
                path.startsWith("/actuator/");
    }

    @Override
    protected void doFilterInternal(
		    @org.jspecify.annotations.NonNull HttpServletRequest request,
		    @org.jspecify.annotations.NonNull HttpServletResponse response,
            @org.jspecify.annotations.NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt)) {
                if (tokenProvider.validateToken(jwt)) {
                    CustomUserDetails userDetails = tokenProvider.getUserDetailsFromToken(jwt);

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authentication.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    // Token is present but invalid/expired
                    log.warn("Invalid JWT token from request: {}", request.getServletPath());
                    // We can either set it to null or throw an exception to be handled by the resolver
                    throw new AuthenticationException("Invalid or expired JWT token") {};
                }
            }

        } catch (Exception ex) {
            log.error("JWT authentication failed: {}", ex.getMessage());
            handlerExceptionResolver.resolveException(request, response, null, ex);
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
