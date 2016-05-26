package dk.os2opgavefordeler.orgunit;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import dk.os2opgavefordeler.employment.EmploymentRepository;
import dk.os2opgavefordeler.employment.MunicipalityRepository;
import dk.os2opgavefordeler.employment.OrgUnitRepository;
import dk.os2opgavefordeler.model.Employment;
import dk.os2opgavefordeler.model.Municipality;
import dk.os2opgavefordeler.model.OrgUnit;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
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
    private EntityManager entityManager;

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

	    logger.info("Deleting employees: {}", getInactiveEmployees());

	    inactivateOrgUnitsForMunicipality(municipalityId); // inactivate all organisation units in preparation of import
        OrgUnit orgUnit = importOrgUnit(municipality, null, orgUnitDTO); // import root organisation unit
	    cleanup(); // delete inactive records

        return orgUnit;
    }

	/**
	 * Imports the given organisational unit for the specified municipality
	 *
	 * @param municipality municipality to import the organisational unit for
	 * @param parent parent of the organisational unit (null if it's the root org. unit)
	 * @param orgUnitDTO DTO object to get values from
	 * @return the resulting organisational unit
	 */
    private OrgUnit importOrgUnit(Municipality municipality, OrgUnit parent, OrgUnitDTO orgUnitDTO) {
        logger.info("Importing OrgUnit: {}, Business key: {}", orgUnitDTO.name, orgUnitDTO.businessKey);

	    // retrieve an existing organisational unit or create a new one, if needed
	    OrgUnit orgUnit = orgUnitRepository.findByBusinessKeyAndMunicipalityId(orgUnitDTO.businessKey, municipality.getId());

	    if (orgUnit == null) {
            logger.info("OrgUnit did not exist, creating new.");
            orgUnit = new OrgUnit();
        }
        else {
            logger.info("OrgUnit already exists, updating.");
        }

        if (parent != null) {
            orgUnit.setParent(parent);
        }

        orgUnit.setBusinessKey(orgUnitDTO.businessKey);
        orgUnit.setMunicipality(municipality);
        orgUnit.setIsActive(true);
        orgUnit.setName(orgUnitDTO.name);
        orgUnit.setEsdhId(orgUnitDTO.esdhId);
        orgUnit.setEsdhLabel(orgUnitDTO.esdhLabel);

        orgUnitRepository.saveAndFlushAndRefresh(orgUnit);

	    inactivateEmploymentsForOrgUnit(orgUnit); // mark all employments for the organisation unit as inactive

	    // create an employment for the manager of the organisational unit, if needed
        if (orgUnitDTO.manager != null) {
            orgUnit.setManager(createEmployment(orgUnit, orgUnitDTO.manager));
        }
        else {
            orgUnit.setManager(null);
        }

        orgUnit.setEmployees(importEmployments(orgUnit, orgUnitDTO));

        orgUnitRepository.saveAndFlushAndRefresh(orgUnit);

        for (OrgUnitDTO o : orgUnitDTO.children) {
            importOrgUnit(municipality, orgUnit, o);
        }

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

		for (Employment employment: employments) {
			employment.setIsActive(false);
			employmentRepository.saveAndFlushAndRefresh(employment);
		}
	}

	/**
	 * Creates an employment for the specified organisational unit
	 *
	 * @param orgUnit organisational unit
	 * @param e DTO object with values to use for the new employment record
	 * @return the created Employment
	 */
    private Employment createEmployment(OrgUnit orgUnit, EmployeeDTO e) {
	    if (orgUnit.getMunicipality().isPresent()) { // municipality correctly defined
		    Municipality municipality = orgUnit.getMunicipality().get();

		    // get existing employment or create a new one, if needed
		    Employment employment;

		    try {
			    employment = employmentRepository.findByEmailAndMunicipality(e.email, municipality);
		    }
		    catch (NoResultException e1) {
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
	    }
	    else { // no municipality defined
		    logger.error("Municipality not defined for OrgUnit '{}'. Unable to create employment.", orgUnit.getName());
		    return null;
	    }
    }

	/**
	 * Imports employments for the given organisational unit
	 *
	 * @param orgUnit organisational unit to create employments for
	 * @param orgUnitDTO DTO object for the organisational unit to get values from
	 * @return list of employments for the organisational unit
	 */
    private List<Employment> importEmployments(OrgUnit orgUnit, OrgUnitDTO orgUnitDTO) {
        logger.info("Importing employments {}", orgUnitDTO.employees);

        if (orgUnitDTO.employees.isEmpty()) {
            logger.info("No employments found!");
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
		entityManager.getTransaction().begin();
		Query query = entityManager.createQuery("UPDATE OrgUnit ou SET ou.isActive = false WHERE ou.municipality.id = :municipalityId");
		query.setParameter("municipalityId", municipalityId);
		query.executeUpdate();
		entityManager.getTransaction().commit();
	}

	/**
	 * Returns all inactive employees
	 *
	 * @return the list of employees
	 */
	private List getInactiveEmployees() {
		entityManager.getTransaction().begin();
		List resultList = entityManager.createQuery("SELECT e FROM Employment e WHERE e.isActive = false").getResultList();
		entityManager.getTransaction().commit();

		return resultList;
	}

	/**
	 * Deletes inactive records
	 */
	private void cleanup() {
		entityManager.getTransaction().begin();

		entityManager.createNativeQuery(
				" DELETE FROM distributionrule WHERE assignedemp IN (SELECT id FROM employment WHERE isactive = FALSE);" +
						" DELETE FROM distributionrule_distributionrulefilter WHERE filters_id IN (SELECT id FROM distributionrule WHERE assignedemp IN (SELECT id FROM employment WHERE isactive = FALSE));" +
						" DELETE FROM distributionrulefilter drf WHERE drf.distributionrule_id IN (SELECT id FROM distributionrule WHERE assignedemp IN (SELECT id FROM employment WHERE isactive = FALSE));" +

						" DELETE FROM distributionrule_distributionrulefilter WHERE filters_id IN (SELECT id FROM distributionrulefilter drf WHERE drf.assignedemp IN (SELECT id FROM employment WHERE isactive = FALSE));" +
						" DELETE FROM distributionrulefilter drf WHERE drf.assignedemp IN (SELECT id FROM employment WHERE isactive = FALSE);" +

						" DELETE FROM role WHERE employment_id IN (SELECT id FROM employment WHERE isactive = FALSE); " +
						" UPDATE orgunit SET manager_id = NULL WHERE manager_id IN (SELECT id FROM employment WHERE isactive = FALSE);" +

						" UPDATE orgunit SET manager_id = NULL WHERE manager_id IN (SELECT id FROM employment WHERE employedin_id IN (SELECT id FROM orgunit WHERE isactive = FALSE));" +

						" DELETE FROM role WHERE employment_id IN (SELECT id FROM employment WHERE employedin_id IN (SELECT id FROM orgunit WHERE isactive = FALSE));" +

						" DELETE FROM distributionrule_distributionrulefilter WHERE distributionrule_id IN (SELECT id FROM distributionrule WHERE assignedemp IN (SELECT id FROM employment WHERE employedin_id IN (SELECT id FROM orgunit WHERE isactive = FALSE)));" +
						" DELETE FROM distributionrule_distributionrulefilter WHERE distributionrule_id IN (SELECT id FROM distributionrule WHERE parent_id IN (SELECT id FROM distributionrule WHERE assignedemp IN (SELECT id FROM employment WHERE employedin_id IN (SELECT id FROM orgunit WHERE isactive = FALSE))));" +
						" DELETE FROM distributionrule WHERE parent_id IN (SELECT id FROM distributionrule WHERE assignedemp IN (SELECT id FROM employment WHERE employedin_id IN (SELECT id FROM orgunit WHERE isactive = FALSE)));" +
						" DELETE FROM distributionrule WHERE assignedemp IN (SELECT id FROM employment WHERE employedin_id IN (SELECT id FROM orgunit WHERE isactive = FALSE));" +

						" DELETE FROM distributionrule_distributionrulefilter WHERE filters_id IN (SELECT id FROM distributionrulefilter WHERE assignedemp IN (SELECT id FROM employment WHERE employedin_id IN (SELECT id FROM orgunit WHERE isactive = FALSE)));" +
						" DELETE FROM distributionrulefilter WHERE assignedemp IN (SELECT id FROM employment WHERE employedin_id IN (SELECT id FROM orgunit WHERE isactive = FALSE));" +

						" DELETE FROM employment WHERE employedin_id IN (SELECT id FROM orgunit WHERE isactive = FALSE);" +
						" DELETE FROM employment WHERE isactive = FALSE;"
		).executeUpdate();

		entityManager.getTransaction().commit();
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