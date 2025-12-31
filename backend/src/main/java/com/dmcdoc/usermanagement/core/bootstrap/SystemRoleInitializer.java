package com.dmcdoc.usermanagement.core.bootstrap;

import com.dmcdoc.usermanagement.core.model.Role;
import com.dmcdoc.usermanagement.core.repository.RoleRepository;
import com.dmcdoc.usermanagement.tenant.SystemTenant;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SystemRoleInitializer implements ApplicationRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(ApplicationArguments args) {

        try {
            if (!roleRepository.existsByNameAndTenantId(
                    "ROLE_SUPER_ADMIN",
                    SystemTenant.SYSTEM_TENANT)) {

                Role role = new Role();
                role.setName("ROLE_SUPER_ADMIN");
                role.setTenantId(SystemTenant.SYSTEM_TENANT);
                role.setActive(true);
                role.setDescription("System-wide super administrator");

                roleRepository.saveAndFlush(role);
            }
        } catch (DataIntegrityViolationException ignored) {
            // 🟢 Cas normal en tests : le rôle existe déjà
            // On ne fait RIEN
        }
    }
}
