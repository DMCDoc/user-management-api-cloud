package com.dmcdoc.usermanagement.security;

import com.dmcdoc.sharedcommon.dto.RegisterRequest;
import com.dmcdocusermanagement.core.model.User;
import com.dmcdoc.usermanagement.core.repository.UserRepository;
import com.dmcdoc.usermanagement.core.service.AuthenticationService;


import jakarta.transaction.Transactional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = com.example.usermanagement.UserManagementApplication.class)
@Transactional
@Sql(statements = {
    "TRUNCATE TABLE user_roles CASCADE",
    "TRUNCATE TABLE users CASCADE",
    "TRUNCATE TABLE roles CASCADE"
}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class PasswordEncodingTest {

    @Autowired
    private AuthenticationService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final String testEmail = "alice@example.com";

    @AfterEach
    void cleanup() {
        // Supprime l'utilisateur de test si présent
        userRepository.findByEmail(testEmail).ifPresent(userRepository::delete);
    }

    @Test
    void testRegisterAndPasswordEncoding() {
        // 1️⃣ Enregistrer un nouvel utilisateur
        RegisterRequest req = new RegisterRequest();
        req.setUsername("alice");
        req.setPassword("mypassword");
        req.setFullName("Alice Wonderland");
        req.setEmail(testEmail);

        authService.register(req);

        // 2️⃣ Retrouver l'utilisateur en DB
        User saved = userRepository.findByUsername("alice").orElseThrow();

        // 3️⃣ Vérifier que le mot de passe est encodé
        assertThat(saved.getPassword()).isNotEqualTo("mypassword");

        // 4️⃣ Vérifier que le PasswordEncoder valide bien
        assertThat(passwordEncoder.matches("mypassword", saved.getPassword())).isTrue();
    }
}
