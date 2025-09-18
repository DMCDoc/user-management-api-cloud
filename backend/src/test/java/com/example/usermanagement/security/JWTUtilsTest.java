package com.example.usermanagement.security;

import com.example.usermanagement.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

public class JWTUtilsTest {

    private User testUser;
    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testuser");

        // Initialisation avec le constructeur par défaut
        jwtUtils = new JwtUtils();

        // Configuration manuelle pour les tests
        jwtUtils.setClock(Clock.systemUTC());
        jwtUtils.jwtSecret = "test-secret-123456789012345678901234567890";
        jwtUtils.jwtExpirationMs = 3600000;
        jwtUtils.loadKey(jwtUtils.jwtSecret);
    }

    @Test
    void testGenerateAndValidateToken() {
        String token = jwtUtils.generateToken(testUser);

        assertNotNull(token);
        assertTrue(jwtUtils.isTokenValid(token), "Le token devrait être valide");

        String username = jwtUtils.extractUsername(token);
        assertEquals("testuser", username, "Le nom d'utilisateur doit correspondre");
    }

    @Test
    void testInvalidToken() {
        String fakeToken = "invalid.token.here";
        assertFalse(jwtUtils.isTokenValid(fakeToken), "Un token invalide doit être rejeté");
    }

    @Test
    void testTokenExpiration() {
        // Crée une nouvelle instance pour le test d'expiration
        JwtUtils jwt = new JwtUtils();
        jwt.jwtSecret = "secret-tres-long-pour-eviter-les-avertissements-1234567890";
        jwt.jwtExpirationMs = 1000;
        jwt.loadKey(jwt.jwtSecret);

        // Configure l'horloge fixe
        Clock fixedClock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
        jwt.setClock(fixedClock);

        // Génère le token
        String token = jwt.generateToken(testUser);

        // Validation immédiate
        assertTrue(jwt.isTokenValid(token), "Le token devrait être valide initialement");

        // Crée une nouvelle instance avec l'horloge avancée
        JwtUtils expiredJwt = new JwtUtils();
        expiredJwt.jwtSecret = jwt.jwtSecret;
        expiredJwt.jwtExpirationMs = jwt.jwtExpirationMs;
        expiredJwt.loadKey(expiredJwt.jwtSecret);
        expiredJwt.setClock(Clock.offset(fixedClock, Duration.ofMillis(1500)));

        // Validation après expiration
        assertFalse(expiredJwt.isTokenValid(token), "Le token devrait être expiré");
    }
}