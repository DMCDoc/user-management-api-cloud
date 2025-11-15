package com.dmcdoc.sharedcommon.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import java.util.Set;

public class AdminUserUpdateRequest {
    @Email
    private String email;
    @Size(min = 3, max = 100)
    private String username;
    private Boolean enabled;
    private Set<String> roles;
    private String password;

    public AdminUserUpdateRequest() {}

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }

    public Set<String> getRoles() { return roles; }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }
    
    public String getPassword() {
        return password;
    }
}
