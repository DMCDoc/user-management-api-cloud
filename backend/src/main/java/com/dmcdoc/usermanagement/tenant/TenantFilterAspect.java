package com.dmcdoc.usermanagement.tenant;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

/**
 * Active automatiquement le filtre Hibernate
 * avant toute méthode @Transactional
 */
@Aspect
@Component
@RequiredArgsConstructor
public class TenantFilterAspect {

    private final HibernateTenantFilterEnabler enabler;

    @Before("@annotation(org.springframework.transaction.annotation.Transactional)")
    public void beforeTransactionalMethod() {
        enabler.enableTenantFilter();
    }
}
/*
 * Option la plus propre : AOP
 */