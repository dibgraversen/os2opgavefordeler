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
            throw new InvalidMunicipalityException("No municipality with id: " + municipalityId);
        }

        entityManager.getTransaction().begin();
        Query query = entityManager.createQuery("UPDATE OrgUnit ou SET ou.isActive=false WHERE ou.municipality.id = :municipalityId");
        query.setParameter("municipalityId", municipality.getId());
        query.executeUpdate();
        entityManager.getTransaction().commit();

        OrgUnit orgUnit = importOrgUnit(municipality, null, orgUnitDTO);

        logger.info("Deleting employees");
        entityManager.getTransaction().begin();
        List resultList = entityManager.createQuery("SELECT e FROM Employment e WHERE e.isActive = false").getResultList();
        logger.info("Employees to delete {}", resultList);
        entityManager.getTransaction().commit();
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
       /* entityManager.getTransaction().begin();
        entityManager.createNativeQuery(
                "DELETE FROM role WHERE owner_id IN (SELECT id FROM tr_user WHERE email NOT IN (SELECT email FROM employment));" +
                        "DELETE FROM tr_user WHERE email NOT IN (SELECT email FROM employment)" +
                        "").executeUpdate();
        entityManager.getTransaction().commit();*/

        return orgUnit;
    }

    private OrgUnit importOrgUnit(Municipality municipality, OrgUnit parent, OrgUnitDTO orgUnitDTO) {
        logger.info("Importing OrgUnit: {}, Business key: {}", orgUnitDTO.name, orgUnitDTO.businessKey);
        OrgUnit orgUnit = orgUnitRepository.findByBusinessKeyAndMunicipalityId(orgUnitDTO.businessKey, municipality.getId());
        if (orgUnit == null) {
            logger.info("OrgUnit did not exist, creating new.");
            orgUnit = new OrgUnit();
        } else {
            logger.info("OrgUnit existsed, updating.");
        }

        if (parent != null) {
            orgUnit.setParent(parent);
        }
        orgUnit.setBusinessKey(orgUnitDTO.businessKey);
        orgUnit.setMunicipality(municipality);
        orgUnit.setIsActive(true);
        orgUnit.setName(orgUnitDTO.name);
        orgUnitRepository.saveAndFlushAndRefresh(orgUnit);
        ImmutableList<Employment> employees = orgUnit.getEmployees();
        for (Employment e : employees) {
            e.setIsActive(false);
            employmentRepository.saveAndFlushAndRefresh(e);
        }
        if (orgUnitDTO.manager != null) {
            orgUnit.setManager(createEmployment(orgUnit, orgUnitDTO.manager));
        } else {
            orgUnit.setManager(null);
        }

        orgUnit.setEmployees(importEmployees(orgUnit, orgUnitDTO));

        orgUnitRepository.saveAndFlushAndRefresh(orgUnit);

        for (OrgUnitDTO o : orgUnitDTO.children) {
            importOrgUnit(municipality, orgUnit, o);
        }

        logger.info("Imported OrgUnit: {}", orgUnit);

        return orgUnit;

    }

    private Employment createEmployment(OrgUnit orgUnit, EmployeeDTO e) {
        Employment employment;
        try {
            employment = employmentRepository.findByEmailAndMunicipality(e.email, orgUnit.getMunicipality().get());
        } catch (NoResultException e1) {
            employment = new Employment();
        }
        employment.setMunicipality(orgUnit.getMunicipality().get());
        employment.setEmail(e.email);
        employment.setEmployedIn(orgUnit);
        employment.setEsdhId(e.esdhId);
        employment.setInitials(e.initials);
        employment.setIsActive(true);
        employment.setJobTitle(e.jobTitle);
        employment.setName(e.name);
        employment.setPhone(e.phone);
        employmentRepository.save(employment);
        return employment;
    }

    private List<Employment> importEmployees(OrgUnit orgUnit, OrgUnitDTO orgUnitDTO) {
        logger.info("Importing employees {}", orgUnitDTO.employees);

        if (orgUnitDTO.employees.isEmpty()) {
            logger.info("No employees found!");
            return Lists.newArrayList();
        }

        List<Employment> employments = new ArrayList<>();

        for (EmployeeDTO e : orgUnitDTO.employees) {
            Employment employment = createEmployment(orgUnit, e);
            employments.add(employment);
        }

        return employments;
    }

    public class InvalidMunicipalityException extends Exception {
        public InvalidMunicipalityException(String message) {
            super(message);
        }

        public InvalidMunicipalityException(String message, Throwable throwable) {
            super(message, throwable);
        }


        public InvalidMunicipalityException(Throwable throwable) {
            super(throwable);
        }
    }

}
