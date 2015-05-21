package dk.os2opgavefordeler.service;

import dk.os2opgavefordeler.model.DistributionRule;
import dk.os2opgavefordeler.model.Role;
import dk.os2opgavefordeler.model.UserSettings;
import dk.os2opgavefordeler.model.Kle;
import dk.os2opgavefordeler.model.presentation.FilterScope;
import dk.os2opgavefordeler.model.presentation.RolePO;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

/**
 * This class serves the sole purpose of providing bootstrap data to work on, while in development.
 * @author hlo@miracle.dk
 */
@Singleton
@Startup
public class BootstrappingDataProviderSingleton {
	@Inject
	private Logger log;

	@Inject
	UsersService usersService;

	@Inject
	private KleImportService importer;

	@Inject
	KleService kleService;

	@Inject
	DistributionService distService;

	@PostConstruct
	public void bootstrap() {
		buildRoles();
		buildUserSettings();

		final List<Kle> groups = loadBootstrapKle();
	}

	public void buildRoles(){
		log.warn("Starting Singleton - loading mock roles");
		buildForUserOne();
		buildForUserTwo();
	}

	private void buildForUserOne(){
		UsersServiceMock mock = new UsersServiceMock();
		RolePO rolePO1 = mock.buildRolePO(1, 1, "Henrik(dig)", 1, false, false, false, false);
		Role role = rolePO1.toRole();
		usersService.createRole(role);
		RolePO rolePO2 = mock.buildRolePO(2, 1, "Admin", 0, false, true, false, false);
		usersService.createRole(rolePO2.toRole());
		RolePO rolePO3 = mock.buildRolePO(3, 1, "Jørgen Jensen", 2, false, false, true, false);
		usersService.createRole(rolePO3.toRole());
		RolePO rolePO4 = mock.buildRolePO(4, 1, "Hans Jørgensen", 3, false, false, false, true);
		usersService.createRole(rolePO4.toRole());
	}

	private void buildForUserTwo(){
		UsersServiceMock mock = new UsersServiceMock();
		RolePO rolePO1 = mock.buildRolePO(5, 2, "Joe(dig)", 4, false, false, false, false);
		Role role = rolePO1.toRole();
		usersService.createRole(role);
		RolePO rolePO2 = mock.buildRolePO(6, 2, "Admin", 0, false, true, false, false);
		usersService.createRole(rolePO2.toRole());
		RolePO rolePO3 = mock.buildRolePO(7, 2, "Adam Savage", 5, false, false, true, false);
		usersService.createRole(rolePO3.toRole());
		RolePO rolePO4 = mock.buildRolePO(8, 2, "Homer Simpson", 6, false, false, false, true);
		usersService.createRole(rolePO4.toRole());
	}

	private void buildUserSettings(){
		buildUserSettingsForUserOne();
		buildUserSettingsForUserTwo();
	}

	private void buildUserSettingsForUserOne(){
		log.warn("Starting Singleton - loading mock user settings");
		UserSettings settings = new UserSettings();
		settings.setId(1);
		settings.setUserId(1);
		settings.setScope(FilterScope.INHERITED);
		settings.setShowResponsible(false);
		settings.setShowExpandedOrg(false);
		usersService.createUserSettings(settings);
	}

	private void buildUserSettingsForUserTwo(){
		log.warn("Starting Singleton - loading mock user settings");
		UserSettings settings = new UserSettings();
		settings.setId(2);
		settings.setUserId(2);
		settings.setScope(FilterScope.ALL);
		settings.setShowResponsible(true);
		settings.setShowExpandedOrg(false);
		usersService.createUserSettings(settings);
	}

	private List<Kle> loadBootstrapKle() {
		log.info("Loading bootstrap KLE");
		try(final InputStream resource = getResource("KLE-valid-data.xml")) {
			final List<Kle> groups = importer.importFromXml(resource);
			kleService.storeAllKleMainGroups(groups);
			return groups;
		} catch (Exception ex) {
			log.error("Couldn't load KLE", ex);
			return Collections.emptyList();
		}
	}

	private InputStream getResource(String name) {
		return Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
	}
}
