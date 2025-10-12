package com.dmcdoc.usermanagement.config.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.dmcdoc.usermanagement.core.model.User;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Clock;
import java.util.Date;

@Component
public class JwtUtils {

    // Changement de visibilité pour les tests
    public String jwtSecret;
    public long jwtExpirationMs;
    protected Key key;
    protected boolean isGeneratedKey = false;
    protected Clock clock = Clock.systemUTC();

    @PostConstruct
    public void init() {
        this.jwtSecret = System.getenv("JWT_SECRET");
        String expStr = System.getenv("JWT_EXPIRATION");

        try {
            this.jwtExpirationMs = expStr != null ? Long.parseLong(expStr) : 3600000;
        } catch (NumberFormatException e) {
            this.jwtExpirationMs = 3600000;
        }

        loadKey(this.jwtSecret);
        logLoadedValues();
    }

    // Changement de visibilité pour les tests
    public void loadKey(String secret) {
        if (secret == null || secret.trim().isEmpty()) {
            this.key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
            this.isGeneratedKey = true;
            return;
        }

        String cleanSecret = secret.trim().replace("\"", "").replace("'", "");
        byte[] keyBytes = cleanSecret.getBytes(StandardCharsets.UTF_8);

        if (keyBytes.length < 32) {
            this.key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
            this.isGeneratedKey = true;
        } else {
            this.key = Keys.hmacShaKeyFor(keyBytes);
            this.isGeneratedKey = false;
        }
    }

    private void logLoadedValues() {
        Logger logger = LoggerFactory.getLogger(JwtUtils.class);
        logger.info("JWTUtils initialisé avec:");
        logger.info("   • Clé = {}", (isGeneratedKey ? "générée aléatoirement" : "chargée depuis l'environnement"));
        logger.info("   • Expiration = {} ms", jwtExpirationMs);
    }

    public String generateToken(User user) {
        final Date now = Date.from(clock.instant());
        final Date expiry = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder().setSubject(user.getUsername()).setIssuedAt(now).setExpiration(expiry).signWith(key)
                .compact();
    }

    public String extractUsername(String token) {
        return parseClaims(token).getBody().getSubject();
    }

    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Jws<Claims> parseClaims(String token) {
        return Jwts.parserBuilder().setClock(() -> Date.from(clock.instant())).setSigningKey(key).build()
                .parseClaimsJws(token);
    }

    // Ajout du setter pour les tests
    public void setClock(Clock clock) {
        this.clock = clock;
    }
}