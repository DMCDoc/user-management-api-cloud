package com.dmcdoc.usermanagement.tenant;

import jakarta.persistence.EntityManager;
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

@Component
@RequiredArgsConstructor
public class TenantFilter extends OncePerRequestFilter {

    private final EntityManager entityManager;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        try {
            UUID tenantId = TenantContext.getTenantId();
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            // Vérifier si l'utilisateur est Super Admin
            boolean isSuperAdmin = auth != null && auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));

            Session session = entityManager.unwrap(Session.class);

            if (isSuperAdmin) {
                // Si Super Admin : on désactive explicitement le filtre pour voir toutes les
                // données
                session.disableFilter("tenantFilter");
            } else if (tenantId != null) {
                // Sinon, si on a un tenantId : on applique l'isolation
                session.enableFilter("tenantFilter")
                        .setParameter("tenantId", tenantId);
            }

            filterChain.doFilter(request, response);

        } finally {
            // IMPORTANT : Nettoyage du ThreadLocal pour éviter les fuites de contexte
            TenantContext.clear();
        }
    }
}