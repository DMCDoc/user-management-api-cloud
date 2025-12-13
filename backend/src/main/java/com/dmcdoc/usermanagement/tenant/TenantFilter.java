package com.dmcdoc.usermanagement.tenant;

import com.dmcdoc.usermanagement.config.security.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class TenantFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public TenantFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        try {
            UUID tenantId = resolveTenant(request);

            if (tenantId == null) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Tenant missing");
                return;
            }

            TenantContext.setTenantId(tenantId);
            filterChain.doFilter(request, response);

        } finally {
            TenantContext.clear();
        }
    }

    private UUID resolveTenant(HttpServletRequest request) {
        // 1) Header
        String header = request.getHeader("X-Tenant-ID");
        if (header != null && !header.isBlank()) {
            try {
                return UUID.fromString(header);
            } catch (IllegalArgumentException ignored) {
            }
        }

        // 2) JWT
        String token = resolveToken(request);
        if (token != null) {
            String tenant = jwtService.extractTenant(token);
            if (tenant != null) {
                try {
                    return UUID.fromString(tenant);
                } catch (IllegalArgumentException ignored) {
                }
            }
        }

        return null;
    }

    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        return (bearer != null && bearer.startsWith("Bearer "))
                ? bearer.substring(7)
                : null;
    }
}
