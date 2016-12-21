package dk.os2opgavefordeler.logging;

import org.apache.deltaspike.data.api.AbstractEntityRepository;
import org.apache.deltaspike.data.api.Repository;

import dk.os2opgavefordeler.model.LogEntry;
import org.apache.deltaspike.jpa.api.transaction.Transactional;

@Repository(forEntity = LogEntry.class)
@Transactional
public abstract class LogEntryRepository extends AbstractEntityRepository<LogEntry, Long> {



}