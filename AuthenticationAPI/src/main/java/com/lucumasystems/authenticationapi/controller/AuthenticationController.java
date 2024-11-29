package com.lucumasystems.authenticationapi.controller;

import com.lucumasystems.authenticationapi.ResponseHolder;
import com.lucumasystems.authenticationapi.dto.JwtResponse;
import com.lucumasystems.authenticationapi.dto.LoginDto;
import com.lucumasystems.authenticationapi.dto.UserDTO;
import com.lucumasystems.authenticationapi.entity.Permission;
import com.lucumasystems.authenticationapi.entity.Role;
import com.lucumasystems.authenticationapi.entity.User;
import com.lucumasystems.authenticationapi.orm.RoleRepository;
import com.lucumasystems.authenticationapi.orm.UserRepository;
import com.lucumasystems.authenticationapi.service.AuditLogService;
import com.lucumasystems.authenticationapi.service.JwtService;
import com.lucumasystems.authenticationapi.service.UserService;
import jakarta.annotation.security.RolesAllowed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AuthenticationController {
    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final AuditLogService auditLogService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;



    @PostMapping
    public ResponseEntity<ResponseHolder> login(@RequestBody LoginDto loginDto) {
        try {
            log.info("Log in request {}", loginDto);
            Authentication authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword()));
            System.out.println("Authentication "+ authentication);
            User user = userRepository.findActiveUserByUsername(loginDto.getUsername()).get();
            List<String> roles = user.getRoles().stream().map(Role::getName).toList();
            List<String> permissions = user.getRoles().stream().flatMap(role -> role.getPermissions().stream()).map(Permission::getName).toList();
            Map<String, Object> claims = new HashMap<>();
            claims.put("username", user.getUsername());
            claims.put("role", roles);
            claims.put("permissions", permissions);
            claims.put("userId",user.getId());
            String token = jwtService.generate(claims, user, JwtService.getDate(3,'H'));
            JwtResponse response = JwtResponse.builder()
                    .permissions(permissions)
                    .roles(roles)
                    .token(token)
                    .build();
            return ResponseEntity.ok(ResponseHolder.builder().message("Login Successful").response(response).build());
        }catch (AuthenticationException e) {
            log.error("Error ",e);
            return ResponseEntity.ok(ResponseHolder.builder().message("Invalid username or password").build());
        }
    }




}
