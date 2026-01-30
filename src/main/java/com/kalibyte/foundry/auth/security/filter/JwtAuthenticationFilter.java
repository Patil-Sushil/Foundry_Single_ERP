package com.kalibyte.foundry.auth.security.filter;

import com.kalibyte.foundry.auth.security.token.JwtTokenProvider;
import com.kalibyte.foundry.auth.security.token.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        log.debug("Processing request to: {}", request.getRequestURI());
        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt)) {
                log.debug("JWT found in request");
                if (tokenProvider.validateToken(jwt)) {
                    String email = tokenProvider.getUsernameFromJWT(jwt);
                    log.debug("JWT validated for user: {}", email);

                    UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("SecurityContext successfully populated for user: {}", email);
                } else {
                    log.warn("JWT validation failed for token starting with: {}", jwt.substring(0, Math.min(jwt.length(), 10)));
                }
            } else {
                log.trace("No JWT token found in Authorization header");
            }
        } catch (Exception ex) {
            log.error("Could not set user authentication in security context", ex);
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken)) {
            // Case-insensitive check for "Bearer "
            if (bearerToken.regionMatches(true, 0, "Bearer ", 0, 7)) {
                return bearerToken.substring(7).trim();
            }
        }
        return null;
    }
}
