package com.lucumasystems.authenticationapi.controller;

import com.lucumasystems.authenticationapi.ResponseHolder;
import com.lucumasystems.authenticationapi.dto.PermissionsDTO;
import com.lucumasystems.authenticationapi.entity.Permission;
import com.lucumasystems.authenticationapi.service.PermissionService;
import com.lucumasystems.authenticationapi.service.AuditLogService;
import com.lucumasystems.authenticationapi.service.JwtService;
import com.lucumasystems.authenticationapi.error.PermissionAlreadyExistsException;
import com.lucumasystems.authenticationapi.error.PermissionNotFoundException;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;
    private final AuditLogService auditLogService;
    private final JwtService jwtService;

    /**
     * Add a new permission.
     *
     * @param permissionsDTO The data for the permission to be added.
     * @param jwtToken       The JWT token of the user making the request.
     * @return A standardized response with the added permission.
     */
    @PostMapping("/add")
    public ResponseEntity<ResponseHolder> addPermission(@RequestBody @Valid PermissionsDTO permissionsDTO, @RequestHeader("Authorization") String jwtToken) {
        try {
            int createdBy = jwtService.getUserId(jwtToken);
            Permission permission = permissionService.addPermission(permissionsDTO, createdBy);
            auditLogService.logAction(jwtService.getUsername(jwtToken), "ADD_PERMISSION");
            return ResponseEntity.ok(
                    ResponseHolder.builder()
                            .response(permission)
                            .message("Permission added successfully.")
                            .build()
            );
        } catch (PermissionAlreadyExistsException e) {
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
     * Change the status of a permission (active/inactive).
     *
     * @param permissionId The ID of the permission whose status will be changed.
     * @param jwtToken     The JWT token of the user making the request.
     * @return A standardized response with the updated permission.
     */
    @PutMapping("/{permissionId}/status")
    public ResponseEntity<ResponseHolder> changePermissionStatus(@PathVariable int permissionId, @RequestHeader("Authorization") String jwtToken) {
        try {
            int updatedBy = jwtService.getUserId(jwtToken);
            Permission permission = permissionService.changePermissionStatus(permissionId, updatedBy);
            auditLogService.logAction(jwtService.getUsername(jwtToken), "CHANGE_PERMISSION_STATUS");
            return ResponseEntity.ok(
                    ResponseHolder.builder()
                            .response(permission)
                            .message("Permission status updated successfully.")
                            .build()
            );
        } catch (PermissionNotFoundException e) {
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
     * Update a permission.
     *
     * @param permissionId  The ID of the permission to be updated.
     * @param permissionsDTO The new data for the permission.
     * @param jwtToken       The JWT token of the user making the request.
     * @return A standardized response with the updated permission.
     */
    @PutMapping("/{permissionId}")
    public ResponseEntity<ResponseHolder> updatePermission(@PathVariable int permissionId, @RequestBody @Valid PermissionsDTO permissionsDTO, @RequestHeader("Authorization") String jwtToken) {
        try {
            int updatedBy = jwtService.getUserId(jwtToken);
            Permission permission = permissionService.updatePermission(permissionId, permissionsDTO, updatedBy);
            auditLogService.logAction(jwtService.getUsername(jwtToken), "UPDATE_PERMISSION");
            return ResponseEntity.ok(
                    ResponseHolder.builder()
                            .response(permission)
                            .message("Permission updated successfully.")
                            .build()
            );
        } catch (PermissionNotFoundException e) {
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

    // Additional endpoint for paginated permissions or other features can be added here if necessary.

}
