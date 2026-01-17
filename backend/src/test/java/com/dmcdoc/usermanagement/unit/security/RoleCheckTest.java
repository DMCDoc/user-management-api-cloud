package com.dmcdoc.usermanagement.unit.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.dmcdoc.usermanagement.UserManagementApplication;
import com.dmcdoc.usermanagement.core.repository.RoleRepository;

@SpringBootTest(classes = UserManagementApplication.class)
@ActiveProfiles("test")
class RoleCheckTest {

    @Autowired
    RoleRepository roleRepository;

    @Test
    void printRoles() {
        System.out.println("=== ROLES EN DB ===");
        roleRepository.findAll().forEach(r -> System.out.println(r.getName()));
    }
}
