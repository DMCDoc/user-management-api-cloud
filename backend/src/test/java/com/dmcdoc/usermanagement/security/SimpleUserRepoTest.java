package com.dmcdoc.usermanagement.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.dmcdoc.usermanagement.core.model.User;
import com.dmcdoc.usermanagement.core.repository.UserRepository;

/*
 * @SpringBootTest
 * class SimpleUserRepoTest {
 * 
 * @Autowired
 * private UserRepository userRepository;
 * 
 * @AfterEach
 * void cleanup() {
 * userRepository.deleteAll(); // ⚡ nettoie après chaque test
 * }
 * 
 * @Test
 * void insertUserDirect() {
 * User u = new User();
 * u.setUsername("test_db");
 * u.setEmail("test@example.com");
 * u.setPassword("pwd");
 * u.setEnabled(true);
 * 
 * 
 * userRepository.save(u);
 * }
 * }
 */
