package com.example.shared.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {

    private String username;
    private String email;
    private String fullName;
}
