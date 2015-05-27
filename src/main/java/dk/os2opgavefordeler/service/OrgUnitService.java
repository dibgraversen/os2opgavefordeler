package dk.os2opgavefordeler.service;

import dk.os2opgavefordeler.model.OrgUnit;

import java.util.Optional;

public interface OrgUnitService {
	/**
	 * Persist an OrgUnit.
	 * @param orgUnit
	 * @return the input orgUnit, for possible ease of method chaining.
	 */
	OrgUnit createOrgUnit(OrgUnit orgUnit);

	/**
	 * Looks up an orgUnit by id.
	 * @param id
	 * @return fetched orgUnit, or Optional.empty if not found.
	 */
	Optional<OrgUnit> getOrgUnit(int id);
}
