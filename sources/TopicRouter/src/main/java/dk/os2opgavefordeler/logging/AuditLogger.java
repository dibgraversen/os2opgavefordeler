package dk.os2opgavefordeler.logging;

import dk.os2opgavefordeler.model.Municipality;

/**
 * Interface for application audit logging
 */
public interface AuditLogger {

    // base methods
    void event(String kle, String user, String operation, String type, String data, String orgUnit, String employment, Municipality municipality);

    void parameterEvent(String user, String operation, String type, String data, Municipality municipality);

}
