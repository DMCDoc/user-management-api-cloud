package com.dmcdoc.usermanagement.config.security;

import lombok.RequiredArgsConstructor;

import java.util.List;

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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.dmcdoc.usermanagement.core.service.auth.CustomOAuth2UserService;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final com.dmcdoc.usermanagement.core.repository.UserRepository userRepository;
    private final CustomAuthEntryPoint authEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final JwtService jwtService;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oauth2AuthenticationSuccessHandler;

    @Value("${security.auth.username-password.enabled:true}")
    private boolean usernamePasswordEnabled;

    @Value("${security.auth.oauth2.enabled:true}")
    private boolean oauth2Enabled;

    @Value("${security.auth.magiclink.enabled:true}")
    private boolean magicLinkEnabled;

    @Value("${security.auth.jwt.enabled:true}")
    private boolean jwtEnabled;

    @Value("${security.jwt.exclude-paths:/api/auth/**,/auth/**,/debug/**,/actuator/**,/ping,/swagger-ui/**,/v3/api-docs/**,/api-docs/**}")
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
        DaoAuthenticationProvider p = new DaoAuthenticationProvider();
        p.setUserDetailsService(userDetailsService());
        p.setPasswordEncoder(passwordEncoder());
        return p;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(UserDetailsService uds) {
        return new JwtAuthenticationFilter(jwtService, uds, excludedPaths);
    }

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

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth.requestMatchers("/debug/**").permitAll());        
        http.csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler));

        http.authorizeHttpRequests(auth -> {
            // public docs & health
            auth.requestMatchers("/ping", "/swagger-ui/**", "/api-docs/**", "/v3/api-docs/**").permitAll();

            // auth endpoints
            auth.requestMatchers(
                    "/auth/**",
                    "/auth/login",
                    "/auth/register",
                    "/users/login",
                    "/users/register",
                    "/users/refresh",
                    "/api/auth/login",
                    "/api/auth/register").permitAll();

            if (magicLinkEnabled) {
                auth.requestMatchers("/users/magiclink/**", "/api/auth/magic/**", "/api/auth/magic/request",
                        "/api/auth/magic/verify").permitAll();
            }

            if (oauth2Enabled) {
                auth.requestMatchers("/oauth2/**", "/login/**", "/api/auth/oauth2/**", "/login/oauth2/**").permitAll();
            }

            auth.requestMatchers(HttpMethod.GET, "/actuator/**").permitAll();

            // example role restricted path
            auth.requestMatchers("/api/dummy/admin").hasRole("ADMIN");
            auth.requestMatchers("/api/dummy/only-auth").authenticated();

            auth.anyRequest().authenticated();
        });

        if (usernamePasswordEnabled) {
            http.authenticationProvider(authenticationProvider());
        }

        if (jwtEnabled) {
            http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        }

        if (oauth2Enabled) {
            http.oauth2Login(o -> o
                    .userInfoEndpoint(u -> u.userService(customOAuth2UserService))
                    .successHandler(oauth2AuthenticationSuccessHandler)
                    .failureHandler((req, res, ex) -> res.sendError(401, "OAuth2 authentication failed")));
        }

        return http.build();
    }
}
