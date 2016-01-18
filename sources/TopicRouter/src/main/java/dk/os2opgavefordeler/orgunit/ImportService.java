package dk.os2opgavefordeler.orgunit;

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

        OrgUnit orgUnit = importOrgUnit(municipality, orgUnitDTO);
        return orgUnit;
    }

    private OrgUnit importOrgUnit(Municipality municipality, OrgUnitDTO orgUnitDTO) {
        logger.info("Importing OrgUnit: {}, Business key: {}", orgUnitDTO.name, orgUnitDTO.businessKey);
        OrgUnit orgUnit = orgUnitRepository.findByBusinessKeyAndMunicipalityId(orgUnitDTO.businessKey, municipality.getId());
        if (orgUnit == null) {
            logger.info("OrgUnit did not exist, creating new.");
            orgUnit = new OrgUnit();
        } else {
            logger.info("OrgUnit existsed, updating.");
        }
        orgUnit.setBusinessKey(orgUnitDTO.businessKey);
        orgUnit.setMunicipality(municipality);
        orgUnit.setIsActive(true);
        orgUnitRepository.saveAndFlushAndRefresh(orgUnit);
        if (orgUnitDTO.manager != null) {
            orgUnit.setManager(createEmployment(orgUnit, orgUnitDTO.manager));
        }
        orgUnit.setEmployees(importEmployees(orgUnit, orgUnitDTO));

        orgUnitRepository.saveAndFlushAndRefresh(orgUnit);

        for (OrgUnitDTO o : orgUnitDTO.children) {
            importOrgUnit(municipality, orgUnitDTO);
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
