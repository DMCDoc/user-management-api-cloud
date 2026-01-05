package com.dmcdoc.sharedcommon.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class UserResponse {

    private UUID id;
    private String email;
    private String username;
    private String fullName;
    private boolean active;
}
