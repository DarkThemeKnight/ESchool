package com.lucumasystems.authenticationapi.controller;

import com.lucumasystems.authenticationapi.ResponseHolder;
import com.lucumasystems.authenticationapi.dto.JwtResponse;
import com.lucumasystems.authenticationapi.dto.LoginDto;
import com.lucumasystems.authenticationapi.dto.UserDTO;
import com.lucumasystems.authenticationapi.entity.Permission;
import com.lucumasystems.authenticationapi.entity.Role;
import com.lucumasystems.authenticationapi.entity.User;
import com.lucumasystems.authenticationapi.orm.Mapper;
import com.lucumasystems.authenticationapi.orm.UserRepository;
import com.lucumasystems.authenticationapi.service.AuditLogService;
import com.lucumasystems.authenticationapi.service.JwtService;
import com.lucumasystems.authenticationapi.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AuthenticationController {
    private final UserService userService;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;



    @PostMapping("/login")
    public ResponseEntity<ResponseHolder> login(@RequestBody LoginDto loginDto) {
        try {
            log.info("Log in request {}", loginDto);
            Authentication authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword()));
            log.info("Authenticated user {}", authentication.getName() +" "+ authentication.getAuthorities());
            User user = userRepository.findActiveUserByUsername(loginDto.getUsername()).orElseThrow(()->new EntityNotFoundException("User not found"));
            List<String> roles = user.getRoles().stream().map(Role::getName).toList();
            List<String> permissions = user.getRoles().stream().flatMap(role -> role.getPermissions().stream()).map(Permission::getName).toList();
            Map<String, Object> claims = new HashMap<>();
            claims.put("username", user.getUsername());
            claims.put("role", roles);
            claims.put("permissions", permissions);
            claims.put("userId", user.getId());
            String token = jwtService.generate(claims, user, JwtService.getDate(3, 'H'));
            JwtResponse response = JwtResponse.builder()
                    .permissions(permissions)
                    .roles(roles)
                    .token(token)
                    .build();

            // Log the successful login action
            auditLogService.logAction(user.getUsername(), "LOGIN");

            return ResponseEntity.ok(ResponseHolder.builder().message("Login Successful").response(response).build());
        } catch (AuthenticationException e) {
            log.error("Error ", e);
            return ResponseEntity.ok(ResponseHolder.builder().message("Invalid username or password").build());
        }
    }

    @PostMapping("/self-register")
    public ResponseEntity<ResponseHolder> selfRegister(@RequestBody UserDTO userDTO) {
        User user = userService.addUser(userDTO, 0);
        auditLogService.logAction(user.getUsername(), "SELF_REGISTER");

        return ResponseEntity.ok(ResponseHolder.builder().message("Self Registration Successful").response(Mapper.toUserOutDto(user)).build());
    }

    @PostMapping("/register")
    public ResponseEntity<ResponseHolder> register(@RequestBody UserDTO userDTO, @RequestHeader("Authorization") String token) {
        token = jwtService.extractTokenFromHeader(token);
        int userId = jwtService.getUserId(token);
        User user = userService.addUser(userDTO, userId);

        // Log the registration action
        auditLogService.logAction(user.getUsername(), "REGISTER");

        return ResponseEntity.ok(ResponseHolder.builder().message("Registration Successful").response(Mapper.toUserOutDto(user)).build());
    }

    @PostMapping("/update")
    public ResponseEntity<ResponseHolder> update(@RequestParam("username") String username, @RequestBody UserDTO userDTO, @RequestHeader("Authorization") String token) {
        token = jwtService.getUsername(token);
        int userId = jwtService.getUserId(token);
        User user = userService.updateUser(userDTO, userId, username);

        // Log the update action
        auditLogService.logAction(user.getUsername(), "UPDATE");

        return ResponseEntity.ok(ResponseHolder.builder().message("Update Successful").response(Mapper.toUserOutDto(user)).build());
    }

    @PutMapping("/reset-password")
    public ResponseEntity<ResponseHolder> resetPassword(@RequestParam @Valid ResetPassword resetPassword, @RequestHeader("Authorization") String token) {
        token = jwtService.extractTokenFromHeader(token);
        long updatedBy = jwtService.getUserId(token);
        User user = userService.resetPassword(resetPassword.getUsername(), resetPassword.getPassword(), updatedBy);

        // Log the password reset action
        auditLogService.logAction(user.getUsername(), "RESET_PASSWORD");

        return ResponseEntity.ok(ResponseHolder.builder().message("Password reset successfully").response(Mapper.toUserOutDto(user)).build());
    }


    @Data
    public static class ResetPassword {
        private String username;
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*?&]{8,20}$", message = "Password must be between 8 and 20 characters, and include at least one letter and one number.")
        private String password;
    }

}
