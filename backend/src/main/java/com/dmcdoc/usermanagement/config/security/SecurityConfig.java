package com.dmcdoc.usermanagement.config.security;

import lombok.RequiredArgsConstructor;

import org.springframework.core.env.Profiles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.dmcdoc.usermanagement.core.service.auth.CustomOAuth2UserService;
import com.dmcdoc.usermanagement.tenant.TenantFilter;
import org.springframework.core.env.Environment;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

        private final TenantFilter tenantFilter;
        private final JwtAuthenticationFilter jwtAuthenticationFilter;
        private final CustomAuthEntryPoint authEntryPoint;
        private final CustomAccessDeniedHandler accessDeniedHandler;
        private final CustomOAuth2UserService oauth2UserService;
        private final OAuth2AuthenticationSuccessHandler oauth2SuccessHandler;
        private final Environment environment;

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

                http
                                .cors(c -> {
                                })
                                .csrf(csrf -> csrf.disable())
                                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .exceptionHandling(ex -> ex
                                                .authenticationEntryPoint(authEntryPoint)
                                                .accessDeniedHandler(accessDeniedHandler));

                // 🔥 ORDRE ABSOLU
                // Alternative très robuste :
                http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
                // On place le TenantFilter juste avant UsernamePasswordAuthenticationFilter
                // également,
                // mais Spring les ordonnera selon l'ordre d'appel.
                http.addFilterAfter(tenantFilter, UsernamePasswordAuthenticationFilter.class);
                

                

                http.authorizeHttpRequests(auth -> auth

                                .requestMatchers(
                                                "/ping",
                                                "/swagger-ui/**",
                                                "/v3/api-docs/**",
                                                "/api/auth/**",
                                                "/api/onboarding/**"
                                                )
                                .permitAll()

                                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                                .anyRequest().authenticated());

                if (!environment.acceptsProfiles(Profiles.of("test"))) {
                        http.oauth2Login(o -> o
                                        .userInfoEndpoint(u -> u.userService(oauth2UserService))
                                        .successHandler(oauth2SuccessHandler));
                }

                return http.build();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public AuthenticationManager authenticationManager(
                        AuthenticationConfiguration cfg) throws Exception {
                return cfg.getAuthenticationManager();
        }



}
