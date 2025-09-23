package com.example.usermanagement.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.example.usermanagement.core.repository.RoleRepository;

@SpringBootTest @ActiveProfiles("test")
class RoleCheckTest {

    @Autowired
    RoleRepository roleRepository;

    @Test
    void printRoles() {
        System.out.println("=== ROLES EN DB ===");
        roleRepository.findAll().forEach(r -> System.out.println(r.getName()));
    }
}
