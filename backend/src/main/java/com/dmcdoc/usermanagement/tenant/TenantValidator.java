package com.dmcdoc.usermanagement.tenant;

import com.dmcdoc.usermanagement.core.model.Tenant;
import com.dmcdoc.usermanagement.core.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TenantValidator {

    private final TenantRepository tenantRepository;

    public void validate(UUID tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new AccessDeniedException("Tenant not found"));

        if (!tenant.isActive()) {
            throw new AccessDeniedException("Tenant disabled");
        }
    }
}
