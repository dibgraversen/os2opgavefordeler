package dk.os2opgavefordeler.service.impl;

import dk.os2opgavefordeler.logging.LogEntryRepository;
import dk.os2opgavefordeler.model.LogEntry;
import dk.os2opgavefordeler.service.AuditLogService;

import javax.inject.Inject;
import java.util.Optional;

/**
 * Audit logging service
 */
public class AuditLogServiceImpl implements AuditLogService {

    @Inject
    private LogEntryRepository logEntryRepository;

    @Override
    public Optional<LogEntry> getLogEntry(long id) {
        return Optional.ofNullable(logEntryRepository.findBy(id));
    }

    @Override
    public void saveLogEntry(LogEntry logEntry) {
        logEntryRepository.save(logEntry);
    }

}
