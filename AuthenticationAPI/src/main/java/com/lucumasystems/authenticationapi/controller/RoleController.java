package com.lucumasystems.authenticationapi.controller;

import com.lucumasystems.authenticationapi.ResponseHolder;
import com.lucumasystems.authenticationapi.dto.RoleDto;
import com.lucumasystems.authenticationapi.dto.UserOutDto;
import com.lucumasystems.authenticationapi.entity.Role;
import com.lucumasystems.authenticationapi.service.AuditLogService;
import com.lucumasystems.authenticationapi.service.JwtService;
import com.lucumasystems.authenticationapi.service.RoleService;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {
    private final RoleService roleService;
    private final JwtService jwtService;
    private final AuditLogService auditLogService;
    /**
     * Add a new role.
     *
     * @param roleDto  The role data to be added.
     * @param jwtToken The JWT token of the user making the request.
     * @return A standardized response with the added role.
     */
    @PostMapping
    public ResponseEntity<ResponseHolder> addRole(@RequestBody @Valid RoleDto roleDto, @RequestHeader("Authorization") String jwtToken) {
        jwtToken = jwtService.extractTokenFromHeader(jwtToken);
        try {
            int createdBy = jwtService.getUserId(jwtToken);
            roleService.addRole(roleDto, createdBy);
            auditLogService.logAction(jwtService.getUsername(jwtToken), "ADD_ROLE");
            return ResponseEntity.ok(
                    ResponseHolder.builder()
                            .message("Role added successfully.")
                            .build()
            );
        } catch (EntityExistsException e) {
            return ResponseEntity.badRequest().body(
                    ResponseHolder.builder()
                            .message(e.getMessage())
                            .build()
            );
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().body(
                    ResponseHolder.builder()
                            .message(e.getMessage())
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    ResponseHolder.builder()
                            .message("An unexpected error occurred.")
                            .build()
            );
        }
    }

    /**
     * Add permissions to a role.
     *
     * @param roleName      The role to which permissions will be added.
     * @param permissionNames List of permission names to add.
     * @param jwtToken      The JWT token of the user making the request.
     * @return A standardized response with the updated role.
     */
    @PutMapping("/{roleName}/permissions")
    public ResponseEntity<ResponseHolder> addPermissionsToRole(
            @PathVariable String roleName,
            @RequestBody List<String> permissionNames,
            @RequestHeader("Authorization") String jwtToken) {
        jwtToken = jwtService.extractTokenFromHeader(jwtToken);
        try {
            int updatedBy = jwtService.getUserId(jwtToken);
            Role role = roleService.addPermissionToRole(roleName, updatedBy, permissionNames);
            auditLogService.logAction(jwtService.getUsername(jwtToken), "ADD_PERMISSIONS_TO_ROLE");
            return ResponseEntity.ok(
                    ResponseHolder.builder()
                            .message("Permissions added to role successfully.")
                            .build()
            );
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().body(
                    ResponseHolder.builder()
                            .message(e.getMessage())
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    ResponseHolder.builder()
                            .message("An unexpected error occurred.")
                            .build()
            );
        }
    }

    /**
     * Change the status of a role.
     *
     * @param roleName The name of the role whose status will be changed.
     * @param jwtToken The JWT token of the user making the request.
     * @return A standardized response with the updated role.
     */
    @PutMapping("/{roleName}/status")
    public ResponseEntity<ResponseHolder> changeRoleStatus(@PathVariable String roleName, @RequestHeader("Authorization") String jwtToken) {
        jwtToken = jwtService.extractTokenFromHeader(jwtToken);
        try {
            int updatedBy = jwtService.getUserId(jwtToken);
            Role role = roleService.changeRoleStatus(roleName, updatedBy);
            auditLogService.logAction(jwtService.getUsername(jwtToken), "CHANGE_ROLE_STATUS");
            return ResponseEntity.ok(
                    ResponseHolder.builder()
                            .message("Role status updated successfully.")
                            .build()
            );
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().body(
                    ResponseHolder.builder()
                            .message(e.getMessage())
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    ResponseHolder.builder()
                            .message("An unexpected error occurred.")
                            .build()
            );
        }
    }
    /**
     * Retrieve users with specific roles.
     *
     * @param roleNames List of role names to filter users by.
     * @param offset    The page offset.
     * @param limit     The page limit.
     * @return A standardized response with the paginated list of users.
     */
    @GetMapping("/users")
    public ResponseEntity<ResponseHolder> findUsersWithRoles(
            @RequestParam List<String> roleNames,
            @RequestParam int offset,
            @RequestParam int limit) {
        try {
            Page<UserOutDto> users = roleService.findUsersWithRoles(roleNames, offset, limit);
            return ResponseEntity.ok(
                    ResponseHolder.builder()
                            .response(users)
                            .message("Users retrieved successfully.")
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    ResponseHolder.builder()
                            .message("An unexpected error occurred.")
                            .build()
            );
        }
    }

    /**
     * Retrieve all roles with optional search criteria.
     *
     * @param offset The page offset.
     * @param limit  The page limit.
     * @param search The search keyword (optional).
     * @return A standardized response with the paginated list of roles.
     */
    @GetMapping
    public ResponseEntity<ResponseHolder> getAllRoles(
            @RequestParam int offset,
            @RequestParam int limit,
            @RequestParam(required = false) String search) {
        try {
            Page<Role> roles = roleService.getAllRoles(offset, limit, search);
            return ResponseEntity.ok(
                    ResponseHolder.builder()
                            .response(roles)
                            .message("Roles retrieved successfully.")
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    ResponseHolder.builder()
                            .message("An unexpected error occurred.")
                            .build()
            );
        }
    }

    /**
     * Retrieve roles by ID or name with pagination.
     *
     * @param offset   The page offset.
     * @param limit    The page limit.
     * @param roleIds  List of role IDs to filter by (optional).
     * @param names    List of role names to filter by (optional).
     * @return A standardized response with the paginated list of roles.
     */
    @GetMapping("/filter")
    public ResponseEntity<ResponseHolder> listRoles(
            @RequestParam int offset,
            @RequestParam int limit,
            @RequestParam(required = false) List<Integer> roleIds,
            @RequestParam(required = false) List<String> names) {
        try {
            Page<Role> roles = roleService.listRoles(offset, limit, roleIds, names);
            return ResponseEntity.ok(
                    ResponseHolder.builder()
                            .response(roles)
                            .message("Roles retrieved successfully.")
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    ResponseHolder.builder()
                            .message("An unexpected error occurred.")
                            .build()
            );
        }
    }

    /**
     * Retrieve roles by permissions with pagination.
     *
     * @param offset         The page offset.
     * @param limit          The page limit.
     * @param permissionIds  List of permission IDs to filter by (optional).
     * @param permissionNames List of permission names to filter by (optional).
     * @return A standardized response with the paginated list of roles.
     */
    @GetMapping("/permissions")
    public ResponseEntity<ResponseHolder> findRolesWithPermissions(
            @RequestParam int offset,
            @RequestParam int limit,
            @RequestParam(required = false) List<Integer> permissionIds,
            @RequestParam(required = false) List<String> permissionNames) {
        try {
            Page<Role> roles = roleService.findRolesWithPermissions(permissionIds, permissionNames, offset, limit);
            return ResponseEntity.ok(
                    ResponseHolder.builder()
                            .response(roles)
                            .message("Roles retrieved successfully.")
                            .build()
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    ResponseHolder.builder()
                            .message(e.getMessage())
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    ResponseHolder.builder()
                            .message("An unexpected error occurred.")
                            .build()
            );
        }
    }


}
