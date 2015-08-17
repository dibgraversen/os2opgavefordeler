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
import java.util.ArrayList;
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
	// flag for bootstrap enabled or disabled.
	private static final boolean BOOTSTRAP = true;

	private static final String DIGITALISERING = "Digitalisering";
	private static final String MODERN_ART = "Moderne kunst";

	private static final String DEVELOPMENT = "Udvikling";
	private static final String CULTURE = "Kultur";

	private static final String MIRACLE_NAME = "Miracle";
	private static final String SYDDJURS_NAME = "Syddjurs Kommune";

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

	@Inject
	private MunicipalityService mService;

	private Municipality miracle;
	private Municipality syddjurs;

	@PostConstruct
	public void bootstrap() {
		if(BOOTSTRAP){
			addMunicipalities();
			buildOrgUnits();
			buildUsers();

			loadBootstrapKle();
			buildDistributionRulesForMunicipality(miracle, findOrg(DIGITALISERING), findOrg(MODERN_ART));
		}

	}

	private void addMunicipalities() {
		if(mService.getMunicipalities().size() == 0){
			miracle = addMunicipality("Miracle");
			syddjurs = addMunicipality("Syddjurs Kommune");
		} else {
			miracle = mService.findByName(MIRACLE_NAME);
			syddjurs = mService.findByName(SYDDJURS_NAME);
		}
	}

	private Municipality addMunicipality(String name) {
		Municipality m = new Municipality();
		m.setName(name);
		mService.createMunicipality(m);
		return m;
	}

	private void buildUsers() {
		addUser("Helle Friis Pedersen", "hfp@miracle.dk", miracle, buildRoles());
		addUser("Hans Ehlert Thomsen", "het@miracle.dk", miracle, buildRoles());
		addUser("Henrik Løvborg", "hlo@miracle.dk", miracle, buildRoles());
		addUser("Simon Møgelvang Bang", "smb@miracle.dk", miracle, buildRoles());
		addUser("Sune Marcher", "sum@miracle.dk", miracle, buildRoles());
		List<Role> syddsjursRoles = new ArrayList<>();
		syddsjursRoles.add(Role.builder().name("Henrik (Syddjurs)").municipalityAdmin(true).build());
		addUser("Henrik", "henrikloevborg@syddjurs.dk", syddjurs, syddsjursRoles);
	}

	private User addUser(String name, String email, Municipality municipality, List<Role> roles) {
		final User user = new User(name, email, roles);
		user.setMunicipality(municipality);
		return usersService.createUser(user);
	}

	private List<Role> buildRoles() {
		final Employment borge = employmentService.findByEmail("borge@kommune.dk").get(0);
		final Employment kodah = employmentService.findByEmail("kodah@kommune.dk").get(0);
		final Employment admin = employmentService.findByEmail("admin@kommune.dk").get(0);
		final Employment jj = employmentService.findByEmail("jj@kommune.dk").get(0);

		final List<Role> roles = ImmutableList.of(
				Role.builder().name(borge.getName()).employment(borge).manager(true).build(),
				Role.builder().name(kodah.getName()).employment(kodah).manager(true).build(),
				Role.builder().name(admin.getName() + " (Kommuneadmin)").employment(admin).municipalityAdmin(true).build(),
				Role.builder().name(jj.getName() + " (Upriviligeret)").employment(jj).build(),
				Role.builder().name("Sysadmin").admin(true).build()
		);

		return roles;
	}

	private void buildOrgUnits() {
		log.info("Loading bootstrap organization");
		final OrgUnit rootOrg = loadBootstrapOrgUnit();
		orgUnitService.importOrganization(rootOrg);
	}

	private void loadBootstrapKle() {
		log.info("Loading bootstrap KLE");
		if(kleService.fetchAllKleMainGroups().size() < 1){
			try (final InputStream resource = getResource("KLE-valid-data.xml")) {
				final List<Kle> groups = importer.importFromXml(resource);
				kleService.storeAllKleMainGroups(groups);
			} catch (Exception ex) {
				log.error("Couldn't load KLE", ex);
			}
		}
	}

	private OrgUnit loadBootstrapOrgUnit() {
		final ObjectMapper mapper = new ObjectMapper();

		try (final InputStream resource = getResource("KLE-valid-data.xml")) {
			return mapper.readValue(getResource("bootstrap-organization.json"), OrgUnit.class);
		} catch (IOException e) {
			log.error("Couldn't deserialize bootstrap org", e);
		}

		return null;
	}


	private void buildDistributionRulesForMunicipality(Municipality municipality,
																										 OrgUnit org1,
																										 OrgUnit org2) {
		if(distService.getDistributionsAll(municipality.getId()).size()<1){
			createRules(
					// === Fully unassigned group
					DistributionRule.builder()
							.responsibleOrg(null)
							.kle(kleService.fetchMainGroup("00").get())
							.municipality(municipality)
							.children(
									DistributionRule.builder()
											.responsibleOrg(null)
											.kle(kleService.fetchMainGroup("00.01").get())
											.municipality(municipality)
											.children(
													DistributionRule.builder()
															.responsibleOrg(null)
															.kle(kleService.fetchMainGroup("00.01.00").get())
															.municipality(municipality)
															.build()
											)
											.build()
							)
							.build(),

					// === Group with assigned toplevel
					DistributionRule.builder()
							.responsibleOrg(org1)
							.kle(kleService.fetchMainGroup("13").get())
							.municipality(municipality)
							.children(
									DistributionRule.builder()
											.responsibleOrg(null)
											.kle(kleService.fetchMainGroup("13.00").get())
											.municipality(municipality)
											.children(
													DistributionRule.builder()
															.responsibleOrg(null)
															.kle(kleService.fetchMainGroup("13.00.00").get())
															.municipality(municipality)
															.build()
											)
											.build()
							)
							.build(),

					// Group with two assigned levels
					DistributionRule.builder()
							.responsibleOrg(org2)
							.kle(kleService.fetchMainGroup("14").get())
							.municipality(municipality)
							.children(
									DistributionRule.builder()
											.responsibleOrg(org2)
											.kle(kleService.fetchMainGroup("14.00").get())
											.municipality(municipality)
											.children(
													DistributionRule.builder()
															.responsibleOrg(null)
															.kle(kleService.fetchMainGroup("14.00.01").get())
															.municipality(municipality)
															.build()
											)
											.build()
							)
							.build()
			);
		}
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
