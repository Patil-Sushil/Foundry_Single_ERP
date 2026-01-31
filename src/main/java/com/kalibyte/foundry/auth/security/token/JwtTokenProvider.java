package com.kalibyte.foundry.auth.security.token;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpirationInMs;

    @Value("${jwt.refresh-expiration:604800000}") // Default 7 days
    private long refreshExpirationInMs;

    private SecretKey key;

    @PostConstruct
    public void init() {
        // Ensure the secret is Base64 encoded in your properties
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(Authentication authentication) {
        CustomUserDetails userPrincipal = (CustomUserDetails) authentication.getPrincipal();
        return generateToken(userPrincipal, jwtExpirationInMs);
    }

    public String generateRefreshToken(Authentication authentication) {
        CustomUserDetails userPrincipal = (CustomUserDetails) authentication.getPrincipal();
        return generateToken(userPrincipal, refreshExpirationInMs);
    }

    public String generateToken(CustomUserDetails userPrincipal, long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        List<String> roles = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return Jwts.builder()
                .subject(userPrincipal.getUsername()) // email
                .claim("userId", userPrincipal.getId())
                .claim("tenantId", userPrincipal.getTenantId())
                .claim("tenantCode", userPrincipal.getTenantCode())
                .claim("tenantSchema", userPrincipal.getSchemaName())
                .claim("roles", roles)
                .issuer("foundry-app")
                .id(UUID.randomUUID().toString()) // jti
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Long getUserIdFromToken(String token) {
        return extractClaim(token, claims -> claims.get("userId", Long.class));
    }

    public String getTenantSchemaFromToken(String token) {
        return extractClaim(token, claims -> claims.get("tenantSchema", String.class));
    }

    public Long getTenantIdFromToken(String token) {
        return extractClaim(token, claims -> claims.get("tenantId", Long.class));
    }
    
    public String getTenantCodeFromToken(String token) {
        return extractClaim(token, claims -> claims.get("tenantCode", String.class));
    }

    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        return extractClaim(token, claims -> claims.get("roles", List.class));
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .clockSkewSeconds(30) // Allow 30 seconds skew
                    .build()
                    .parseSignedClaims(authToken);
            return true;
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token");
        } catch (JwtException | IllegalArgumentException ex) {
            log.error("Invalid JWT token: {}", ex.getMessage());
        }
        return false;
    }
}
