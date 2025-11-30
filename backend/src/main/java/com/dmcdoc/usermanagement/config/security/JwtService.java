package com.dmcdoc.usermanagement.config.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.time.Clock;

@Service
public class JwtService {

    private final String secretKey;
    private final long jwtExpirationMs;
    private Clock clock = Clock.systemUTC();

    public JwtService(@Value("${jwt.secret}") String secretKey,
            @Value("${jwt.expiration:3600000}") long jwtExpirationMs) {
        this.secretKey = secretKey;
        this.jwtExpirationMs = jwtExpirationMs;
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String extractEmail(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return claims.getSubject();
        } catch (JwtException e) {
            return null;
        }
    }

    /**
     * Génère un token JWT à partir de UserDetails et du tenantId.
     * Utilise le claim "roles" et le claim "tenant".
     */
    public String generateToken(UserDetails userDetails, String tenantId) {
        Map<String, Object> claims = Map.of(
                "roles", userDetails.getAuthorities().stream().map(a -> a.getAuthority()).toList(),
                "tenant", tenantId);

        Date now = Date.from(clock.instant());
        Date exp = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Version générique si tu veux simplement ajouter des claims.
     */
    public String generateTokenWithClaims(UserDetails userDetails, Map<String, Object> extraClaims) {
        Date now = Date.from(clock.instant());
        Date exp = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Surcharge pratique : génère un token directement depuis notre entité `User`.
     * Ceci permet aux appels passant un `User` (au lieu de `UserDetails`) de
     * fonctionner.
     */
    public String generateToken(com.dmcdoc.usermanagement.core.model.User user) {
        try {
            List<String> roles = user.getRoles() == null ? List.of()
                    : user.getRoles().stream().map(r -> r.getName()).toList();
            Map<String, Object> claims = Map.of(
                    "roles", roles,
                    "tenant", user.getTenantId() == null ? null : user.getTenantId().toString());

            Date now = Date.from(clock.instant());
            Date exp = new Date(now.getTime() + jwtExpirationMs);

            return Jwts.builder()
                    .setClaims(claims)
                    .setSubject(user.getUsername())
                    .setIssuedAt(now)
                    .setExpiration(exp)
                    .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                    .compact();
        } catch (JwtException ex) {
            throw ex;
        }
    }

    // 🔹 Pour extraire les rôles depuis un token
    public List<String> extractRoles(String token) {
        try {
            Claims claims = extractAllClaims(token);
            Object rolesObj = claims.get("roles");
            if (rolesObj instanceof List<?>) {
                return ((List<?>) rolesObj).stream()
                        .map(Object::toString)
                        .toList();
            }
        } catch (JwtException ignored) {
        }
        return List.of();
    }

    /**
     * Récupère le tenant stocké dans le claim "tenant" (String)
     */
    public String extractTenant(String token) {
        try {
            Claims claims = extractAllClaims(token);
            Object t = claims.get("tenant");
            return t == null ? null : t.toString();
        } catch (JwtException e) {
            return null;
        }
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username != null && username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        Date exp = extractExpiration(token);
        return exp.before(Date.from(clock.instant()));
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(getSignInKey()).build().parseClaimsJws(token).getBody();
    }

    private Key getSignInKey() {
        try {
            // Essayer d'abord en Base64
            byte[] keyBytes = Base64.getDecoder().decode(secretKey);
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (IllegalArgumentException e) {
            // Sinon fallback UTF-8
            return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        }
    }

    @PostConstruct
    public void checkConfig() {
        // Optionnel : vérifier que le secret a une longueur minimale pour HS256
        try {
            getSignInKey();
        } catch (Exception ex) {
            throw new IllegalStateException("Invalid JWT signing key configuration", ex);
        }
    }

    // Ajout pour faciliter les tests (similaire à JwtUtils)
    public void setClock(Clock clock) {
        this.clock = clock == null ? Clock.systemUTC() : clock;
    }
}
