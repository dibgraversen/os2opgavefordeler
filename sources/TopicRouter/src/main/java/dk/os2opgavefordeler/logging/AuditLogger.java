package dk.os2opgavefordeler.logging;

import dk.os2opgavefordeler.model.Municipality;

/**
 * Interface for application audit logging
 */
public interface AuditLogger {

    // base methods
    void create(String kle, String user, String type, String data, String orgUnit, String employment, Municipality municipality);
    void update(String kle, String user, String type, String data, String orgUnit, String employment, Municipality municipality);

}
