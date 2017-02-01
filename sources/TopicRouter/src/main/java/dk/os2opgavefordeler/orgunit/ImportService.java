package dk.os2opgavefordeler.orgunit;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import dk.os2opgavefordeler.distribution.DistributionRuleFilterRepository;
import dk.os2opgavefordeler.distribution.DistributionRuleRepository;
import dk.os2opgavefordeler.model.Role;
import dk.os2opgavefordeler.repository.EmploymentRepository;
import dk.os2opgavefordeler.repository.MunicipalityRepository;
import dk.os2opgavefordeler.repository.OrgUnitRepository;
import dk.os2opgavefordeler.model.DistributionRule;
import dk.os2opgavefordeler.model.DistributionRuleFilter;
import dk.os2opgavefordeler.model.Employment;
import dk.os2opgavefordeler.model.Municipality;
import dk.os2opgavefordeler.model.OrgUnit;
import dk.os2opgavefordeler.repository.RoleRepository;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.slf4j.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NonUniqueResultException;
import java.util.ArrayList;
import java.util.List;

@RequestScoped
@Transactional
public class ImportService {

	@Inject
	private Logger logger;

	@Inject
	private MunicipalityRepository municipalityRepo;

	@Inject
	private OrgUnitRepository orgRepo;

	@Inject
	private EmploymentRepository employmentRepo;

	@Inject
	DistributionRuleFilterRepository filterRepo;

	@Inject
	DistributionRuleRepository ruleRepo;

	@Inject
	RoleRepository roleRepo;

//	@Inject
//	private DistributionRuleController distributionRuleController;

	@Inject
	private EntityManager entityManager;

	@Inject
	private OrgUpdateManager updateManager;

	/**
	 * Imports an organization into a municipality.
	 *
	 * @param municipalityId The municipality to import for
	 * @param orgUnitDTO     A dto to describe the new OrgUnit structure
	 */
	public OrgUnit importOrganization(long municipalityId, OrgUnitDTO orgUnitDTO) throws InvalidMunicipalityException {
		Municipality municipality = municipalityRepo.findBy(municipalityId);
		if (municipality == null) {
			throw new InvalidMunicipalityException("No municipality with ID: " + municipalityId);
		}
		if (!updateManager.importJobAllowedFor(municipalityId)) {
			logger.warn("Job already running for {}", municipalityId);
			return null;
		}
		OrgUnit orgUnit = new OrgUnit();
		try {
			orgRepo.deactivateForMunicipality(municipalityId); // inactivate all organisation units in preparation of import
			orgUnit = importOrgUnit(municipality, null, orgUnitDTO); // import root organisation unit
			cleanup(municipalityId); // delete inactive records
		} finally {
			updateManager.endJob(municipalityId);
		}
		return orgUnit;
	}

