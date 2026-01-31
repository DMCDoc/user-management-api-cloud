package com.dmcdoc.usermanagement.security;

import com.dmcdoc.usermanagement.tenant.TenantContextFilter;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class TestSecurityConfig {

    /**
     * Chaîne de sécurité ultra-light pour les tests.
     * - Pas de JWT
     * - Pas de contraintes
     */
    @Bean
    SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }

    /**
     * AuthenticationManager minimal.
     * Suffit à satisfaire AuthController.
     */
    @Bean
    AuthenticationManager authenticationManager() {
        return authentication -> authentication;
    }

    /**
     * PasswordEncoder réel mais simple.
     * Obligatoire pour UserServiceImpl.
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Mock du filtre HTTP multi-tenant.
     * Inactif en test (le tenant est géré manuellement).
     */
    @Bean
    @Primary
    TenantContextFilter tenantContextFilter() {
        return mock(TenantContextFilter.class);
    }
}
