package com.example.usermanagement;

import com.example.usermanagement.config.RoleInitializer;
import com.example.usermanagement.model.Role;
import com.example.usermanagement.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest @ContextConfiguration(classes = { RoleInitializer.class, RoleRepository.class })
class RoleInitializerTest {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RoleInitializer roleInitializer;

    @Test
    void shouldCreateDefaultRolesIfNotExist() {
        // Run initializer
        roleInitializer.initRoles();

        Optional<Role> userRole = roleRepository.findByName("ROLE_USER");
        Optional<Role> adminRole = roleRepository.findByName("ROLE_ADMIN");

        assertThat(userRole).isPresent();
        assertThat(adminRole).isPresent();
    }

    @Test
    void shouldNotDuplicateRolesIfAlreadyExist() {
        roleInitializer.initRoles();
        long countAfterFirstRun = roleRepository.count();

        roleInitializer.initRoles();
        long countAfterSecondRun = roleRepository.count();

        assertThat(countAfterSecondRun).isEqualTo(countAfterFirstRun);
    }
}
