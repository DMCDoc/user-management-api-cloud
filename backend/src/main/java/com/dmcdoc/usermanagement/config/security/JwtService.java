package com.dmcdoc.usermanagement.config.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Clock;
import java.util.*;
import java.util.function.Function;

@Service
public class JwtService {

    private final String secretKeyRaw;
    private final long jwtExpirationMs;
    private Key signingKey;

    private Clock clock = Clock.systemUTC();

    public JwtService(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.expiration:3600000}") long jwtExpirationMs) {
        this.secretKeyRaw = secretKey;
        this.jwtExpirationMs = jwtExpirationMs;
    }

    /*
     * ---------------------------------------------------------
     * INITIALIZATION
     * ---------------------------------------------------------
     */
    @PostConstruct
    public void init() {
        this.signingKey = buildSigningKey(secretKeyRaw);

        if (this.signingKey == null) {
            throw new IllegalStateException("Invalid JWT signing key configuration.");
        }
    }

    private Key buildSigningKey(String raw) {
        try {
            // Try Base64
            byte[] decoded = Base64.getDecoder().decode(raw);
            return Keys.hmacShaKeyFor(decoded);
        } catch (Exception ignored) {
            // Fallback to UTF-8 bytes
            byte[] bytes = raw.getBytes(StandardCharsets.UTF_8);
            return Keys.hmacShaKeyFor(bytes);
        }
    }

    /*
     * ---------------------------------------------------------
     * EXTRACTION
     * ---------------------------------------------------------
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractEmail(String token) {
        try {
            return extractUsername(token);
        } catch (JwtException e) {
            return null;
        }
    }

    public String extractTenant(String token) {
        try {
            Claims claims = extractAllClaims(token);
            Object value = claims.get("tenant");
            return value == null ? null : value.toString();
        } catch (JwtException e) {
            return null;
        }
    }

    public List<String> extractRoles(String token) {
        try {
            Claims claims = extractAllClaims(token);
            Object rolesObj = claims.get("roles");

            if (rolesObj instanceof List<?> list) {
                return list.stream()
                        .map(Object::toString)
                        .toList();
            }

        } catch (JwtException ignored) {
        }

        return List.of();
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(extractAllClaims(token));
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /*
     * ---------------------------------------------------------
     * TOKEN VALIDATION
     * ---------------------------------------------------------
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = extractUsername(token);

        return username != null
                && username.equals(userDetails.getUsername())
                && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        Date exp = extractClaim(token, Claims::getExpiration);
        return exp.before(Date.from(clock.instant()));
    }

    /*
     * ---------------------------------------------------------
     * TOKEN GENERATION
     * ---------------------------------------------------------
     */
    public String generateToken(UserDetails userDetails, String tenantId) {

        Map<String, Object> claims = Map.of(
                "roles", userDetails.getAuthorities().stream().map(a -> a.getAuthority()).toList(),
                "tenant", tenantId);

        return buildToken(claims, userDetails.getUsername());
    }

    public String generateToken(com.dmcdoc.usermanagement.core.model.User user) {

        List<String> roles = user.getRoles() == null ? List.of()
                : user.getRoles().stream().map(r -> r.getName()).toList();

        Map<String, Object> claims = Map.of(
                "roles", roles,
                "tenant", user.getTenantId() == null ? null : user.getTenantId().toString());

        return buildToken(claims, user.getUsername());
    }

    public String generateTokenWithClaims(UserDetails userDetails, Map<String, Object> extraClaims) {
        return buildToken(extraClaims, userDetails.getUsername());
    }

    private String buildToken(Map<String, Object> claims, String subject) {
        Date now = Date.from(clock.instant());
        Date exp = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /*
     * ---------------------------------------------------------
     * TESTING UTILITIES
     * ---------------------------------------------------------
     */
    public void setClock(Clock clock) {
        this.clock = clock == null ? Clock.systemUTC() : clock;
    }
}