	/**
	 * Imports the given organisational unit for the specified municipality
	 *
	 * @param municipality municipality to import the organisational unit for
	 * @param parent       parent of the organisational unit (null if it's the root org. unit)
	 * @param orgUnitDTO   DTO object to get values from
	 * @return the resulting organisational unit
	 */
	private OrgUnit importOrgUnit(Municipality municipality, OrgUnit parent, OrgUnitDTO orgUnitDTO) {
		logger.info("Importing OrgUnit: {}, Business key: {}", orgUnitDTO.name, orgUnitDTO.businessKey);

		// retrieve an existing organisational unit or create a new one, if needed
		OrgUnit orgUnit = orgRepo.findByBusinessKeyAndMunicipalityId(orgUnitDTO.businessKey, municipality.getId());

		if (orgUnit == null) {
			orgUnit = new OrgUnit();
			orgUnit.setBusinessKey(orgUnitDTO.businessKey);
			orgUnit.setMunicipality(municipality);
		} else {
//			logger.info("OrgUnit already exists, updating.");
		}
		if (parent != null) {
			orgUnit.setParent(parent);
		}
		orgUnit.setIsActive(true);
		orgUnit.setName(orgUnitDTO.name);
		orgUnit.setEsdhId(orgUnitDTO.esdhId);
		orgUnit.setEsdhLabel(orgUnitDTO.esdhLabel);
		orgUnit.setEmail(orgUnitDTO.email);
		orgUnit.setPhone(orgUnitDTO.phone);

		orgRepo.saveAndFlushAndRefresh(orgUnit);

		inactivateEmploymentsForOrgUnit(orgUnit); // mark all employments for the organisation unit as inactive

		// create an repository for the manager of the organisational unit, if needed
		if (orgUnitDTO.manager != null) {
			orgUnit.setManager(createEmployment(orgUnit, orgUnitDTO.manager));
		} else {
			orgUnit.setManager(null);
		}

		orgUnit.setEmployees(importEmployments(orgUnit, orgUnitDTO));

		orgRepo.saveAndFlushAndRefresh(orgUnit);

		for (OrgUnitDTO o : orgUnitDTO.children) {
			importOrgUnit(municipality, orgUnit, o);
		}
		orgRepo.saveAndFlushAndRefresh(orgUnit);
		logger.info("Imported OrgUnit: {}", orgUnit);
		return orgUnit;
	}

	/**
	 * Inactivates all employments for a given organisational unit
	 *
	 * @param orgUnit the organisational unit to disable employments for
	 */
	private void inactivateEmploymentsForOrgUnit(OrgUnit orgUnit) {
		ImmutableList<Employment> employments = orgUnit.getEmployees();

		for (Employment employment : employments) {
			employment.setIsActive(false);
			employmentRepo.saveAndFlushAndRefresh(employment);
		}
	}

	/**
	 * Creates an repository for the specified organisational unit
	 *
	 * @param orgUnit organisational unit
	 * @param e       DTO object with values to use for the new repository record
	 * @return the created Employment
	 */
	private Employment createEmployment(OrgUnit orgUnit, EmployeeDTO e) {
//		logger.info("Creating repository, name: {}", e.name);
		if (orgUnit.getMunicipality().isPresent()) { // municipality correctly defined
			Municipality municipality = orgUnit.getMunicipality().get();

			// get existing repository or create a new one, if needed
			Employment employment;
			try {
				employment = employmentRepo.findByBusinessKeyAndMunicipalityId(e.businessKey, municipality.getId());
			} catch (NonUniqueResultException nre){
				logger.error("Found multiple results for businessKey: {} and municipality: {}", e.businessKey, municipality.getId());
				employment = new Employment();
			}
			if(employment == null) employment = new Employment();

			// set values for repository
			employment.setMunicipality(municipality);
			employment.setBusinessKey(e.businessKey);
			employment.setEmail(e.email);
			employment.setEmployedIn(orgUnit);
			employment.setEsdhId(e.esdhId);
			employment.setInitials(e.initials);
			employment.setIsActive(true);
			employment.setJobTitle(e.jobTitle);
			employment.setName(e.name);
			employment.setPhone(e.phone);

			// save employment
			employmentRepo.save(employment);

			return employment;
		} else { // no municipality defined
			logger.warn("Municipality not defined for OrgUnit '{}'. Unable to create employment.", orgUnit.getName());
			return null;
		}
	}

	/**
	 * Imports employments for the given organisational unit
	 *
	 * @param orgUnit    organisational unit to create employments for
	 * @param orgUnitDTO DTO object for the organisational unit to get values from
	 * @return list of employments for the organisational unit
	 */
	private List<Employment> importEmployments(OrgUnit orgUnit, OrgUnitDTO orgUnitDTO) {
//		logger.info("Importing employments {}", orgUnitDTO.employees);
		if (orgUnitDTO.employees.isEmpty()) {
			return Lists.newArrayList();
		}
		List<Employment> employments = new ArrayList<>();
		for (EmployeeDTO e : orgUnitDTO.employees) {
			employments.add(createEmployment(orgUnit, e));
		}
		return employments;
	}

