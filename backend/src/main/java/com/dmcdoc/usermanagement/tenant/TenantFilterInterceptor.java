package com.dmcdoc.usermanagement.tenant;

/*
Intercepteur Spring (clé du système) */

import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

import org.hibernate.Session;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class TenantFilterInterceptor implements HandlerInterceptor {

    private final EntityManager entityManager;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // 🚀 BYPASS TOTAL SUPER_ADMIN
        if (auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"))) {
            return true;
        }

        String tenantHeader = request.getHeader("X-Tenant-ID");
        if (tenantHeader != null && !tenantHeader.trim().isEmpty()) {
            try {
                UUID tenantId = UUID.fromString(tenantHeader);
                // On stocke dans le contexte pour le Controller
                TenantContext.setTenantId(tenantId);

                // On active le filtre Hibernate pour l'isolation (TEST 9)
                Session session = entityManager.unwrap(Session.class);
                session.enableFilter("tenantFilter").setParameter("tenantId", tenantId);
            } catch (IllegalArgumentException e) {
                // UUID mal formé : on ne fait rien, le Controller renverra 403 (TEST 3)
            }
        }
        return true;
    }
}