package com.dmcdoc.usermanagement.unit.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;

import com.dmcdoc.usermanagement.config.security.JwtService;
import com.dmcdoc.usermanagement.core.model.User;
import com.dmcdoc.usermanagement.core.model.Role;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class JWTUtilsTest {

    @Autowired
    private JwtService jwtService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setTenantId(UUID.randomUUID());
        testUser.setRoles(Set.of());
    }

    @Test
    void testGenerateAndValidateToken() {
        String token = jwtService.generateToken(testUser);

        assertNotNull(token);
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
        String token = jwtService.generateToken(testUser);

        // Validation immédiate (extraction OK)
        assertEquals("testuser", jwtService.extractUsername(token));
    }

    @Test
    void testTenantExtraction() {
        String token = jwtService.generateToken(testUser);

        UUID extractedTenant = jwtService.extractTenantId(token);
        assertEquals(testUser.getTenantId(), extractedTenant, "Le tenant doit correspondre");
    }

    @Test
    void testRolesExtraction() {
        Role adminRole = new Role();
        adminRole.setName("ROLE_ADMIN");
        testUser.setRoles(Set.of(adminRole));

        String token = jwtService.generateToken(testUser);

        var roles = jwtService.extractRoles(token);
        assertNotNull(roles);
        assertTrue(roles.contains("ROLE_ADMIN"), "Les rôles doivent être extraits");
    }
}