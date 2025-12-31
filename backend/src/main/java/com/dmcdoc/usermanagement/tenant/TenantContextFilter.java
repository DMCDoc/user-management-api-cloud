package com.dmcdoc.usermanagement.tenant;

import com.dmcdoc.usermanagement.config.security.JwtService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * 🏢 Filtre responsable du contexte tenant
 * → Sécurité
 * → Activation du filtre Hibernate
 */
@Component
@RequiredArgsConstructor
public class TenantContextFilter extends OncePerRequestFilter {

    public static final String TENANT_HEADER = "X-Tenant-ID";

    private final JwtService jwtService;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * ❌ On ignore les endpoints publics
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/auth")
                || path.startsWith("/swagger")
                || path.startsWith("/v3/api-docs")
                || path.equals("/ping")
                || path.startsWith("/error");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 🔓 Pas authentifié → pas de tenant
        if (authentication == null || !authentication.isAuthenticated()) {
            filterChain.doFilter(request, response);
            return;
        }

        // 👑 SUPER ADMIN → bypass TOTAL
        if (isSuperAdmin(authentication)) {
            TenantContext.clear();
            filterChain.doFilter(request, response);
            return;
        }

        // 🏢 Tenant obligatoire
        String tenantHeader = request.getHeader(TENANT_HEADER);
        if (tenantHeader == null || tenantHeader.isBlank()) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Missing tenant header");
            return;
        }

        try {
            UUID tenantId = UUID.fromString(tenantHeader);

            // 1️⃣ Contexte applicatif
            TenantContext.setTenantId(tenantId);

            // 2️⃣ Activation du filtre Hibernate
            Session session = entityManager.unwrap(Session.class);
            session.enableFilter("tenantFilter")
                    .setParameter("tenantId", tenantId);

            filterChain.doFilter(request, response);

        } catch (IllegalArgumentException ex) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Invalid tenant id");
        } finally {
            TenantContext.clear();
        }
    }

    /**
     * 👑 Détection du rôle SUPER_ADMIN
     */
    private boolean isSuperAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));
    }
}
