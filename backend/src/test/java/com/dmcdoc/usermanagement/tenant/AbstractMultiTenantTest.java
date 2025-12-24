package com.dmcdoc.usermanagement.tenant;

import com.dmcdoc.usermanagement.config.security.JwtService;
import com.dmcdoc.usermanagement.core.model.Role;
import com.dmcdoc.usermanagement.core.model.User;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;
import java.util.UUID;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc // Permet l'injection de MockMvc dans les classes filles
public abstract class AbstractMultiTenantTest {

    @Autowired
    protected JwtService jwtService;

    // UUID fixes ou aléatoires pour les tests
    protected final UUID tenantA = UUID.randomUUID();
    protected final UUID tenantB = UUID.randomUUID();

    protected String tokenForTenant(UUID tenantId, String roleName) {
        User user = new User();
        user.setUsername("user-" + tenantId);
        user.setTenantId(tenantId);

        Role role = new Role();
        role.setName(roleName);
        role.setActive(true); // Cohérence avec votre schéma SQL
        user.setRoles(Set.of(role));

        return jwtService.generateToken(user);
    }

    protected String superAdminToken() {
        User user = new User();
        user.setUsername("super-admin");
        // Souvent, le super admin n'a pas de tenantId ou un tenantId spécifique
        // "system"
        user.setTenantId(UUID.fromString("00000000-0000-0000-0000-000000000000"));

        Role role = new Role();
        role.setName("ROLE_SUPER_ADMIN");
        role.setActive(true);
        user.setRoles(Set.of(role));

        return jwtService.generateToken(user);
    }

    @AfterEach
    void cleanup() {
        // Nettoyage impératif pour ne pas influencer le test suivant
        TenantContext.clear();
    }
}