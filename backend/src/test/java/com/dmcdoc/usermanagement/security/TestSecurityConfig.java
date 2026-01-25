package com.dmcdoc.usermanagement.security;

import com.dmcdoc.usermanagement.tenant.TenantContextFilter;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class TestSecurityConfig {

    @Bean
    SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }

    /**
     * Mock du filtre HTTP multi-tenant.
     * Obligatoire pour satisfaire SecurityConfig,
     * mais volontairement inactif en test.
     */
    @Bean
    @Primary
    TenantContextFilter tenantContextFilter() {
        return mock(TenantContextFilter.class);
    }
}
