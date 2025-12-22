package com.dmcdoc.usermanagement.tenant;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.hibernate.Session;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class TenantHibernateFilter extends OncePerRequestFilter {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        try {
            if (!TenantContext.isBypass()) {
                UUID tenantId = TenantContext.getTenantId();
                if (tenantId != null) {
                    Session session = entityManager.unwrap(Session.class);
                    session.enableFilter("tenantFilter")
                            .setParameter("tenantId", tenantId);
                }
            }

            filterChain.doFilter(request, response);

        } finally {
            Session session = entityManager.unwrap(Session.class);
            if (session.getEnabledFilter("tenantFilter") != null) {
                session.disableFilter("tenantFilter");
            }
        }
    }
}
