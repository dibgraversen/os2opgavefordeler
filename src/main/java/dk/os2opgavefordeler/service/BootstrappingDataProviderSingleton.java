package dk.os2opgavefordeler.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.os2opgavefordeler.model.*;
import dk.os2opgavefordeler.model.presentation.FilterScope;
import dk.os2opgavefordeler.model.presentation.RolePO;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

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
	UserService usersService;

	@Inject
	OrgUnitService orgUnitService;

	@Inject
	private KleImportService importer;

	@Inject
	KleService kleService;

	@Inject
	DistributionService distService;

	@PostConstruct
	public void bootstrap() {
		buildUsers();

		buildRoles();
		buildUserSettings();

		buildOrgUnits();

		final List<Kle> groups = loadBootstrapKle();
		buildDistributionRules();
	}

	private void buildUsers() {
		usersService.createUser(new User("hfp@miracle.dk", Collections.<Role>emptyList()));
		usersService.createUser(new User("hlo@miracle.dk", Collections.<Role>emptyList()));
		usersService.createUser(new User("sum@miracle.dk", Collections.<Role>emptyList()));
	}

	public void buildRoles(){
		log.warn("Starting Singleton - loading mock roles");
		buildForUserOne();
		buildForUserTwo();
		buildForUserThree();
	}

	private void buildForUserOne(){
		UsersServiceMock mock = new UsersServiceMock();
		RolePO rolePO1 = RolePO.builder().id(1).userId(1).name("Henrik(dig)").employment(1)
				.manager(false).admin(false).municipalityAdmin(false).substitute(false).build();
		usersService.createRole(rolePO1.toRole());
		RolePO rolePO2 = RolePO.builder().id(2).userId(1).name("sysadm").employment(0)
				.manager(false).admin(true).municipalityAdmin(false).substitute(false).build();
		usersService.createRole(rolePO2.toRole());
		RolePO rolePO3 = RolePO.builder().id(3).userId(1).name("Borge Meister").employment(2)
				.manager(false).admin(false).municipalityAdmin(true).substitute(false).build();
		usersService.createRole(rolePO3.toRole());
		RolePO rolePO4 = RolePO.builder().id(4).userId(1).name("Olfert Kvium").employment(3)
				.manager(false).admin(false).municipalityAdmin(false).substitute(true).build();
		usersService.createRole(rolePO4.toRole());
		RolePO rolePO5 = RolePO.builder().id(13).userId(1).name("David Hilbert").employment(10)
				.manager(true).admin(false).municipalityAdmin(false).substitute(true).build();
		usersService.createRole(rolePO5.toRole());
	}

	private void buildForUserTwo(){
		UsersServiceMock mock = new UsersServiceMock();
		RolePO rolePO1 = RolePO.builder().id(5).userId(2).name("Sune(dig)").employment(4)
				.manager(false).admin(true).municipalityAdmin(false).substitute(false).build();
		usersService.createRole(rolePO1.toRole());
	}

	private void buildForUserThree(){
		UsersServiceMock mock = new UsersServiceMock();
		RolePO rolePO1 = RolePO.builder().id(9).userId(3).name("Helle(dig)").employment(7)
				.manager(false).admin(false).municipalityAdmin(false).substitute(false).build();
		usersService.createRole(rolePO1.toRole());
		RolePO rolePO2 = RolePO.builder().id(10).userId(3).name("sysadmin").employment(0)
				.manager(false).admin(true).municipalityAdmin(false).substitute(false).build();
		usersService.createRole(rolePO2.toRole());
		RolePO rolePO3 = RolePO.builder().id(11).userId(3).name("Adam Savage").employment(5)
				.manager(false).admin(false).municipalityAdmin(true).substitute(false).build();
		usersService.createRole(rolePO3.toRole());
		RolePO rolePO4 = RolePO.builder().id(12).userId(3).name("Linus Thorvalds").employment(6)
				.manager(false).admin(false).municipalityAdmin(false).substitute(true).build();
		usersService.createRole(rolePO4.toRole());
	}

	private void buildUserSettings(){
		buildUserSettingsForUserOne();
		buildUserSettingsForUserTwo();
	}

	private void buildOrgUnits() {
		log.info("Loading bootstrap organization");
		final OrgUnit rootOrg = loadBootstrapOrgUnit();
		orgUnitService.importOrganization(rootOrg);
	}

	private void buildUserSettingsForUserOne(){
		log.warn("Starting Singleton - loading mock user settings");
		UserSettings settings = new UserSettings();
		settings.setUserId(1);
		settings.setScope(FilterScope.INHERITED);
		settings.setShowResponsible(false);
		settings.setShowExpandedOrg(false);
		usersService.createUserSettings(settings);
	}

	private void buildUserSettingsForUserTwo(){
		log.warn("Starting Singleton - loading mock user settings");
		UserSettings settings = new UserSettings();
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

	private OrgUnit loadBootstrapOrgUnit() {
		final ObjectMapper mapper = new ObjectMapper();

		try (final InputStream resource = getResource("KLE-valid-data.xml")){
			return mapper.readValue(getResource("bootstrap-organization.json"), OrgUnit.class);
		} catch (IOException e) {
			log.error("Couldn't deserialize bootstrap org", e);
		}

		return null;
	}


	private void buildDistributionRules() {
		createRules(
			// === Fully unassigned group
			DistributionRule.builder()
				.responsibleOrg(null)
				.kle(kleService.fetchMainGroup("00").get())
				.children(
					DistributionRule.builder()
						.responsibleOrg(null)
						.kle(kleService.fetchMainGroup("00.01").get())
						.children(
							DistributionRule.builder()
								.responsibleOrg(null)
								.kle(kleService.fetchMainGroup("00.01.00").get())
							.build()
						)
					.build()
				)
			.build(),

			// === Group with assigned toplevel
			DistributionRule.builder()
				.responsibleOrg(findOrg("Digitalisering"))
				.kle(kleService.fetchMainGroup("13").get())
				.children(
					DistributionRule.builder()
						.responsibleOrg(null)
						.kle(kleService.fetchMainGroup("13.00").get())
						.children(
							DistributionRule.builder()
								.responsibleOrg(null)
								.kle(kleService.fetchMainGroup("13.00.00").get())
							.build()
						)
					.build()
				)
			.build(),

			// Group with two assigned levels
			DistributionRule.builder()
				.responsibleOrg(findOrg("Moderne kunst"))
				.kle(kleService.fetchMainGroup("14").get())
				.children(
					DistributionRule.builder()
						.responsibleOrg(findOrg("Moderne kunst"))
						.kle(kleService.fetchMainGroup("14.00").get())
						.children(
							DistributionRule.builder()
								.responsibleOrg(null)
								.kle(kleService.fetchMainGroup("14.00.01").get())
								.build()
						)
						.build()
				)
			.build()
		);
	}

	// =================================================================================================================
	//	Helpers
	// =================================================================================================================
	private OrgUnit findOrg(String name) {
		return orgUnitService.findByName(name).get(0);
	}

	private void createRules(DistributionRule... rules) {
		Stream.of(rules).forEach(distService::createDistributionRule);
	}

	private InputStream getResource(String name) {
		return Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
	}
}