	/**
	 * Deletes inactive records
	 */
	@SuppressWarnings("unchecked")
	private void cleanup(long municipalityId) {
		// setup
		List<Employment> empsForDeletion = employmentRepo.findEmploymentsToDelete(municipalityId);
		List<OrgUnit> orgsForDeletion = orgRepo.findOrgsToDelete(municipalityId);
//		orgsForDeletion.addAll(findChildren(orgsForDeletion));

		// find rules for clearance with reason org.
		List<DistributionRule> rulesForClearing = new ArrayList();
		if (!orgsForDeletion.isEmpty()) {
			rulesForClearing = ruleRepo.findRulesByAssignedOrg(orgsForDeletion);
		}

		handleFilters(rulesForClearing, orgsForDeletion, empsForDeletion);
		clearEntitiesForDeletion(orgsForDeletion, empsForDeletion);

		// delete employments
		if (!empsForDeletion.isEmpty()) {
			for (Employment employment : empsForDeletion) {
				if (employment != null) {
					clearRoles(employment);
					employmentRepo.remove(employment);
				}
			}
			employmentRepo.flush();
		}
		for (OrgUnit orgUnit : orgsForDeletion) {
			orgRepo.remove(orgUnit);
		}
		orgRepo.flush();
	}

	private void clearRoles(Employment employment){
		List<Role> roles = roleRepo.findByEmployment(employment);
		for (Role role : roles) {
			role.setEmployment(null);
			if(safeToDelete(role)){
				roleRepo.saveAndFlush(role);
				roleRepo.removeAndFlush(role);
			}
		}
	}

	private boolean safeToDelete(Role role){
		return !role.isAdmin() || !role.isMunicipalityAdmin();
	}

	/**
	 * This method makes sure that filters are cleaned and deleted.
	 */
	private void handleFilters(List<DistributionRule> rulesForClearing, List<OrgUnit> orgsForDeletion, List<Employment> empsForDeletion) {
		List<DistributionRuleFilter> filtersToBeDeleted = new ArrayList<>();

		if (!rulesForClearing.isEmpty()) {
			filtersToBeDeleted.addAll(filterRepo.findForRules(rulesForClearing));
		}
		if (!orgsForDeletion.isEmpty()) {
			filtersToBeDeleted.addAll(filterRepo.findByAssignedOrg(orgsForDeletion));
		}
		if (!empsForDeletion.isEmpty()) {
			filterRepo.unsetEmployments(empsForDeletion);
		}
		filtersToBeDeleted.addAll(filterRepo.findAbandoned());

		// delete filter rules for rules that are to be cleared.
		for (DistributionRuleFilter filter : filtersToBeDeleted) {
			if (filter != null) {
				filter.setAssignedEmployee(null);
				filter.setAssignedOrg(null);
				filterRepo.saveAndFlush(filter);
				filterRepo.remove(filter);
			}
		}
		filterRepo.flush();
	}

	/**
	 * This method sets reference blank where entity will be deleted.
	 */
	private void clearEntitiesForDeletion(List<OrgUnit> orgsForDeletion, List<Employment> empsForDeletion) {
		if (!empsForDeletion.isEmpty()) {
			ruleRepo.clearEmployments(empsForDeletion);
		}
		if (!orgsForDeletion.isEmpty()) {
			ruleRepo.clearOrgs(orgsForDeletion);
			orgRepo.clearManager(orgsForDeletion);
			orgRepo.clearParents(orgsForDeletion);
			employmentRepo.clearEmployedIn(orgsForDeletion);
		}
	}

	/**
	 * Exception used when in invalid municipality is used
	 */
	public class InvalidMunicipalityException extends Exception {
		public InvalidMunicipalityException(String message) {
			super(message);
		}
	}

}