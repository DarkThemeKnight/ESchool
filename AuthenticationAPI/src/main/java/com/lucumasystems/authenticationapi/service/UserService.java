package com.lucumasystems.authenticationapi.service;

import com.lucumasystems.authenticationapi.dto.PermissionsDTO;
import com.lucumasystems.authenticationapi.dto.UserDTO;
import com.lucumasystems.authenticationapi.entity.Permission;
import com.lucumasystems.authenticationapi.entity.Role;
import com.lucumasystems.authenticationapi.entity.User;
import com.lucumasystems.authenticationapi.orm.PermissionRepository;
import com.lucumasystems.authenticationapi.orm.RoleRepository;
import com.lucumasystems.authenticationapi.orm.UserRepository;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final PermissionService permissionService;

    public User addUser(UserDTO userDTO, long createdBy) {
        User creator = null;
        Optional<User> alreadyExistingUser = userRepository.findActiveUserByUsername(userDTO.getUsername());
        if (alreadyExistingUser.isPresent()) {
            throw new EntityExistsException("User already exists");
        }
        if (createdBy > 0){
            creator = userRepository.findById(createdBy).orElseThrow(()->new EntityNotFoundException("Creator not found"));
        }
        List<String> roleNames = userDTO.getRoles();
        List<Role> roles = new ArrayList<>();
        int buffer = 100;
        int offset = 0;
        int defaultSize = buffer;
        while (buffer == defaultSize){
            Page<Role> rolesPaged = roleRepository.listRoles(PageRequest.of(offset,buffer), new ArrayList<>(), roleNames);
            roles.addAll(rolesPaged.getContent());
            offset++;
            defaultSize = rolesPaged.getContent().size();
        }
        User toSave = User.builder()
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .enabled(true)
                .username(userDTO.getUsername())
                .password(passwordEncoder.encode(userDTO.getPassword()))
                .build();


        if (createdBy > 0){
            toSave.setCreatedBy(creator);
        }
        toSave = userRepository.save(toSave);
        if (createdBy < 1){
            toSave.setCreatedBy(toSave);
        }
        toSave = userRepository.save(toSave);
        return toSave;
    }

    public User updateUser(UserDTO userDTO, long updatedBy, String username) {
        User existingUser = userRepository.findActiveUserByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        User updater = userRepository.findById(updatedBy)
                .orElseThrow(() -> new EntityNotFoundException("Updater not found"));

        if (userDTO.getUsername() != null) {
            existingUser.setUsername(userDTO.getUsername());
        }

        if (userDTO.getPassword() != null) {
            existingUser.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }

        if (userDTO.getRoles() != null && !userDTO.getRoles().isEmpty()) {
            List<Role> roles = roleRepository.findAllByNameIn(userDTO.getRoles());
            existingUser.setRoles(new HashSet<>(roles));
        }

        existingUser.setUpdatedBy(updater);
        return userRepository.save(existingUser);
    }

    public void deactivateUser(Long userId, long updatedBy) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        User updater = userRepository.findById(updatedBy)
                .orElseThrow(() -> new EntityNotFoundException("Updater not found"));

        user.setEnabled(false);
        user.setUpdatedBy(updater);
        userRepository.save(user);
    }

    public void activateUser(Long userId, long updatedBy) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        User updater = userRepository.findById(updatedBy)
                .orElseThrow(() -> new EntityNotFoundException("Updater not found"));

        user.setEnabled(true);
        user.setUpdatedBy(updater);
        userRepository.save(user);
    }

    public User assignRolesToUser(Long userId, List<String> roleNames, long updatedBy) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        User updater = userRepository.findById(updatedBy)
                .orElseThrow(() -> new EntityNotFoundException("Updater not found"));
        List<Role> roles = roleRepository.findAllByNameIn(roleNames);
        user.getRoles().addAll(roles);
        user.setUpdatedBy(updater);
        return userRepository.save(user);
    }

    public User removeRolesFromUser(Long userId, List<String> roleNames, long updatedBy) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        User updater = userRepository.findById(updatedBy)
                .orElseThrow(() -> new EntityNotFoundException("Updater not found"));

        user.getRoles().removeIf(role -> roleNames.contains(role.getName()));
        user.setUpdatedBy(updater);

        return userRepository.save(user);
    }

    public User resetPassword(String username, String newPassword, long updatedBy) {
        User user = userRepository.findActiveUserByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        User updater = userRepository.findById(updatedBy)
                .orElseThrow(() -> new EntityNotFoundException("Updater not found"));

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedBy(updater);

        return userRepository.save(user);
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findActiveUserByUsername(username).orElseThrow(()-> new UsernameNotFoundException("User not found"));
    }



}
