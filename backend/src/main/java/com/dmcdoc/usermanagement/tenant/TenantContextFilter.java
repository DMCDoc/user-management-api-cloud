package com.dmcdoc.usermanagement.tenant;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class TenantContextFilter extends OncePerRequestFilter {

    private static final String TENANT_HEADER = "X-Tenant-Id";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        try {
            // 🔹 BYPASS actif → on laisse passer
            if (TenantContext.isBypassEnabled()) {
                filterChain.doFilter(request, response);
                return;
            }

            // 🔹 Déjà résolu (ex: JWT)
            if (TenantContext.isResolved()) {
                filterChain.doFilter(request, response);
                return;
            }

            // 🔹 Résolution via header
            String header = request.getHeader(TENANT_HEADER);
            if (header == null || header.isBlank()) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Tenant missing");
                return;
            }

            UUID tenantId;
            try {
                tenantId = UUID.fromString(header);
            } catch (IllegalArgumentException ex) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid tenant id");
                return;
            }

            TenantContext.setTenantId(tenantId);
            filterChain.doFilter(request, response);

        } finally {
            TenantContext.clear();
        }
    }
}
