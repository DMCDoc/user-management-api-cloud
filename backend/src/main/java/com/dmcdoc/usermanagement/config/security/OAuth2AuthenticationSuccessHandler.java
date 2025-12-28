
/* 
package com.dmcdoc.usermanagement.config.security;

import com.dmcdoc.usermanagement.core.model.User;
import com.dmcdoc.usermanagement.core.repository.UserRepository;
import com.dmcdoc.usermanagement.core.service.auth.RefreshTokenService;
import com.dmcdoc.usermanagement.core.service.tenant.TenantAutoProvisioningService;
import com.dmcdoc.usermanagement.tenant.TenantContext;
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
        private final TenantAutoProvisioningService tenantProvisioningService;

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
                                        "Email manquant dans OAuth2 response");
                }

                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                                                "User not found"));

                boolean isSuperAdmin = user.getRoles()
                                .stream()
                                .map(r -> r.getName())
                                .anyMatch(r -> r.equals("ROLE_SUPER_ADMIN"));

                UUID tenantId;

                if (isSuperAdmin) {
                        // 🔥 SUPER ADMIN → BYPASS
                        TenantContext.enableBypass();
                        tenantId = null;
                } else {
                        tenantId = user.getTenantId();
                        if (tenantId == null) {
                                tenantId = UUID.randomUUID();
                                tenantProvisioningService.provisionIfNeeded(tenantId, user);
                                user.setTenantId(tenantId);
                                userRepository.save(user);
                        }
                        TenantContext.setTenantId(tenantId);
                }

                String accessToken = jwtService.generateToken(user);
                var refreshToken = refreshTokenService.create(user);

                log.info("OAuth2 success | user={} | superAdmin={} | tenant={}",
                                email, isSuperAdmin, tenantId);

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
*/