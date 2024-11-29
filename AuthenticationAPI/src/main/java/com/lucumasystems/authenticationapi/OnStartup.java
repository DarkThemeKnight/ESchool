package com.lucumasystems.authenticationapi;

import com.lucumasystems.authenticationapi.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor(onConstructor = @__(@Lazy))
@EnableWebSecurity
@EnableMethodSecurity
@Slf4j

public class OnStartup {
    private final UserDetailsService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtAuthFilter jwtAuthFilter;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder);
        daoAuthenticationProvider.setUserDetailsService(userService);
        return daoAuthenticationProvider;
    }


    @Bean
    public DefaultSecurityFilterChain securityFilterChain(HttpSecurity security) {
        DefaultSecurityFilterChain filterChain = null;
        try {
            filterChain = security.csrf(AbstractHttpConfigurer::disable)
                    .csrf(AbstractHttpConfigurer::disable)
                    .sessionManagement(managementConfigure -> managementConfigure.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .authorizeHttpRequests(authorizationManagerRequestMatcherRegistry ->
                            authorizationManagerRequestMatcherRegistry
                                    .requestMatchers("/api/permissions/**")
                                    .hasRole("SUPER_ADMIN")
                                    .anyRequest()
                                    .permitAll()
                    )
                    .authenticationManager(authenticationManager)
                    .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                    .exceptionHandling(
                            httpSecurityExceptionHandlingConfigurer ->
                                    httpSecurityExceptionHandlingConfigurer.
                                            authenticationEntryPoint(
                                                    (request, response, authException) ->{
                                                        log.error("Unauthorized error: cant access resource request -> {} response -> {}, error ->  {}",
                                                                request.getRequestURI(),response ,authException.getMessage());
                                                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
                                                    }
                                            )
                                            .accessDeniedHandler(
                                                    (request, response, accessDeniedException) ->{
                                                        log.error("Unauthorized error: cant access resource requestEndpoint -> {}, response -> {}, error ->  {}",
                                                                request.getRequestURI(),response ,accessDeniedException.getMessage());
                                                        response.sendError(HttpServletResponse.SC_FORBIDDEN, accessDeniedException.getMessage());
                                                    }
                                            )
                    )
                    .build();
        }catch (Exception e) {
            log.error("Exception in filter Security filter chain");
            throw new RuntimeException  (e);
        }
        return filterChain;
    }
}
