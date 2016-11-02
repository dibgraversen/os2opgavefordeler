package dk.os2opgavefordeler.service.impl;

import dk.os2opgavefordeler.logging.LogEntryRepository;

import dk.os2opgavefordeler.model.DistributionRuleFilterName;
import dk.os2opgavefordeler.model.LogEntry;

import dk.os2opgavefordeler.service.AuditLogService;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import java.util.List;
import java.util.Optional;

/**
 * Audit logging service
 */
public class AuditLogServiceImpl implements AuditLogService {

    @Inject
    private LogEntryRepository logEntryRepository;

    @Inject
    private EntityManager entityManager;

    @Inject
    Logger log;

    @Override
    public Optional<LogEntry> getLogEntry(long id) {
        return Optional.ofNullable(logEntryRepository.findBy(id));
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<LogEntry> getAllLogEntries(long municipalityId) {
        Query query = entityManager.createQuery("SELECT logEntry FROM LogEntry logEntry WHERE logEntry.municipality.id = :municipalityId ORDER BY logEntry.id ASC");
        query.setParameter("municipalityId", municipalityId);

        return query.getResultList();
    }

    @Override
    public void saveLogEntry(LogEntry logEntry) {
        logEntryRepository.save(logEntry);
    }

}
