package com.dmcdoc.usermanagement.config.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import com.dmcdoc.usermanagement.core.model.User;
import com.dmcdoc.usermanagement.core.model.Role;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.*;

@Service
public class JwtService {

    private Key signingKey;

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration:3600000}")
    private long expirationMs;

    @PostConstruct
    void init() {
        signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(User user) {

        Map<String, Object> claims = new HashMap<>();
        claims.put("tenant", user.getTenantId() != null ? user.getTenantId().toString(): "GLOBAL");
        claims.put("roles",
                user.getRoles().stream()
                        .map(Role::getName)
                        .toList());

        return buildToken(claims, user.getUsername());
    }

    private String buildToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expirationMs))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails user) {
        return extractUsername(token).equals(user.getUsername())
                && !isExpired(token);
    }

    public boolean isTokenValid(String token) {
        return !isExpired(token);
    }

    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    public String extractTenant(String token) {
        return extractClaims(token).get("tenant", String.class);
    }

    public UUID extractTenantId(String token) {
        String tenant = extractClaims(token).get("tenant", String.class);

        if (tenant == null || tenant.equalsIgnoreCase("GLOBAL")) {
            return null; // super-admin ou contexte global
        }

        return UUID.fromString(tenant);
    }

    public List<String> extractRoles(String token) {
        List<?> roles = extractClaims(token).get("roles", List.class);
        return roles != null ? roles.stream()
                .map(Object::toString)
                .toList() : new ArrayList<>();
    }

    public boolean tokenHasRole(String token, String role) {
        if (token == null || role == null) {
            return false;
        }
        List<String> roles = extractRoles(token);
        return roles.stream().anyMatch(r -> r.equalsIgnoreCase(role));
    }

    public boolean isValid(String token) {
    try {
        extractClaims(token);
        return true;
    } catch (JwtException e) {
        return false;
    }
}

    public Authentication getAuthentication(String token) {

    String username = extractUsername(token);
    UUID tenantId = extractTenantId(token);
    boolean superAdmin = extractRoles(token).contains("ROLE_SUPER_ADMIN");

    JwtPrincipal principal = new JwtPrincipal(username, tenantId, superAdmin);

    List<GrantedAuthority> authorities = new ArrayList<>(
            extractRoles(token).stream()
                    .map(SimpleGrantedAuthority::new)
                    .toList());

    return new UsernamePasswordAuthenticationToken(
            principal,
            null,
            authorities);
}


    public boolean isSuperAdmin(String token) {
        return tokenHasRole(token, "ROLE_SUPER_ADMIN");
    }

    private boolean isExpired(String token) {
        return extractClaims(token)
                .getExpiration()
                .before(new Date());
    }

    private Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
