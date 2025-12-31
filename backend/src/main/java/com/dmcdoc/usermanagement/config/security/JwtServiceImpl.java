package com.dmcdoc.usermanagement.config.security;

import com.dmcdoc.usermanagement.core.model.Role;
import com.dmcdoc.usermanagement.core.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class JwtServiceImpl implements JwtService {

    private final SecretKey secretKey;
    private final long expirationMs;
    
    

    public JwtServiceImpl(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.expiration}") long expirationMs) {
        System.out.println("expirationMs: " + expirationMs);
        System.out.println("Secret: " + secret);

        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    @Override
    public String generateToken(User user) {
        List<String> roles = user.getRoles()
                .stream()
                .map(Role::getName)
                .toList();

        var builder = Jwts.builder()
                // CHANGEMENT ICI : On utilise getUsername() au lieu de getEmail()
                .setSubject(user.getUsername())
                .claim("roles", roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(secretKey, SignatureAlgorithm.HS256);

        if (user.getTenantId() != null) {
            builder.claim("tenantId", user.getTenantId().toString());
        }

        return builder.compact();
    }

    @Override
    public Claims extractAllClaims(String token) {
        return extractClaims(token);
    }

    @Override
    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    @Override
    public List<String> extractRoles(String token) {
        Object rolesObj = extractClaims(token).get("roles");
        return rolesObj instanceof List
                ? ((List<?>) rolesObj).stream().map(String::valueOf).toList()
                : List.of();
    }

    @Override
    public UUID extractTenantId(String token) {
        String tenant = extractClaims(token).get("tenantId", String.class);
        return tenant != null ? UUID.fromString(tenant) : null;
    }

    @Override
    public Authentication getAuthentication(String token) {
        List<SimpleGrantedAuthority> authorities = extractRoles(token)
                .stream()
                .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role) // Assure le préfixe ROLE_
                .map(SimpleGrantedAuthority::new)
                .toList();

        return new UsernamePasswordAuthenticationToken(extractUsername(token), null, authorities);
    }

    @Override
    public boolean isSuperAdmin(String token) {
        return extractRoles(token).stream()
                .anyMatch(r -> r.equals("SUPER_ADMIN") || r.equals("ROLE_SUPER_ADMIN"));
    }

    @Override
    public boolean isValid(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String extractToken(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        return (auth != null && auth.startsWith("Bearer "))
                ? auth.substring(7)
                : null;
    }

    private Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
