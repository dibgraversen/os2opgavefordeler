package dk.os2opgavefordeler.service;

import dk.os2opgavefordeler.model.LogEntry;

import java.util.List;
import java.util.Optional;

/**
 * Service class for handling audit logging
 */
public interface AuditLogService {

    Optional<LogEntry> getLogEntry(long id);
    List<LogEntry> getAllLogEntries(long municipalityId);

    void saveLogEntry(LogEntry logEntry);

}
