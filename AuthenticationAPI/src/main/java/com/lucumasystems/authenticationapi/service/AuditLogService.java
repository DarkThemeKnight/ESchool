package com.lucumasystems.authenticationapi.service;

import com.lucumasystems.authenticationapi.entity.AuditLog;
import com.lucumasystems.authenticationapi.orm.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    /**
     * Log an action performed by a user.
     *
     * @param username The username of the user performing the action.
     * @param action   The action performed (e.g., LOGIN, LOGOUT, FAILED_ATTEMPT).
     */
    public void logAction(String username, String action) {
        AuditLog auditLog = new AuditLog();
        auditLog.setUsername(username);
        auditLog.setAction(action);
        auditLog.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(auditLog);
    }

    /**
     * Retrieve all audit logs.
     *
     * @return A list of all audit logs.
     */
    public List<AuditLog> getAllLogs() {
        return auditLogRepository.findAll();
    }

    /**
     * Retrieve paged audit logs.
     *
     * @param page The page number.
     * @param size The page size.
     * @return A paged result of audit logs.
     */
    public Page<AuditLog> getPagedLogs(int page, int size) {
        return auditLogRepository.findAll(PageRequest.of(page, size));
    }

    /**
     * Retrieve audit logs for a specific user.
     *
     * @param username The username to filter logs.
     * @return A list of audit logs for the specified user.
     */
    public List<AuditLog> getLogsByUsername(String username) {
        return auditLogRepository.findAll().stream()
                .filter(log -> log.getUsername().equalsIgnoreCase(username))
                .toList();
    }

    /**
     * Retrieve audit logs for a specific action.
     *
     * @param action The action to filter logs (e.g., LOGIN, LOGOUT, FAILED_ATTEMPT).
     * @return A list of audit logs for the specified action.
     */
    public List<AuditLog> getLogsByAction(String action) {
        return auditLogRepository.findAll().stream()
                .filter(log -> log.getAction().equalsIgnoreCase(action))
                .toList();
    }

    /**
     * Delete all audit logs before a specific date.
     *
     * @param beforeDate The cutoff date for deletion.
     */
    public void deleteLogsBeforeDate(LocalDateTime beforeDate) {
        List<AuditLog> logsToDelete = auditLogRepository.findAll().stream()
                .filter(log -> log.getTimestamp().isBefore(beforeDate))
                .toList();
        auditLogRepository.deleteAll(logsToDelete);
    }
}
