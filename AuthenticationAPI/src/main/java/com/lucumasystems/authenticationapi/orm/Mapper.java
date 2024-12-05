package com.lucumasystems.authenticationapi.orm;

import com.lucumasystems.authenticationapi.dto.UserDetailsDto;
import com.lucumasystems.authenticationapi.dto.UserOutDto;
import com.lucumasystems.authenticationapi.entity.Role;
import com.lucumasystems.authenticationapi.entity.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.stream.Collectors;

public class Mapper {
    public static UserDetailsDto toUserOutDto(User user) {
        return UserDetailsDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                .createdBy(user.getCreatedBy() != null ? user.getCreatedBy().getUsername() : null)
                .updatedBy(user.getUpdatedBy() != null ? user.getUpdatedBy().getUsername() : null)
                .accountNonExpired(user.isAccountNonExpired())
                .accountNonLocked(user.isAccountNonLocked())
                .credentialsNonExpired(user.isCredentialsNonExpired())
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }


}
