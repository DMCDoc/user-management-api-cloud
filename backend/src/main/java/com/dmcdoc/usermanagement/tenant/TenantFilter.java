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

@Component
public class TenantFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public TenantFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        try {
            // 1) priorité au header X-Tenant-ID
            String tenant = request.getHeader("X-Tenant-ID");

            // 2) fallback sur JWT
            if (tenant == null || tenant.isBlank()) {
                String token = resolveToken(request);
                if (token != null) {
                    tenant = jwtService.extractClaim(token, claims -> (String) claims.get("tenant_id"));
                }
            }

            if (tenant != null && !tenant.isBlank()) {
                TenantContext.setCurrentTenant(tenant);
            }

            filterChain.doFilter(request, response);

        } finally {
            TenantContext.clear();
        }
    }

    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}
