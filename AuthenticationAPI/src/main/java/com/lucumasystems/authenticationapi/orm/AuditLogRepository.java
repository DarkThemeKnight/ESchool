package com.lucumasystems.authenticationapi.orm;

import com.lucumasystems.authenticationapi.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    // Find logs by username
    List<AuditLog> findByUsername(String username);

    // Find logs by action
    List<AuditLog> findByAction(String action);

    // Find logs by timestamp range
    List<AuditLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
}
