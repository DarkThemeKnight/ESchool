package com.lucumasystems.authenticationapi;

import com.lucumasystems.authenticationapi.entity.User;
import com.lucumasystems.authenticationapi.orm.RefreshTokenRepository;
import com.lucumasystems.authenticationapi.orm.UserRepository;
import com.lucumasystems.authenticationapi.service.JwtService;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;


    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String token;
        final String username;
        log.debug("Request received with Authorization header: {}", authHeader);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            if (authHeader == null) {
                log.info("Null Token");
            } else {
                log.info("Token does not start with \"Bearer \"");
            }
            filterChain.doFilter(request, response);
            return;
        }

        try {
            token = authHeader.substring(7);
            log.debug("Token extracted: {}", token);
            username = jwtService.getUsername(token);
            log.debug("Username extracted from token: {}", username);

            if (!username.isEmpty() && SecurityContextHolder.getContext().getAuthentication() == null) {
                User applicationUser = userRepository.findActiveUserByUsername(username).orElse(null);
                if (applicationUser == null) {
                    log.warn("User not found for ID: {}", username);
                    response.sendError(HttpStatus.UNAUTHORIZED.value(), "Username not found");
                    return;
                }
                log.debug("User found: {}", applicationUser);
                if (!applicationUser.isEnabled()) {
                    log.warn("User account is disabled: {}", username);
                    response.sendError(HttpStatus.UNAUTHORIZED.value(), "Disabled Account");
                    return;
                }

                if (!applicationUser.isCredentialsNonExpired()) {
                    log.warn("User credentials are expired: {}", username);
                    response.sendError(HttpStatus.UNAUTHORIZED.value(), "Expired credentials");
                    return;
                }

                if (jwtService.isValidToken(token, applicationUser)) {
                    log.debug("Token is valid for user: {}", username);
                    SecurityContext context = SecurityContextHolder.createEmptyContext();

                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(applicationUser, null, applicationUser.getAuthorities());
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    context.setAuthentication(authenticationToken);
                    SecurityContextHolder.setContext(context);
                    log.debug("Authentication set in security context for user: {}", username);
                }
            }
        } catch (MalformedJwtException e) {
            log.error("Malformed Token: {}", e.getMessage());
            response.sendError(HttpStatus.BAD_REQUEST.value(), "Malformed Jwt Exception");
        } catch (Exception e) {
            log.error("Error occurred during token processing: {}", e.getMessage(), e);
            response.sendError(HttpStatus.BAD_REQUEST.value(), "Exception occur " + e.getMessage());
            return;
        }

        filterChain.doFilter(request, response);
    }
}
