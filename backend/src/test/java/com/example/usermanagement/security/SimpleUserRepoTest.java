package com.example.usermanagement.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.usermanagement.model.User;
import com.example.usermanagement.repository.UserRepository;

@SpringBootTest
class SimpleUserRepoTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void insertUserDirect() {
        User u = new User();
        u.setUsername("test_db");
        u.setEmail("test@example.com");
        u.setPassword("pwd");
        u.setEnabled(true);

        userRepository.save(u);
    }
}
