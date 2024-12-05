package com.lucumasystems.authenticationapi.controller;

import com.lucumasystems.authenticationapi.ResponseHolder;
import com.lucumasystems.authenticationapi.entity.User;
import com.lucumasystems.authenticationapi.orm.Mapper;
import com.lucumasystems.authenticationapi.orm.UserRepository;
import com.lucumasystems.authenticationapi.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/authenticate")
@RequiredArgsConstructor
public class TokenAuthenticate {
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @PostMapping("/token")
    public ResponseEntity<ResponseHolder> authenticate(@RequestHeader("Authorization") String token) {
        token = jwtService.extractTokenFromHeader(token);
        User user = userRepository.findActiveUserByUsername(jwtService.getUsername(token)).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ResponseHolder.builder().message("Authenticated").response(Mapper.toUserOutDto(user)).build());
    }
}
