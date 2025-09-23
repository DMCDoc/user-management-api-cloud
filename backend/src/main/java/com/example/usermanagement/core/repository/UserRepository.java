package com.example.usermanagement.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.usermanagement.core.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    // 🔹 Pour tes tests : supprimer par username
    void deleteByUsername(String username);
}
