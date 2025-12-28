package com.dmcdoc.usermanagement.config.security;

import com.dmcdoc.usermanagement.tenant.TenantCurrentProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;


import java.util.UUID;

@Component("tenantSecurity") // Identifiant utilisé dans le SpEL
@RequiredArgsConstructor
public class TenantSecurity {

    private final TenantCurrentProvider tenantProvider;

    /**
     * Logique de validation transverse
     */
    public boolean check(Authentication authentication, Object resourceId) {
        // 1. Bypass si Super Admin
        if (tenantProvider.isSuperAdmin()) {
            return true;
        }

        // 2. Vérification du contexte
        UUID currentTenant = tenantProvider.getTenantId();
        if (currentTenant == null || authentication == null || resourceId == null) {
            return false;
        }

        // 3. Conversion sécurisée du resourceId (supporte String ou UUID)
        UUID targetId;
        try {
            targetId = (resourceId instanceof UUID uuid) ? uuid : UUID.fromString(resourceId.toString());
        } catch (IllegalArgumentException e) {
            return false;
        }

        return belongsToTenant(targetId, currentTenant);
    }

    private boolean belongsToTenant(UUID resourceId, UUID tenantId) {
        // TODO: Appel à votre Repository (ex: documentRepository.existsByIdAndTenantId)
        return true;
    }
}