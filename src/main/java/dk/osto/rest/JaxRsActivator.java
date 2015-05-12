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
	@Inject
	private Logger log;

	@Inject
	private KleImportService importer;

	@Inject
	PersistenceService ps;

	@PostConstruct
	private void init()
	{
		log.info("REST API initialized");

		// During the development cycle, we have frequent application redeployments and non-persistent datastore.
		// To avoid the hassle of importing data via the REST endpoints, let's load some bootstrap data here.
		loadBootstrapKle();
	}

	private void loadBootstrapKle() {
		log.info("Loading bootstrap KLE");
		try(final InputStream resource = getResource("KLE-valid-data.xml")) {
			final List<KleMainGroup> groups = importer.importFromXml(resource);
			ps.storeAllKleMainGroups(groups);
		} catch (Exception ex) {
			log.error("Couldn't load KLE", ex);
		}
	}

	private InputStream getResource(String name) {
		return Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
	}
}
