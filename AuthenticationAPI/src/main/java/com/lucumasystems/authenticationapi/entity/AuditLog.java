package com.lucumasystems.authenticationapi.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String action; // LOGIN, LOGOUT, FAILED_ATTEMPT
    private LocalDateTime timestamp;
}
