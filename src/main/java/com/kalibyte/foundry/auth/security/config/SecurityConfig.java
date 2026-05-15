package com.kalibyte.foundry.auth.security.config;

import com.kalibyte.foundry.auth.security.filter.JwtAuthenticationFilter;
import com.kalibyte.foundry.auth.security.filter.RateLimitingFilter;
import com.kalibyte.foundry.auth.security.handler.JwtAuthenticationEntryPoint;
import com.kalibyte.foundry.auth.security.token.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationEntryPoint unauthorizedHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RateLimitingFilter rateLimitingFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .exceptionHandling(handling -> handling.authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth

                        // Public
                        .requestMatchers(
                                "/api/auth/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/swagger-ui/index.html",
                                "/webjars/**",
                                "/error"
                        ).permitAll()

                        // ========================================
                        // GST REPORTS - CA ROLE (EXCLUSIVE ACCESS)
                        // ========================================
                        .requestMatchers("/api/gst/**")
                        .hasAnyRole("CA", "ADMIN")

                        // Actuator Security
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers("/actuator/**").hasRole("ADMIN")

                        // Reports
                        .requestMatchers("/api/reports/**")
                        .hasAnyRole("ADMIN", "MANAGER", "FINANCE")

                        // Admin
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // Sales
                        .requestMatchers("/api/enquiry/**", "/api/quotation/**")
                        .hasAnyRole("ADMIN", "SALES")

                        // Production
                        .requestMatchers("/api/production/**")
                        .hasAnyRole("ADMIN", "PRODUCTION")

                        // Finance
                        .requestMatchers("/api/finance/**")
                        .hasAnyRole("ADMIN", "FINANCE")

                        // Inventory
                        .requestMatchers(
                                "/api/items/**",
                                "/api/vendors/**",
                                "/api/departments/**",
                                "/api/purchase-orders/**",
                                "/api/inwards/**",
                                "/api/material-issues/**",
                                "/api/inventory/reports/**"
                        ).hasAnyRole("ADMIN", "STORE", "FINANCE")

                        // QA
                        .requestMatchers("/api/qa/**")
                        .hasAnyRole("ADMIN", "QUALITY", "PRODUCTION")

                        // Labor Management
                        .requestMatchers(
                                "/api/labors/**",
                                "/api/attendance/**",
                                "/api/advances/**",
                                "/api/labor-reports/**",
                                "/api/payouts/**"
                        ).hasRole("ADMIN")

                        // Scrap Management
                        .requestMatchers("/api/scrap/**")
                        .hasAnyRole("ADMIN", "PRODUCTION", "QUALITY", "METALLURGIST")

                        // Furnace Management
                        .requestMatchers("/api/furnace/**", "/api/electricity-rate/**")
                        .hasAnyRole("ADMIN", "PRODUCTION")

                        .anyRequest().authenticated()
                )

                .authenticationProvider(authenticationProvider())
                .addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Restrict origins in production, allow localhost for development
        configuration.setAllowedOriginPatterns(List.of(
                "http://localhost:3000",
                "http://localhost:5173",
                "https://*.vercel.app"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin"));
        configuration.setExposedHeaders(List.of("Authorization", "Content-Disposition"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}