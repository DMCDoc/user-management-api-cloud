package com.dmcdoc.usermanagement.tenant;

import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class TenantSpecifications {

    public static <T> Specification<T> tenantEquals(UUID tenantId) {
        return (root, query, cb) -> cb.equal(root.get("tenantId"), tenantId);
    }
}
