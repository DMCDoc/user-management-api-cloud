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

/**
 * Active automatiquement le filtre Hibernate "tenantFilter"
 * pour chaque requête HTTP.
 */
@Component
@RequiredArgsConstructor
public class HibernateTenantFilter extends OncePerRequestFilter {

    private final EntityManager entityManager;
    private final CurrentTenantProvider tenantProvider;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        UUID tenantId = tenantProvider.getCurrentTenant();

        if (tenantId != null) {
            entityManager.unwrap(Session.class)
                    .enableFilter("tenantFilter")
                    .setParameter("tenantId", tenantId);
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            try {
                entityManager.unwrap(Session.class)
                        .disableFilter("tenantFilter");
            } catch (Exception ignored) {
            }
        }
    }
}
