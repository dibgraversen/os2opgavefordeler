package dk.os2opgavefordeler.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import dk.os2opgavefordeler.repository.EmploymentRepository;
import dk.os2opgavefordeler.repository.MunicipalityRepository;
import dk.os2opgavefordeler.repository.UserRepository;
import dk.os2opgavefordeler.model.*;
import dk.os2opgavefordeler.orgunit.ImportService;
import dk.os2opgavefordeler.orgunit.OrgUnitDTO;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * This class serves the sole purpose of providing bootstrap data to work on, while in development.
 *
 * @author hlo@miracle.dk
 */
@ApplicationScoped
public class BootstrappingDataProviderSingleton {
	//	public static final String KLE_FILE = "KLE-valid-data.xml";
	public static final String KLE_FILE = "KLE-Emneplan_Version2-0_2015-08-01.xml";
	// flag for bootstrap enabled or disabled.
	private static final String DIGITALISERING = "Digitalisering";
	private static final String MODERN_ART = "Moderne kunst";
	private static final String DEVELOPMENT = "Udvikling";
	private static final String CULTURE = "Kultur";
	private static final String MIRACLE_NAME = "Miracle";
	private static final String SYDDJURS_NAME = "Syddjurs Kommune";

	private boolean buildLight = true;

	@Inject
	private Logger log;

	@Inject
	private UserService usersService;

	@Inject
	private UserRepository userRepository;

	@Inject
	private OrgUnitService orgUnitService;

	@Inject
	private ImportService importService;

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

	@Inject
	private EmploymentRepository employmentRepository;

	@Inject
	private MunicipalityRepository municipalityRepository;

	private Municipality miracle;
	private Municipality syddjurs;

	public void bootstrap() {
		if (municipalityRepository.findAll().size() > 0) {
			log.warn("Bootstrap attempted with municipalities existing, returning without action");
			return;
		}
		addMunicipalities();
		log.warn("municipalities added");
		log.warn("Miracle: {}", miracle);
		log.warn("Syddjurs: {}", syddjurs);
		buildOrgUnits(miracle.getId());
		buildUsers();

		loadBootstrapKle();
		if (buildLight) {
			buildDistributionRulesForMunicipality(miracle, findOrg(DIGITALISERING), findOrg(MODERN_ART));
		} else {
			buildAllRules();
		}
	}

	private void addMunicipalities() {
		if (mService.getMunicipalities().size() == 0) {
			miracle = addMunicipality(MIRACLE_NAME, "ABC");
			syddjurs = addMunicipality(SYDDJURS_NAME, "DEF");
		} else {
			miracle = municipalityRepository.findByName(MIRACLE_NAME);
			syddjurs = municipalityRepository.findByName(SYDDJURS_NAME);
		}
	}

	private Municipality addMunicipality(String name, String token) {
		Municipality m = new Municipality();
		m.setName(name);
		m.setActive(true);
		m.setToken(token);
		return municipalityRepository.save(m);
	}

	private void buildUsers() {
		addUser("Helle Friis Pedersen", "hfp@miracle.dk", miracle, buildRoles());
		addUser("Hans Ehlert Thomsen", "het@miracle.dk", miracle, buildRoles());
		addUser("Henrik Løvborg", "hlo@miracle.dk", miracle, buildRole("Henrik Løvborg", "hlo@miracle.dk", true, true, true, true));
		addUser("Simon Møgelvang Bang", "smb@miracle.dk", miracle, buildRoles());
		addUser("Sune Marcher", "sum@miracle.dk", miracle, buildRoles());
		addUser("Rasmus Rosendal", "rro@miracle.dk", miracle, buildRole("Rasmus Rosendal", "rro@miracle.dk", true, true, true, true));
		
		addUser("Daniel Torres", "dto@digital-identity.dk", miracle, buildRole("Daniel Torres", "dto@digital-identity.dk", true, true, true, true));
		addUser("Piotr Suski", "psu@digital-identity.dk", miracle, buildRole("Piotr Suski", "psu@digital-identity.dk", true, true, true, false));
		
		List<Role> syddsjursRoles = new ArrayList<>();
		syddsjursRoles.add(Role.builder().name("Henrik (Syddjurs)").admin(true).municipalityAdmin(true).build());
		addUser("Henrik", "henrikloevborg@syddjurs.dk", syddjurs, syddsjursRoles);
	}

	private User addUser(String name, String email, Municipality municipality, List<Role> roles) {
		final User user = new User(name, email, roles);
		Municipality m = municipalityRepository.findBy(municipality.getId());
		user.setMunicipality(m);
		return userRepository.save(user);
	}

