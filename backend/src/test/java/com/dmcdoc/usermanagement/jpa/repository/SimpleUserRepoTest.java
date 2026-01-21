package com.dmcdoc.usermanagement.jpa.repository;

import com.dmcdoc.usermanagement.core.model.User;
import com.dmcdoc.usermanagement.core.repository.UserRepository;
import com.dmcdoc.usermanagement.support.BaseJpaTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class SimpleUserRepoTest extends BaseJpaTest {

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
        entityManager.flush();
        entityManager.clear();
    }
}
