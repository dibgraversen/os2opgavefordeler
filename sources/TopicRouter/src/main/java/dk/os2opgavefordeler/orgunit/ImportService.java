package dk.os2opgavefordeler.orgunit;

import com.google.common.collect.Lists;
import dk.os2opgavefordeler.employment.MunicipalityRepository;
import dk.os2opgavefordeler.employment.OrgUnitRepository;
import dk.os2opgavefordeler.model.Employment;
import dk.os2opgavefordeler.model.Municipality;
import dk.os2opgavefordeler.model.OrgUnit;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
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

    /**
     * Imports an organization into a municipality.
     *
     * @param municipalityId The municipality to import for
     * @param orgUnitDTO     A dto to describe the new OrgUnit structure
     */
    public void importOrganization(long municipalityId, OrgUnitDTO orgUnitDTO) throws InvalidMunicipalityException {
        Municipality municipality = municipalityRepository.findBy(municipalityId);
        if (municipality == null) {
            throw new InvalidMunicipalityException("No municipality with id: " + municipalityId);
        }

        OrgUnit orgUnit = importOrgUnit(municipality, orgUnitDTO);
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
        if (orgUnitDTO.manager != null) {
            orgUnit.setManager(createEmployment(orgUnit, orgUnitDTO.manager));
        }
        orgUnit.setBusinessKey(orgUnitDTO.businessKey);
        orgUnit.setMunicipality(municipality);
        orgUnit.setEmployees(importEmployees(orgUnit, orgUnitDTO));

        orgUnitRepository.save(orgUnit);

        for (OrgUnitDTO o : orgUnitDTO.children) {
            importOrgUnit(municipality, orgUnitDTO);
        }

        logger.info("Imported OrgUnit: {}", orgUnit);

        return orgUnit;

    }

    private Employment createEmployment(OrgUnit orgUnit, EmployeeDTO e) {
        Employment employment = new Employment();
        employment.setMunicipality(orgUnit.getMunicipality().get());
        employment.setEmail(e.email);
        employment.setEmployedIn(orgUnit);
        employment.setEsdhId(e.esdhId);
        employment.setInitials(e.initials);
        employment.setIsActive(true);
        employment.setJobTitle(e.jobTitle);
        employment.setName(e.name);
        employment.setPhone(e.phone);
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
            employments.add(createEmployment(orgUnit, e));
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
