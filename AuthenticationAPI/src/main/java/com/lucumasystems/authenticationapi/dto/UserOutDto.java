package com.lucumasystems.authenticationapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class UserOutDto {
    private long id;
    private String username;
    private boolean enabled;
}

