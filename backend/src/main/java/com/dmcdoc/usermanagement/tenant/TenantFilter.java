/*Activer le filter au niveau Hibernate /Ce filtre ne détermine PAS le tenant, il l’applique seulement. */

package com.dmcdoc.usermanagement.tenant;

import jakarta.persistence.EntityManager;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
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

            if (tenantId != null) {
                Session session = entityManager.unwrap(Session.class);
                session.enableFilter("tenantFilter")
                        .setParameter("tenantId", tenantId);
            }

            filterChain.doFilter(request, response);

        } finally {
            // IMPORTANT
            TenantContext.clear();
        }
    }
}
