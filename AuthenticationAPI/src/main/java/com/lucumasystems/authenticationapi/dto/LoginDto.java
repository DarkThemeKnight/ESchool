package com.lucumasystems.authenticationapi.dto;

import lombok.Data;

@Data
public class LoginDto {
    private String username;
    private String password;
}
