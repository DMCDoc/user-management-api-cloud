package com.example.usermanagement.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.usermanagement.core.model.Role;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name)
    ;
}