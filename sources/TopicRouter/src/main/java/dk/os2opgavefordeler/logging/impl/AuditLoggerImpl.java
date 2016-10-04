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
    public void event(String kle, String user, String operation, String type, String data, String orgUnit, String employment, Municipality municipality) {
        LogEntry logEntry = new LogEntry(kle, user, operation, type, data, orgUnit, employment, municipality);
        auditLogService.saveLogEntry(logEntry);
    }

    @Override
    public void parameterEvent(String user, String operation, String type, String data, Municipality municipality) {
        event("", user, operation, type, data, "", "", municipality);
    }


}
