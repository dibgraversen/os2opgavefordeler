package dk.os2opgavefordeler.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import dk.os2opgavefordeler.model.*;
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
	private UserService usersService;

	@Inject
	private OrgUnitService orgUnitService;

	@Inject
	private EmploymentService employmentService;

	@Inject
	private KleImportService importer;

	@Inject
	private KleService kleService;

	@Inject
	private DistributionService distService;

	@PostConstruct
	public void bootstrap() {
		buildOrgUnits();
		buildUsers();

		final List<Kle> groups = loadBootstrapKle();
		buildDistributionRules();
	}

	private void buildUsers() {
		addUser("Helle Friis Pedersen",		"hfp@miracle.dk", buildRoles());
		addUser("Hans Ehlert Thomsen",		"het@miracle.dk", buildRoles());
		addUser("Henrik Løvborg",			"hlo@miracle.dk", buildRoles());
		addUser("Simon Møgelvang Bang",		"smb@miracle.dk", buildRoles());
		addUser("Sune Marcher",				"sum@miracle.dk", buildRoles());
	}

	private User addUser(String name, String email, List<Role> roles) {
		final User user = new User(name, email, roles);
		return usersService.createUser(user);
	}

	private List<Role> buildRoles() {
		final Employment borge = employmentService.findByEmail("borge@kommune.dk").get(0);
		final Employment kodah = employmentService.findByEmail("kodah@kommune.dk").get(0);
		final Employment admin = employmentService.findByEmail("admin@kommune.dk").get(0);
		final Employment menig = employmentService.findByEmail("menig@kommune.dk").get(0);

		final List<Role> roles = ImmutableList.of(
			Role.builder().name(borge.getName()).employment(borge.getId()).manager(true).build(),
			Role.builder().name(kodah.getName()).employment(kodah.getId()).manager(true).build(),
			Role.builder().name(admin.getName() + " (Kommuneadmin)").employment(admin.getId()).municipalityAdmin(true).build(),
			Role.builder().name(menig.getName() + " (Upriviligeret)").employment(menig.getId()).build(),
			Role.builder().name("Sysadmin").admin(true).build()
		);

		return roles;
	}

	private void buildOrgUnits() {
		log.info("Loading bootstrap organization");
		final OrgUnit rootOrg = loadBootstrapOrgUnit();
		orgUnitService.importOrganization(rootOrg);
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
