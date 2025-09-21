package com.example.usermanagement;

import com.example.usermanagement.config.RoleInitializer;
import com.example.usermanagement.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RoleInitializerTest {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RoleInitializer roleInitializer;

    @Test
    void shouldCreateDefaultRolesIfNotExist() {
        roleInitializer.initRoles();

        assertThat(roleRepository.findByName("ROLE_USER")).isPresent();
        assertThat(roleRepository.findByName("ROLE_ADMIN")).isPresent();
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
