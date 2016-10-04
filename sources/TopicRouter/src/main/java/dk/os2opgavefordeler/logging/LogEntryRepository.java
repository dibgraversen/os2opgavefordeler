package dk.os2opgavefordeler.logging;

import org.apache.deltaspike.data.api.AbstractEntityRepository;
import org.apache.deltaspike.data.api.Repository;

import dk.os2opgavefordeler.model.LogEntry;

@Repository(forEntity = LogEntry.class)
public abstract class LogEntryRepository extends AbstractEntityRepository<LogEntry, Long> {



}