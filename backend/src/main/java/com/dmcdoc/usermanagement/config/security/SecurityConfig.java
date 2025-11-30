package com.dmcdoc.usermanagement.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.dmcdoc.usermanagement.core.repository.UserRepository;
import com.dmcdoc.usermanagement.core.service.auth.CustomOAuth2UserService;
import com.dmcdoc.usermanagement.tenant.TenantFilter;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserRepository userRepository;
    private final CustomAuthEntryPoint authEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final JwtService jwtService;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oauth2AuthenticationSuccessHandler;
    private final TenantFilter tenantFilter;

    @Value("${security.auth.username-password.enabled:true}")
    private boolean usernamePasswordEnabled;

    @Value("${security.auth.oauth2.enabled:true}")
    private boolean oauth2Enabled;

    @Value("${security.auth.magiclink.enabled:true}")
    private boolean magicLinkEnabled;

    @Value("${security.auth.jwt.enabled:true}")
    private boolean jwtEnabled;

    @Value("${security.jwt.exclude-paths:/api/auth/**,/swagger-ui/**,/v3/api-docs/**,/ping}")
    private String[] excludedPaths;

    /*
     * ----------------------------------------
     * UserDetailsService
     * -----------------------------------------
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return email -> userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }

    /*
     * ----------------------------------------
     * Password Encoder
     * -----------------------------------------
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /*
     * ----------------------------------------
     * AuthenticationManager
     * -----------------------------------------
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }

    /*
     * ----------------------------------------
     * CORS
     * -----------------------------------------
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /*
     * ----------------------------------------
     * JWT Filter
     * -----------------------------------------
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(UserDetailsService uds) {
        return new JwtAuthenticationFilter(jwtService, uds);
    }

    /*
     * ----------------------------------------
     * SecurityFilterChain
     * -----------------------------------------
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
            JwtAuthenticationFilter jwtFilter) throws Exception {

        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));
        http.csrf(csrf -> csrf.disable());
        http.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.exceptionHandling(ex -> ex
                .authenticationEntryPoint(authEntryPoint)
                .accessDeniedHandler(accessDeniedHandler));

        /*
         * ============================================================
         * Filtres : ordre correct
         * - TenantFilter AVANT tout -> tenant disponible partout
         * - JWT avant UsernamePassword
         * ============================================================
         */
        http.addFilterBefore(tenantFilter, UsernamePasswordAuthenticationFilter.class);

        if (jwtEnabled) {
            http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        }

        /*
         * ============================================================
         * Autorizations
         * ============================================================
         */
        

        http.authorizeHttpRequests(auth -> {

            // Public
            auth.requestMatchers("/ping",
                    "/swagger-ui/**", "/v3/api-docs/**", "/api-docs/**").permitAll();

            // Authentication public
            auth.requestMatchers("/api/auth/**", "/auth/**",
                    "/users/login", "/users/register",
                    "/users/refresh",
                    "/api/auth/login", "/api/auth/register",
                    "/api/auth/forgot-password", "/api/auth/reset-password").permitAll();

            if (magicLinkEnabled) {
                auth.requestMatchers(
                        "/users/magiclink/**",
                        "/api/auth/magic/**",
                        "/api/auth/magic/request",
                        "/api/auth/magic/verify").permitAll();
            }

            if (oauth2Enabled) {
                auth.requestMatchers("/oauth2/**",
                        "/login/**",
                        "/api/auth/oauth2/**",
                        "/login/oauth2/**").permitAll();
            }

            // Global admin (multi-tenant compatible)
            auth.requestMatchers("/api/admin/**").hasRole("ADMIN");

            // Dummy demo
            auth.requestMatchers("/api/dummy/admin").hasRole("ADMIN");
            auth.requestMatchers("/api/dummy/only-auth").authenticated();

            // Everything else requires auth
            auth.anyRequest().authenticated();
        });

        /*
         * ============================================================
         * OAuth2 login (optionnel)
         * ============================================================
         */
        if (oauth2Enabled) {
            http.oauth2Login(o -> o
                    .userInfoEndpoint(u -> u.userService(customOAuth2UserService))
                    .successHandler(oauth2AuthenticationSuccessHandler)
                    .failureHandler((req, res, ex) -> res.sendError(401, "OAuth2 authentication failed")));
        }

        return http.build();
    }
}
