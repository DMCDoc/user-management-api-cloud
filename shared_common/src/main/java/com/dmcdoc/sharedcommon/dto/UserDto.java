package com.dmcdoc.sharedcommon.dto;

import java.util.List;
import java.util.UUID;

public record UserDto(UUID id, String email, String username, List<String> roles) {
}
