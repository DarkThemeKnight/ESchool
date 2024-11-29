package com.lucumasystems.authenticationapi.service;

import com.lucumasystems.authenticationapi.dto.PermissionsDTO;
import com.lucumasystems.authenticationapi.entity.Permission;
import com.lucumasystems.authenticationapi.entity.User;
import com.lucumasystems.authenticationapi.error.PermissionAlreadyExistsException;
import com.lucumasystems.authenticationapi.error.PermissionNotFoundException;
import com.lucumasystems.authenticationapi.orm.PermissionRepository;
import com.lucumasystems.authenticationapi.orm.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;

    @Transactional
    public Permission addPermission(PermissionsDTO permissionsDTO, int createdBy) {
        Optional<User> optionalUser =  userRepository.findActiveUserById(createdBy);
        if(optionalUser.isEmpty()) {
            throw new EntityNotFoundException("User not found");
        }

        Optional<Permission> optionalPermission = permissionRepository.findByNameAndActive(permissionsDTO.getPermission());
        if (optionalPermission.isPresent()) {
            throw new PermissionAlreadyExistsException("Permission already exists");
        }
        Permission permission = Permission.builder()
                .active(true)
                .name(permissionsDTO.getPermission())
                .description(permissionsDTO.getDescription())
                .createdBy(optionalUser.get())
                .build();
        return permissionRepository.save(permission);
    }

    @Transactional
    public Permission changePermissionStatus(int permissionId, int updatedBy) {
        Optional<User> optionalUser =  userRepository.findActiveUserById(updatedBy);
        if(optionalUser.isEmpty()) {
            throw new EntityNotFoundException("User not found");
        }
        Optional<Permission> optionalPermission = permissionRepository.findById(permissionId);
        if (optionalPermission.isEmpty()) {
            throw new PermissionNotFoundException("Permission does not exist");
        }
        Permission permission = optionalPermission.get();
        permission.setActive(!permission.isActive());
        permission.setUpdatedBy(optionalUser.get());
        return permissionRepository.save(permission);
    }

    public Page<Permission> findPermissionsFromPermissionsIdOrPermissionsNames(int offset, int limit, List<Integer> permissionIds, List<String> permissionsNames) {
        Pageable pageable = PageRequest.of(offset, limit);
        return permissionRepository.findPermissionsFromPermissionsIdOrPermissionsNames(pageable, permissionIds, permissionsNames);
    }

    public Permission updatePermission(int permissionId, PermissionsDTO permissionsDTO, int updatedBy) {
        Optional<User> optionalUser =  userRepository.findActiveUserById(updatedBy);
        if(optionalUser.isEmpty()) {
            throw new EntityNotFoundException("User not found");
        }
        Optional<Permission> optionalPermission = permissionRepository.findById(permissionId);
        if (optionalPermission.isPresent()) {
            Permission permission = optionalPermission.get();
            if (permissionsDTO.getPermission() != null) {
                permission.setName(permissionsDTO.getPermission());
            }
            if (permissionsDTO.getDescription() != null) {
                permission.setDescription(permissionsDTO.getDescription());
            }
            permission.setUpdatedBy(optionalUser.get());
            return permissionRepository.save(permission);
        }
        throw new PermissionNotFoundException("Permission does not exist");
    }

}
