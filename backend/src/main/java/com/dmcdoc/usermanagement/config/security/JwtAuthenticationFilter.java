package com.dmcdoc.usermanagement.config.security;

import com.dmcdoc.usermanagement.tenant.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/auth")
                || path.startsWith("/health")
                || path.startsWith("/actuator");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        try {
            // 1️⃣ Authorization obligatoire
            String auth = request.getHeader("Authorization");
            if (auth == null || !auth.startsWith("Bearer ")) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Missing authentication");
                return;
            }

            String token = auth.substring(7);

            // 2️⃣ JWT valide
            if (!jwtService.isValid(token)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid token");
                return;
            }

            // 3️⃣ Auth Spring Security
            Authentication authentication = jwtService.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 4️⃣ Super-admin → bypass total
            if (jwtService.isSuperAdmin(token)) {
                TenantContext.enableBypass();
            } else {
                UUID tenantId = jwtService.extractTenantId(token);
                if (tenantId == null) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Tenant missing");
                    return;
                }
                TenantContext.setTenantId(tenantId);
            }

            // 5️⃣ Validation du header X-Tenant-Id (s'il est présent)
            String headerTenant = request.getHeader("X-Tenant-Id");
            if (headerTenant != null) {
                if (headerTenant.isBlank()) {
                    response.sendError(
                            HttpServletResponse.SC_FORBIDDEN,
                            "Invalid tenant header");
                    return;
                }

                try {
                    UUID headerTenantId = UUID.fromString(headerTenant);

                    if (!TenantContext.isBypassEnabled()
                            && !headerTenantId.equals(TenantContext.getTenantId())) {
                        response.sendError(
                                HttpServletResponse.SC_FORBIDDEN,
                                "Tenant mismatch");
                        return;
                    }

                } catch (IllegalArgumentException e) {
                    response.sendError(
                            HttpServletResponse.SC_FORBIDDEN,
                            "Invalid tenant header");
                    return;
                }
            }

            // 6️⃣ Garde-fou final
            if (!TenantContext.isBypassEnabled() && TenantContext.getTenantId() == null) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Tenant context missing");
                return;
            }

            filterChain.doFilter(request, response);

        } finally {
            TenantContext.clear();
            SecurityContextHolder.clearContext();
        }
    }
}
