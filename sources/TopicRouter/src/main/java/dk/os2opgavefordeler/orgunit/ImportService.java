package dk.os2opgavefordeler.orgunit;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import dk.os2opgavefordeler.distribution.DistributionRuleController;
import dk.os2opgavefordeler.employment.EmploymentRepository;
import dk.os2opgavefordeler.employment.MunicipalityRepository;
import dk.os2opgavefordeler.employment.OrgUnitRepository;
import dk.os2opgavefordeler.model.DistributionRuleFilter;
import dk.os2opgavefordeler.model.Employment;
import dk.os2opgavefordeler.model.Municipality;
import dk.os2opgavefordeler.model.OrgUnit;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.slf4j.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

@RequestScoped
@Transactional
public class ImportService {

	@Inject
	private Logger logger;

	@Inject
	private MunicipalityRepository municipalityRepository;

	@Inject
	private OrgUnitRepository orgUnitRepository;

	@Inject
	private EmploymentRepository employmentRepository;

	@Inject
	private DistributionRuleController distributionRuleController;

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
		Municipality municipality = municipalityRepository.findBy(municipalityId);
		if (municipality == null) {
			throw new InvalidMunicipalityException("No municipality with ID: " + municipalityId);
		}
		if(!updateManager.importJobAllowedFor(municipalityId)){
			logger.warn("Job already running for {}", municipalityId);
			return null;
		}
		OrgUnit orgUnit = new OrgUnit();
		try {
			inactivateOrgUnitsForMunicipality(municipalityId); // inactivate all organisation units in preparation of import
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
		OrgUnit orgUnit = orgUnitRepository.findByBusinessKeyAndMunicipalityId(orgUnitDTO.businessKey, municipality.getId());

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

		orgUnitRepository.saveAndFlushAndRefresh(orgUnit);

		inactivateEmploymentsForOrgUnit(orgUnit); // mark all employments for the organisation unit as inactive

		// create an employment for the manager of the organisational unit, if needed
		if (orgUnitDTO.manager != null) {
			orgUnit.setManager(createEmployment(orgUnit, orgUnitDTO.manager));
		} else {
			orgUnit.setManager(null);
		}

		orgUnit.setEmployees(importEmployments(orgUnit, orgUnitDTO));

		orgUnitRepository.saveAndFlushAndRefresh(orgUnit);

		for (OrgUnitDTO o : orgUnitDTO.children) {
			importOrgUnit(municipality, orgUnit, o);
		}
		orgUnitRepository.saveAndFlushAndRefresh(orgUnit);
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
			employmentRepository.saveAndFlushAndRefresh(employment);
		}
	}

	/**
	 * Creates an employment for the specified organisational unit
	 *
	 * @param orgUnit organisational unit
	 * @param e       DTO object with values to use for the new employment record
	 * @return the created Employment
	 */
	private Employment createEmployment(OrgUnit orgUnit, EmployeeDTO e) {
//		logger.info("Creating employment, name: {}", e.name);
		if (orgUnit.getMunicipality().isPresent()) { // municipality correctly defined
			Municipality municipality = orgUnit.getMunicipality().get();

			// get existing employment or create a new one, if needed
			Employment employment;

			try {
				// TODO find by businessKey. That's what it's there for.
				employment = employmentRepository.findByEmailAndMunicipality(e.email, municipality);
			} catch (NoResultException e1) {
				employment = new Employment();
			}

			// set values for employment
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
			employmentRepository.save(employment);

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
	 * Inactivates all organisational units for a given municipality
	 *
	 * @param municipalityId ID for the municipality to inactivate organisational units for
	 */
	private void inactivateOrgUnitsForMunicipality(long municipalityId) {
		Query query = entityManager.createQuery("update OrgUnit ou set ou.isActive = false where ou.municipality.id = :municipalityId");
		query.setParameter("municipalityId", municipalityId);
		query.executeUpdate();
	}

	/**
	 * Returns all inactive employees
	 *
	 * @return the list of employees
	 */
	private List getInactiveEmployees() {
		List resultList = entityManager.createQuery("select e from Employment e where e.isActive = false").getResultList();
		return resultList;
	}

	/**
	 * Deletes inactive records
	 */
	private void cleanup(long municipalityId) {
		// find employments for deletion.
		Query getEmploymentsForDelete = entityManager.createQuery("select emp.id from Employment emp where emp.isActive = false and emp.municipality.id = :municipalityId");
		getEmploymentsForDelete.setParameter("municipalityId", municipalityId);
		List empsForDeletion = getEmploymentsForDelete.getResultList();

		// find orgs for deletion.
		Query getOrgsForDelete = entityManager.createQuery("select org.id from OrgUnit org where org.isActive = false and org.municipality.id = :municipalityId");
		getOrgsForDelete.setParameter("municipalityId", municipalityId);
		List<Long> orgsForDeletion = getOrgsForDelete.getResultList();

		// find rules for clearance with reason org.
		List rulesForClearing = new ArrayList();
		if (!orgsForDeletion.isEmpty()){
			Query getRulesForClearing = entityManager.createQuery("select rule.id from DistributionRule rule where rule.assignedOrg.id in ( :orgsForDeletion ) ");
			getRulesForClearing.setParameter("orgsForDeletion", orgsForDeletion);
			rulesForClearing = getRulesForClearing.getResultList();
		}

		cleanFilters(rulesForClearing, orgsForDeletion, empsForDeletion);
		clearEntitiesForDeletion(orgsForDeletion, empsForDeletion);

		// delete employments
		if(!empsForDeletion.isEmpty()){
			Query deleteRoles = entityManager.createQuery("delete from Role role where role.employment.id in ( :empsForDeletion )");
			deleteRoles.setParameter("empsForDeletion", empsForDeletion).executeUpdate();

			Query deleteEmployments = entityManager.createQuery("delete from Employment emp where emp.id in ( :empsForDeletion )");
			deleteEmployments.setParameter("empsForDeletion", empsForDeletion).executeUpdate();
		}

		if(!orgsForDeletion.isEmpty()){
			// delete org units
			for (Long orgId : orgsForDeletion) {
				OrgUnit orgUnit = entityManager.find(OrgUnit.class, orgId);
				if(orgUnit != null){
					entityManager.remove(orgUnit);
				}
			}
		}
	}

	/**
	 * This method makes sure that filters are cleaned and deleted.
	 */
	@SuppressWarnings("unchecked")
	private void cleanFilters(List rulesForClearing, List orgsForDeletion, List empsForDeletion){
		List<Long> filtersToBeDeleted = new ArrayList<>();

		if(!rulesForClearing.isEmpty()) {
			Query getAssociatedFiltersForDeletion = entityManager.createQuery("select filter.id from DistributionRuleFilter filter where filter.distributionRule.id in ( :rulesForClearing )");
			getAssociatedFiltersForDeletion.setParameter("rulesForClearing", rulesForClearing);
			filtersToBeDeleted.addAll(getAssociatedFiltersForDeletion.getResultList());
		}

		if(!orgsForDeletion.isEmpty()){
			// unset org for filters.
			Query getFiltersWithDeletedOrg = entityManager.createQuery("select filter.id from DistributionRuleFilter filter where filter.assignedOrg.id in ( :orgsForDeletion )");
			filtersToBeDeleted.addAll(getFiltersWithDeletedOrg.setParameter("orgsForDeletion", orgsForDeletion).getResultList());
		}

		if(!empsForDeletion.isEmpty()){
			Query unsetEmpForFilters = entityManager.createQuery("update DistributionRuleFilter filter set filter.assignedEmp = null where filter.assignedEmp.id in ( :empsForDeletion )");
			unsetEmpForFilters.setParameter("empsForDeletion", empsForDeletion).executeUpdate();
		}

		// delete filters with no org and no empl.
		Query findAbandonedFilters = entityManager.createQuery("select filter.id from DistributionRuleFilter filter where filter.assignedEmp is null and filter.assignedOrg is null");
		filtersToBeDeleted.addAll(findAbandonedFilters.getResultList());

		// delete filter rules for rules that are to be cleared.
		for (Long filterId : filtersToBeDeleted) {
			DistributionRuleFilter filter = entityManager.find(DistributionRuleFilter.class, filterId);
			if(filter != null){
				entityManager.remove(filter);
			}
		}
	}

	/**
	 * This method sets reference blank where entity will be deleted.
	 */
	private void clearEntitiesForDeletion(List orgsForDeletion, List empsForDeletion){
		if(!empsForDeletion.isEmpty()){

			// clear assigned emp.
			Query clearAssignedEmp = entityManager.createQuery("update DistributionRule rule set rule.assignedEmp = null where rule.assignedEmp in ( :empsForDeletion )");
			clearAssignedEmp.setParameter("empsForDeletion", empsForDeletion).executeUpdate();

			// clear managers
			Query clearManagers = entityManager.createQuery("update OrgUnit org set org.manager = null where org.manager.id in ( :empsForDeletion )");
			clearManagers.setParameter("empsForDeletion", empsForDeletion).executeUpdate();
		}

		if(!orgsForDeletion.isEmpty()){
			// clear responsible org.
			Query clearResponsible = entityManager.createQuery("update DistributionRule rule set rule.responsibleOrg = null where rule.responsibleOrg.id in ( :orgsForDeletion )");
			clearResponsible.setParameter("orgsForDeletion", orgsForDeletion).executeUpdate();

			// clear assigned org
			Query clearAssignedOrg = entityManager.createQuery("update DistributionRule rule set rule.assignedOrg = null where rule.assignedOrg.id in ( :orgsForDeletion )");
			clearAssignedOrg.setParameter("orgsForDeletion", orgsForDeletion).executeUpdate();

			// rinse org units
			Query rinseOrgUnits = entityManager.createQuery("update OrgUnit org set org.manager = null where org.id in ( :orgsForDeletion )");
			rinseOrgUnits.setParameter("orgsForDeletion", orgsForDeletion).executeUpdate();

			// clear employed in
			Query clearEmployedIn = entityManager.createQuery("update Employment emp set emp.employedIn = null where emp.employedIn.id in ( :orgsForDeletion )");
			clearEmployedIn.setParameter("orgsForDeletion", orgsForDeletion).executeUpdate();
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