package com.dmcdoc.usermanagement.config.security;

import com.dmcdoc.usermanagement.core.model.OAuth2Provider;
import com.dmcdoc.usermanagement.core.model.Role;
import com.dmcdoc.usermanagement.core.model.User;
import com.dmcdoc.usermanagement.core.repository.RoleRepository;
import com.dmcdoc.usermanagement.core.repository.UserRepository;
import com.dmcdoc.usermanagement.core.service.RefreshTokenService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

        private final UserRepository userRepository;
        private final RoleRepository roleRepository;
        private final RefreshTokenService refreshTokenService;
        private final JwtService jwtService;

        @Override
        public void onAuthenticationSuccess(HttpServletRequest request,
                        HttpServletResponse response,
                        Authentication authentication)
                        throws IOException, ServletException {

                OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
                String email = (String) oAuth2User.getAttributes().get("email");

                if (email == null) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                        "Email manquant dans la réponse OAuth2");
                }

                String registrationId = authentication.getAuthorities()
                                .stream()
                                .findFirst()
                                .map(a -> a.getAuthority().toUpperCase())
                                .orElse("UNKNOWN");

                OAuth2Provider provider = switch (registrationId) {
                        case "GOOGLE" -> OAuth2Provider.GOOGLE;
                        case "GITHUB" -> OAuth2Provider.GITHUB;
                        case "FACEBOOK" -> OAuth2Provider.FACEBOOK;
                        default -> OAuth2Provider.LOCAL;
                };

                // 🔹 find or create user
                User user = userRepository.findByEmail(email).orElseGet(() -> {
                        Role userRole = roleRepository.findByName("ROLE_USER")
                                        .orElseThrow(() -> new IllegalStateException("Role USER manquant en DB"));

                        User newUser = User.builder()
                                        .username(email.split("@")[0])
                                        .email(email)
                                        .fullName(email)
                                        .roles(Set.of(userRole))
                                        .password(UUID.randomUUID().toString())
                                        .enabled(true)
                                        .provider(provider)
                                        .build();

                        return userRepository.save(newUser);
                });

                String jwt = jwtService.generateToken(user);
                var refreshToken = refreshTokenService.create(user);

                log.info("✅ Authentification OAuth2 réussie pour {} via {}", user.getEmail(), provider);

                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentType("application/json");
                response.getWriter().write(
                                "{\"accessToken\": \"" + jwt + "\", " +
                                                "\"refreshToken\": \"" + refreshToken.getToken() + "\"}");
                response.getWriter().flush();
        }
}
