// 3️⃣ Appel depuis OAuth2 Success Handler (cas Google, etc.)

package com.dmcdoc.usermanagement.config.security;

import com.dmcdoc.usermanagement.core.model.OAuth2Provider;
import com.dmcdoc.usermanagement.core.model.User;
import com.dmcdoc.usermanagement.core.repository.UserRepository;
import com.dmcdoc.usermanagement.core.service.auth.RefreshTokenService;
import com.dmcdoc.usermanagement.tenant.TenantContext;
import com.dmcdoc.usermanagement.core.service.tenant.TenantProvisioningService;

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
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler
                extends SimpleUrlAuthenticationSuccessHandler {

        private final UserRepository userRepository;
        private final RefreshTokenService refreshTokenService;
        private final JwtService jwtService;
        private final TenantProvisioningService tenantProvisioningService;

        @Override
        public void onAuthenticationSuccess(
                        HttpServletRequest request,
                        HttpServletResponse response,
                        Authentication authentication)
                        throws IOException, ServletException {

                OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
                String email = (String) oAuth2User.getAttributes().get("email");

                if (email == null) {
                        throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
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

                // 🔹 Find or create OAuth2 user
                User user = userRepository.findByEmail(email)
                                .orElseGet(() -> userRepository.save(
                                                User.builder()
                                                                .username(email.split("@")[0])
                                                                .email(email)
                                                                .fullName(email)
                                                                .password(UUID.randomUUID().toString())
                                                                .provider(provider)
                                                                .build()));

                // 🔥 TENANT RESOLUTION + PROVISIONING
                UUID tenantId = user.getTenantId() != null
                                ? user.getTenantId()
                                : UUID.randomUUID();

                // ensure tenant schema exists (provisioning)
                if (user.getTenantId() == null) {
                        tenantProvisioningService.provisionIfNeeded(tenantId, user);
                }

                // if user had no tenant assigned, attach and persist
                if (user.getTenantId() == null) {
                        user.setTenantId(tenantId);
                        userRepository.save(user);
                }

                TenantContext.setTenantId(tenantId);

                // 🔐 Tokens
                String accessToken = jwtService.generateToken(user);
                var refreshToken = refreshTokenService.create(user);

                log.info("✅ OAuth2 login OK | user={} | provider={} | tenant={}",
                                user.getEmail(), provider, tenantId);

                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentType("application/json");
                response.getWriter().write("""
                                {
                                  "accessToken": "%s",
                                  "refreshToken": "%s"
                                }
                                """.formatted(accessToken, refreshToken.getToken()));
                response.getWriter().flush();
        }
}
