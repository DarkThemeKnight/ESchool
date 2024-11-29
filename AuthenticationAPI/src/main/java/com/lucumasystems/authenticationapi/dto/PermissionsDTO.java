package com.lucumasystems.authenticationapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PermissionsDTO {
    private String permission;
    private String description;
}
