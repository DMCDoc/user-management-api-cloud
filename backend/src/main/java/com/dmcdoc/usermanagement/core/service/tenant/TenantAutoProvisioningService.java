package com.dmcdoc.usermanagement.core.service.tenant;

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
public class TenantAutoProvisioningService {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;

    @Transactional
    public Tenant provisionIfNeeded(UUID tenantId, User owner) {

        return tenantRepository.findById(tenantId)
                .orElseGet(() -> createTenant(tenantId, owner));
    }

    private Tenant createTenant(UUID tenantId, User owner) {

        log.info("Provisioning new tenant {}", tenantId);

        Tenant tenant = Tenant.builder()
                .id(tenantId)
                .name("tenant-" + tenantId)
                .active(true)
                .build();

        tenantRepository.save(tenant);

        owner.setTenantId(tenantId);
        userRepository.save(owner);

        initializeTenantDefaults(tenant);

        return tenant;
    }

    private void initializeTenantDefaults(Tenant tenant) {
        log.info("Initializing defaults for tenant {}", tenant.getId());
        // quotas, settings, plans, workspaces…
    }
}

/*
 * Son rôle est clair :
 * 
 * création implicite
 * 
 * auto-provisioning
 * 
 * hook futur
 */

/*
 * ✔ Plus aucun effet de bord global
 * ✔ Service purement métier
 * ✔ Utilisable partout
 * ✔ Compatible avec ton multi-tenant Hibernate
 */