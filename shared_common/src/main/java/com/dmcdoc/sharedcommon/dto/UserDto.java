package com.dmcdoc.sharedcommon.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private UUID id;
    private String username;
    private String email;
    private String fullName;
    private boolean active;
}
