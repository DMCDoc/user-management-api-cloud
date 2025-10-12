package com.dmcdoc.sharedcommon.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter @Setter
public class RegisterRequest {
    private String fullName;
    private String username;
    private String password;
    private String email;
    private Set<String> roles; // exemple : ["ROLE_USER", "ROLE_ADMIN"]

    
}