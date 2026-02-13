// Provisioning d’un tenant (AUTOMATIQUE)

package com.dmcdoc.usermanagement.tenant.provisioning;

import com.dmcdoc.sharedcommon.dto.RegisterTenantRequest;
import com.dmcdoc.usermanagement.Kds.service.RestaurantService;
import com.dmcdoc.usermanagement.core.model.*;
import com.dmcdoc.usermanagement.core.service.UserServiceImpl;
import com.dmcdoc.usermanagement.core.service.tenant.TenantService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TenantProvisioningService {

    private final TenantService tenantService;
    private final RestaurantService restaurantService;
    private final UserServiceImpl userService;

    public User provisionTenant(RegisterTenantRequest request, String encodedPassword) {

        UUID tenantId = UUID.randomUUID();

        // 1️⃣ Tenant
        tenantService.createTenant(
                tenantId,
                request.getTenantName(),
                request.getTenantKey(),
                request.getMetadata());

        // 2️⃣ Admin
        User admin = userService.createAdminForTenant(
                tenantId,
                request.getAdminEmail(),
                encodedPassword,
                request.getFirstName(),
                request.getLastName());

        // 3️⃣ Defaults (restaurant)
        restaurantService.create(
                request.getRestaurantName(),
                request.getRestaurantAddress(),
                request.getMetadata());

        return admin;
    }
}
