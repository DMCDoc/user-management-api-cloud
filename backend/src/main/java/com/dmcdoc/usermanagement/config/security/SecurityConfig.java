package com.dmcdoc.usermanagement.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.dmcdoc.usermanagement.core.repository.UserRepository;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserRepository userRepository;
    private final CustomAuthEntryPoint authEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final JwtService jwtService; // injecte le service JWT ici

    @Value("${security.jwt.exclude-paths:/auth/**,/actuator/**}")
    private String[] excludedPaths;

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByUsername(username)
                .orElseThrow(() -> new org.springframework.security.core.userdetails.UsernameNotFoundException(
                        "Utilisateur introuvable: " + username));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // Déclare le filtre comme bean ici — plus de cycle car Spring connaît l'ordre
    // de création
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(UserDetailsService uds) {
        // on passe UserDetailsService (uds) et JwtService injecté plus haut +
        // excludedPaths
        return new JwtAuthenticationFilter(jwtService, uds, excludedPaths);
    }

    // Le JwtAuthenticationFilter bean est injecté en paramètre — pas de champ avec
    // cycle
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter)
            throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers("/users/login", "/users/register", "/users/refresh", "/ping").permitAll();

                    if (excludedPaths != null) {
                        for (String p : excludedPaths) {
                            if (p != null && !p.trim().isEmpty()) {
                                auth.requestMatchers(p.trim()).permitAll();
                            }
                        }
                    }

                    auth.requestMatchers(HttpMethod.GET, "/actuator/health", "/actuator/info").permitAll();

                    auth.requestMatchers("/api/dummy/admin").hasRole("ADMIN");
                    auth.requestMatchers("/api/dummy/only-auth").authenticated();

                    auth.anyRequest().authenticated();
                })
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler));

                http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**", "/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll()
                .anyRequest().authenticated());

        http.oauth2Login(o -> o
                .defaultSuccessUrl("/login-success", true)
                .userInfoEndpoint(userInfo -> userInfo
                        .userService(oauth2UserService())) // optional custom user service
        );

        return http.build();
    }
}
