package com.dmcdoc.usermanagement.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.dmcdoc.usermanagement.config.security.JwtService;
import com.dmcdoc.usermanagement.core.model.User;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

public class JWTUtilsTest {

    private User testUser;
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testuser");

        // Initialisation avec secret simple pour tests
        jwtService = new JwtService("test-secret-123456789012345678901234567890", 3600000L);
        jwtService.setClock(Clock.systemUTC());
    }

    @Test
    void testGenerateAndValidateToken() {
        String token = jwtService.generateToken(testUser);

        assertNotNull(token);
        // JwtService requires a UserDetails when validating; here we assert by
        // extracting username
        String username = jwtService.extractUsername(token);
        assertEquals("testuser", username, "Le nom d'utilisateur doit correspondre");
    }

    @Test
    void testInvalidToken() {
        String fakeToken = "invalid.token.here";
        assertThrows(Exception.class, () -> jwtService.extractUsername(fakeToken));
    }

    @Test
    void testTokenExpiration() {
        // Crée une nouvelle instance pour le test d'expiration
        JwtService jwt = new JwtService("secret-tres-long-pour-eviter-les-avertissements-1234567890", 1000L);

        // Configure l'horloge fixe
        Clock fixedClock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
        jwt.setClock(fixedClock);

        // Génère le token
        String token = jwt.generateToken(testUser);

        // Validation immédiate (extraction OK)
        assertEquals("testuser", jwt.extractUsername(token));

        // Avance l'horloge au-delà de l'expiration
        jwt.setClock(Clock.offset(fixedClock, Duration.ofMillis(1500)));

        // Maintenant l'extraction d'expiration devrait échouer via isTokenValid with a
        // dummy UserDetails
        // On crée un simple UserDetails impl via anonymous class
        org.springframework.security.core.userdetails.UserDetails ud = new org.springframework.security.core.userdetails.User(
                "testuser", "x", java.util.List.of());

        assertFalse(jwt.isTokenValid(token, ud), "Le token devrait être expiré");
    }
}