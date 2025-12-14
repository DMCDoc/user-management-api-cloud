/*
✔️ Stateless
✔️ Aucune dépendance web
✔️ Réutilisable partout (@PreAuthorize)
*/

package com.dmcdoc.usermanagement.config.security;

import com.dmcdoc.usermanagement.tenant.TenantCurrentProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("tenantAuth")
@RequiredArgsConstructor
public class TenantAuthorizationService {

    private final TenantCurrentProvider tenantProvider;

    /**
     * Vérifie que la ressource appartient au tenant courant
     */
    public boolean isSameTenant(UUID resourceTenantId) {
        return resourceTenantId != null
                && resourceTenantId.equals(tenantProvider.getTenantId());
    }

    /**
     * Vérifie que l'appel concerne le tenant courant
     * (utile si le tenantId est déjà résolu côté service)
     */
    public boolean isCurrentTenant(UUID tenantId) {
        return tenantId != null
                && tenantId.equals(tenantProvider.getTenantId());
    }
}
