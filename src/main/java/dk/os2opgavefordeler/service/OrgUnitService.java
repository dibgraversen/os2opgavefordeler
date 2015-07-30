package dk.os2opgavefordeler.service;

import dk.os2opgavefordeler.model.Employment;
import dk.os2opgavefordeler.model.OrgUnit;
import dk.os2opgavefordeler.model.presentation.OrgUnitPO;

import java.util.List;
import java.util.Optional;

public interface OrgUnitService {

	/**
	 * Persist an OrgUnit.
	 * @param orgUnit
	 * @return the input orgUnit, for possible ease of method chaining.
	 */
	OrgUnit saveOrgUnit(OrgUnit orgUnit);

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

	Optional<OrgUnit> getToplevelOrgUnit(long municipalityId);
	List<OrgUnit> findByName(String name);

	List<OrgUnitPO> getToplevelOrgUnitPO(long municipalityId);
	Optional<OrgUnitPO> getOrgUnitPO(long id);

	Optional<Employment> getEmployment(long id);
	Optional<Employment> getEmploymentByName(long municipalityId, String name);
}
