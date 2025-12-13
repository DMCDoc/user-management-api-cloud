package com.dmcdoc.usermanagement.tenant;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class TenantFilterAspect {

    private final EntityManager entityManager;

    @Before("execution(* com.dmcdoc..repository..*(..))")
    public void enableTenantFilter() {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return; // SUPER_ADMIN ou bootstrap
        }

        Session session = entityManager.unwrap(Session.class);
        session.enableFilter("tenantFilter")
                .setParameter("tenantId", tenantId);
    }
}
