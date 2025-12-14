package com.dmcdoc.usermanagement.core.service.tenant;

import com.dmcdoc.usermanagement.tenant.TenantContext;
import com.dmcdoc.usermanagement.core.model.Tenant;
import com.dmcdoc.usermanagement.core.repository.TenantRepository;
import com.dmcdoc.usermanagement.core.model.User;
import com.dmcdoc.usermanagement.core.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantProvisioningService {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;

    @Transactional
    public Tenant provisionIfNeeded(UUID tenantId, User user) {

        return tenantRepository.findById(tenantId)
                .orElseGet(() -> createTenant(tenantId, user));
    }

    private Tenant createTenant(UUID tenantId, User owner) {

        log.info("Provisioning new tenant {}", tenantId);

        Tenant tenant = Tenant.builder()
                .id(tenantId)
                .name("tenant-" + tenantId)
                .active(true)
                .build();

        tenantRepository.save(tenant);

        // attach tenant id to owner and persist
        owner.setTenantId(tenantId);
        userRepository.save(owner);

        TenantContext.setTenantId(tenantId);

        // 🔥 hook futur : quotas, settings, workspaces, etc.
        initializeTenantDefaults(tenant);

        return tenant;
    }

    private void initializeTenantDefaults(Tenant tenant) {
        log.info("Initializing defaults for tenant {}", tenant.getId());
        // placeholders : settings, permissions, plans, etc.
    }
}
