package com.lucumasystems.authenticationapi.service;

import com.lucumasystems.authenticationapi.dto.RoleDto;
import com.lucumasystems.authenticationapi.dto.UserOutDto;
import com.lucumasystems.authenticationapi.entity.Permission;
import com.lucumasystems.authenticationapi.entity.Role;
import com.lucumasystems.authenticationapi.entity.User;
import com.lucumasystems.authenticationapi.orm.PermissionRepository;
import com.lucumasystems.authenticationapi.orm.RoleRepository;
import com.lucumasystems.authenticationapi.orm.UserRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@Service
public class RoleService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;

    @Transactional
    public void addRole(RoleDto role, int createdBy) {
        Optional<User> optionalUser =  userRepository.findActiveUserById(createdBy);
        if(optionalUser.isEmpty()) {
            throw new EntityNotFoundException("User not found");
        }
        Optional<Role> roleOptional = roleRepository.findByNameAndIsActive(role.getRole());
        if (roleOptional.isPresent()) {
            throw new EntityExistsException("Role already exists");
        }
        int buffer = 30;
        int offset = 0;
        long outputSize = buffer;
        Set<Permission> permissions = new HashSet<>();
        while (outputSize == buffer) {
            Pageable pageable = PageRequest.of(offset, buffer);
            Page<Permission> permissionPage = permissionRepository.findPermissionsFromPermissionsIdOrPermissionsNames(pageable,new ArrayList<>(), role.getPermissions());
            permissions.addAll(permissionPage.getContent());
            outputSize = permissionPage.getTotalElements();
            offset++;
        }
        Role toSave = Role.builder()
                .permissions(permissions)
                .description(role.getDescription())
                .isActive(true)
                .createdBy(optionalUser.get())
                .name(role.getRole())
                .build();

        roleRepository.save(toSave);
    }

    @Transactional
    public void addPermissionToRole(String roleName, int updatedBy, List<String> permissionName) {
        Optional<User> optionalUser =  userRepository.findActiveUserById(updatedBy);

        if(optionalUser.isEmpty()) {
            throw new EntityNotFoundException("User not found");
        }

        Optional<Role> roleOptional = roleRepository.findByNameAndIsActive(roleName);

        if (roleOptional.isEmpty()) {
            throw new EntityNotFoundException("Role Not Found");
        }

        int buffer = 30;
        int offset = 0;
        long outputSize = buffer;
        Set<Permission> permissions = new HashSet<>();

        while (outputSize == buffer) {
            Pageable pageable = PageRequest.of(offset, buffer);
            Page<Permission> permissionPage = permissionRepository
                    .findPermissionsFromPermissionsIdOrPermissionsNames(pageable,new ArrayList<>(), permissionName);
            permissions.addAll(permissionPage.getContent());
            outputSize = permissionPage.getTotalElements();
            offset++;
        }

        Role r = roleOptional.get();
        r.setPermissions(permissions);
        r.setUpdatedBy(optionalUser.get());

        roleRepository.save(r);
    }
    public void changeRoleStatus(String name, int updatedBy) {
        Optional<User> optionalUser =  userRepository.findActiveUserById(updatedBy);
        if(optionalUser.isEmpty()) {
            throw new EntityNotFoundException("User not found");
        }
        Optional<Role> optionalRole = roleRepository.findByNameAndIsActive(name);
        if(optionalRole.isEmpty()) {
            throw new EntityNotFoundException("Role not found");
        }
        Role role = optionalRole.get();
        role.setActive(!role.isActive());
        role.setUpdatedBy(optionalUser.get());
        roleRepository.save(role);
    }

    public  Page<UserOutDto>  findUsersWithRoles(List<String> roleName, int offset, int limit) {
        Pageable pageable = PageRequest.of(offset, limit);
        return roleRepository.findUsersWithRoleNames(roleName, pageable);
    }

    public Page<Role> getAllRoles(int offset, int limit, String search) {
        Pageable pageable = PageRequest.of(offset, limit);
        return roleRepository.findRolesWithSearch(search, pageable);
    }

    public Page<Role> listRoles(int offset, int limit,  List<Integer> roleIds, List<String> names) {
        Pageable pageable = PageRequest.of(offset, limit);
        return roleRepository.listRoles(pageable, roleIds, names);
    }

    public Page<Role> findRolesWithPermissions(List<Integer> permissionIds, List<String>permissionNames, int offset, int limit) {
        Pageable pageable = PageRequest.of(offset, limit);
        if (permissionIds != null && !permissionIds.isEmpty()) {
            return roleRepository.findRolesWithPermissions(permissionIds, pageable);
        }if (permissionNames != null && !permissionNames.isEmpty()) {
            return roleRepository.findRolesWithPermissionNames(permissionNames, pageable);
        }
        throw new IllegalArgumentException("Empty filters");
    }




}
