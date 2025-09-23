package com.example.usermanagement.config;

import com.example.usermanagement.core.model.Role;
import com.example.usermanagement.core.repository.RoleRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component @RequiredArgsConstructor
public class RoleInitializer {

    private final RoleRepository roleRepository;

    @PostConstruct
    public void initRoles() {
        List<String> defaultRoles = List.of("ROLE_USER", "ROLE_ADMIN");

        for (String roleName : defaultRoles) {
            roleRepository.findByName(roleName).orElseGet(() -> {
                Role role = Role.builder().name(roleName).build();
                System.out.println(">>> Initialisation des rôles en DB");
                return roleRepository.save(role);
            });
        }
    }
}
