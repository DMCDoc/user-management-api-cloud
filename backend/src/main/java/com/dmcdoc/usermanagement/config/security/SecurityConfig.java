package com.dmcdoc.usermanagement.config.security;

import com.dmcdoc.usermanagement.tenant.TenantContextFilter;
import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

        // 🔐 Auth JWT
        private final JwtAuthenticationFilter jwtAuthenticationFilter;

        // 🏢 Multi-tenant context
        private final TenantContextFilter tenantContextFilter;

        // 👤 UserDetails
        private final CustomUserDetailsService customUserDetailsService;

        /**
         * 🔐 Chaîne de sécurité principale
         */
        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

                http
                                // ❌ Pas de CSRF (API stateless)
                                .csrf(csrf -> csrf.disable())

                                // ❌ Pas de HTTP Basic
                                .httpBasic(httpBasic -> httpBasic.disable())

                                // 📜 Règles d'accès
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(
                                                                "/ping",
                                                                "/swagger-ui/**",
                                                                "/v3/api-docs/**",
                                                                "/api/auth/**",
                                                                "/api/onboarding/**",
                                                                "/error")
                                                .permitAll()
                                                .anyRequest().authenticated())

                                // 🔁 Stateless (JWT only)
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                                // 🔐 Provider DAO
                                .authenticationProvider(authenticationProvider())

                                // ⚠️ ORDRE CRITIQUE DES FILTRES
                                // 1️⃣ JWT → authentifie
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                                // 2️⃣ Tenant → contrôle isolation
                                .addFilterAfter(tenantContextFilter, JwtAuthenticationFilter.class);

                return http.build();
        }

        /**
         * 🔑 Password encoder
         */
        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        /**
         * 🔐 AuthenticationManager (utilisé par /login)
         */
        @Bean
        public AuthenticationManager authenticationManager(
                        AuthenticationConfiguration config) throws Exception {
                return config.getAuthenticationManager();
        }

        /**
         * 👤 AuthenticationProvider basé sur UserDetailsService
         */
        @Bean
        public AuthenticationProvider authenticationProvider() {
                DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
                provider.setUserDetailsService(customUserDetailsService);
                provider.setPasswordEncoder(passwordEncoder());
                return provider;
        }
}
