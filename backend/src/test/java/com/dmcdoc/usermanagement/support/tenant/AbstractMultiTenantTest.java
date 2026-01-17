package com.dmcdoc.usermanagement.support.tenant;   

import com.dmcdoc.usermanagement.core.model.User;
import com.dmcdoc.usermanagement.core.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractMultiTenantTest {

    @Autowired
    protected UserRepository userRepository;

    protected User loadUser(String username) {
        return userRepository.findByUsername(username).orElseThrow();
    }
}
