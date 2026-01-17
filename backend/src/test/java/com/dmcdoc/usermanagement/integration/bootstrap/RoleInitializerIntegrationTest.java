package com.dmcdoc.usermanagement.integration.bootstrap;

import com.dmcdoc.usermanagement.core.model.Role;
import com.dmcdoc.usermanagement.core.repository.RoleRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;




@ActiveProfiles("test")
@DataJpaTest
class RoleInitializerIntegrationTest {

    @Autowired
    private RoleRepository roleRepository;

    @Test
    void rolesShouldBeInitializedAtStartup() {
        assertTrue(roleRepository.findByName("ROLE_USER").isPresent(), "ROLE_USER should be initialized");
        assertTrue(roleRepository.findByName("ROLE_ADMIN").isPresent(), "ROLE_ADMIN should be initialized");
    }

    @Test
    void initializedRolesShouldHaveCorrectNames() {
        Optional<Role> userRole = roleRepository.findByName("ROLE_USER");
        Optional<Role> adminRole = roleRepository.findByName("ROLE_ADMIN");
        assertTrue(userRole.isPresent());
        assertEquals("ROLE_USER", userRole.get().getName());
        assertTrue(adminRole.isPresent());
        assertEquals("ROLE_ADMIN", adminRole.get().getName());
    }


}
