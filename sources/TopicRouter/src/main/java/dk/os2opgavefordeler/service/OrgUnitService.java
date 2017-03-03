package dk.os2opgavefordeler.service;

import dk.os2opgavefordeler.model.Employment;
import dk.os2opgavefordeler.model.Municipality;
import dk.os2opgavefordeler.model.OrgUnit;
import dk.os2opgavefordeler.model.presentation.OrgUnitPO;

import java.util.List;
import java.util.Optional;

public interface OrgUnitService {

	/*
	 * Persist an OrgUnit.
	 * @param orgUnit
	 * @return the input orgUnit, for possible ease of method chaining.
	 */
//	OrgUnit saveOrgUnit(OrgUnit orgUnit);

	/**
	 * Imports an organization tree.
	 * @param orgUnit
	 */
	void importOrganization(OrgUnit orgUnit);

	/**
	 * Looks up an orgUnit by id.
	 * @param id
	 * @return fetched orgUnit, or Optional.empty if not found.
	 */
	Optional<OrgUnit> getOrgUnit(long id);
	Optional<OrgUnit> getOrgUnit(long id, Municipality municipality);

	Optional<OrgUnit> getToplevelOrgUnit(long municipalityId);

	List<OrgUnit> getManagedOrgUnits(long municipalityId, long employmentId);

	List<OrgUnitPO> getManagedOrgUnitsPO(long municipalityId, long employmentId);
	List<OrgUnit> findByName(String name);

	List<OrgUnitPO> getToplevelOrgUnitPO(long municipalityId);
	Optional<OrgUnitPO> getOrgUnitPO(long id);

	Optional<Employment> getEmployment(long id);
	Optional<Employment> getEmploymentByName(long municipalityId, String name);

	Optional<Employment> findResponsibleManager(OrgUnit orgUnit);

	Optional<Employment> getActualManager(Long orgId);

	Optional<OrgUnit> findByBusinessKeyAndMunicipality(String businessKey, Municipality municipality);


}
