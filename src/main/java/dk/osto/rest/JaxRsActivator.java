package dk.osto.rest;

import dk.os2opgavefordeler.model.kle.KleMainGroup;
import dk.os2opgavefordeler.service.KleImportService;
import dk.os2opgavefordeler.service.PersistenceService;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.io.InputStream;
import java.util.List;

/**
 * A class extending {@link Application} and annotated with @ApplicationPath is the Java EE 6
 * "no XML" approach to activating JAX-RS.
 * 
 * <p>
 * Resources are served relative to the servlet path specified in the {@link ApplicationPath}
 * annotation.
 * </p>
 */
@ApplicationPath("/rest")
public class JaxRsActivator extends Application {
}
