package dk.os2opgavefordeler.logging.impl;

import dk.os2opgavefordeler.logging.AuditLogger;
import dk.os2opgavefordeler.model.LogEntry;
import dk.os2opgavefordeler.model.Municipality;
import dk.os2opgavefordeler.service.AuditLogService;

import javax.inject.Inject;

/**
 * Audit logger implementation class
 *
 * String kle, String user, String operation, String type, String data, String orgUnit, String employment
 */
public class AuditLoggerImpl implements AuditLogger {

    @Inject
    AuditLogService auditLogService;

    @Override
    public void create(String kle, String user, String type, String data, String orgUnit, String employment, Municipality municipality) {
        LogEntry logEntry = new LogEntry(kle, user, LogEntry.CREATE_TYPE, type, data, orgUnit, employment, municipality);
        auditLogService.saveLogEntry(logEntry);
    }

    @Override
    public void update(String kle, String user, String type, String data, String orgUnit, String employment, Municipality municipality) {
        LogEntry logEntry = new LogEntry(kle, user, LogEntry.UPDATE_TYPE, type, data, orgUnit, employment, municipality);
        auditLogService.saveLogEntry(logEntry);
    }

}
