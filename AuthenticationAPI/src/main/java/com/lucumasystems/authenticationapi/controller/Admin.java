package com.lucumasystems.authenticationapi.controller;

import com.lucumasystems.authenticationapi.ResponseHolder;
import com.lucumasystems.authenticationapi.entity.User;
import com.lucumasystems.authenticationapi.orm.Mapper;
import com.lucumasystems.authenticationapi.service.AuditLogService;
import com.lucumasystems.authenticationapi.service.JwtService;
import com.lucumasystems.authenticationapi.service.UserService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/admin")
@RequiredArgsConstructor
public class Admin {
    private final UserService userService;
    private final AuditLogService auditLogService;
    private final JwtService jwtService;

    @Data
    public static class AssignRoles{
        private List<String> data;
    }

    @PostMapping("/assign-roles/{userId}")
    public ResponseEntity<ResponseHolder> assignRolesToUser(@PathVariable Long userId, @RequestBody AssignRoles roleNames, @RequestHeader("Authorization") String token) {
        token = jwtService.extractTokenFromHeader(token);
        long updatedBy = jwtService.getUserId(token);
        User user = userService.assignRolesToUser(userId, roleNames.getData(), updatedBy);
        return ResponseEntity.ok(ResponseHolder.builder().message("Roles assigned successfully").response(Mapper.toUserOutDto(user)).build());
    }

    @PostMapping("/remove-roles/{userId}")
    public ResponseEntity<ResponseHolder> removeRolesFromUser(@PathVariable Long userId, @RequestBody AssignRoles roleNames, @RequestHeader("Authorization") String token) {
        token = jwtService.extractTokenFromHeader(token);
        long updatedBy = jwtService.getUserId(token);
        User user = userService.removeRolesFromUser(userId, roleNames.getData(), updatedBy);
        return ResponseEntity.ok(ResponseHolder.builder().message("Roles removed successfully").response(Mapper.toUserOutDto(user)).build());
    }

    @PutMapping("/deactivate/{userId}")
    public ResponseEntity<ResponseHolder> deactivateUser(@PathVariable Long userId, @RequestHeader("Authorization") String token) {
        token = jwtService.extractTokenFromHeader(token);
        long updatedBy = jwtService.getUserId(token);
        userService.deactivateUser(userId, updatedBy);

        // Log the deactivation action
        auditLogService.logAction("Admin", "DEACTIVATE_USER");

        return ResponseEntity.ok(ResponseHolder.builder().message("User deactivated successfully").build());
    }

    @PutMapping("/activate/{userId}")
    public ResponseEntity<ResponseHolder> activateUser(@PathVariable Long userId, @RequestHeader("Authorization") String token) {
        token = jwtService.extractTokenFromHeader(token);
        long updatedBy = jwtService.getUserId(token);
        userService.activateUser(userId, updatedBy);

        // Log the activation action
        auditLogService.logAction("Admin", "ACTIVATE_USER");

        return ResponseEntity.ok(ResponseHolder.builder().message("User activated successfully").build());
    }


}