	private List<Role> buildRole(String name, String email, boolean admin, boolean manager, boolean municipalityAdmin, boolean kleAssigner){
		Employment employment = employmentRepository.findByEmail(email);
		return ImmutableList.of(Role.builder().name(name).employment(employment).admin(admin).manager(manager).municipalityAdmin(municipalityAdmin).kleAssigner(kleAssigner).build());
	}


	private List<Role> buildRoles() {
		final Employment borge = employmentRepository.findByEmail("borge@kommune.dk");
		final Employment kodah = employmentRepository.findByEmail("kodah@kommune.dk");
		final Employment admin = employmentRepository.findByEmail("admin@kommune.dk");
		final Employment jj = employmentRepository.findByEmail("jj@kommune.dk");

		final List<Role> roles = ImmutableList.of(
				Role.builder().name(borge.getName()).employment(borge).manager(true).build(),
				Role.builder().name(kodah.getName()).employment(kodah).manager(true).build(),
				Role.builder().name(admin.getName() + " (Kommuneadmin)").employment(admin).municipalityAdmin(true).build(),
				Role.builder().name(jj.getName() + " (Upriviligeret)").employment(jj).build(),
				Role.builder().name("Sysadmin").admin(true).build()
		);
		return roles;
	}

	private void buildOrgUnits(long municipalityId) {
		log.info("Loading bootstrap organization");
		final OrgUnitDTO rootOrg = loadBootstrapOrgUnit();
		try {
			importService.importOrganization(municipalityId, rootOrg);
		} catch (ImportService.InvalidMunicipalityException e) {
			log.error("failed Org Import", e);
		}
	}

	private void loadBootstrapKle() {
		log.info("Loading bootstrap KLE");
		if (kleService.fetchAllKleMainGroups().size() < 1) {
			try (final InputStream resource = getResource(KLE_FILE)) {
				final List<Kle> groups = importer.importFromXml(resource);
				kleService.storeAllKleMainGroups(groups);
			} catch (Exception ex) {
				log.error("Couldn't load KLE", ex);
			}
		}
	}

	private OrgUnitDTO loadBootstrapOrgUnit() {
		final ObjectMapper mapper = new ObjectMapper();
		try (final InputStream resource = getResource(KLE_FILE)) {
			return mapper.readValue(getResource("bootstrap-organization.json"), OrgUnitDTO.class);
		} catch (IOException e) {
			log.error("Couldn't deserialize bootstrap org", e);
		}
		return null;
	}

	private void buildAllRules() {
		distService.buildRulesForMunicipality(miracle.getId());
//		distService.buildRulesForMunicipality(syddjurs.getId());
	}

	private void buildDistributionRulesForMunicipality(Municipality municipality,
																										 OrgUnit org1,
																										 OrgUnit org2) {
		log.info("building light version of rules");
		if (distService.getDistributionsAll(municipality.getId()).size() < 1) {
			createRules(
					// === Fully unassigned group
					DistributionRule.builder()
							.responsibleOrg(null)
							.kle(kleService.fetchMainGroup("00", municipality.getId()).get())
							.municipality(municipality)
							.children(
									DistributionRule.builder()
											.responsibleOrg(null)
											.kle(kleService.fetchMainGroup("00.01", municipality.getId()).get())
											.municipality(municipality)
											.children(
													DistributionRule.builder()
															.responsibleOrg(null)
															.kle(kleService.fetchMainGroup("00.01.00", municipality.getId()).get())
															.municipality(municipality)
															.build()
											)
											.build()
							)
							.build(),

					// === Group with assigned toplevel
					DistributionRule.builder()
							.responsibleOrg(org1)
							.kle(kleService.fetchMainGroup("13", municipality.getId()).get())
							.municipality(municipality)
							.children(
									DistributionRule.builder()
											.responsibleOrg(null)
											.kle(kleService.fetchMainGroup("13.00", municipality.getId()).get())
											.municipality(municipality)
											.children(
													DistributionRule.builder()
															.responsibleOrg(null)
															.kle(kleService.fetchMainGroup("13.00.00", municipality.getId()).get())
															.municipality(municipality)
															.build()
											)
											.build()
							)
							.build(),

					// Group with two assigned levels
					DistributionRule.builder()
							.responsibleOrg(org2)
							.kle(kleService.fetchMainGroup("14", municipality.getId()).get())
							.municipality(municipality)
							.children(
									DistributionRule.builder()
											.responsibleOrg(org2)
											.kle(kleService.fetchMainGroup("14.00", municipality.getId()).get())
											.municipality(municipality)
											.children(
													DistributionRule.builder()
															.responsibleOrg(null)
															.kle(kleService.fetchMainGroup("14.00.01", municipality.getId()).get())
															.municipality(municipality)
															.build()
											)
											.build()
							)
							.build()
			);
			log.info("rules built.");
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
