package dk.os2opgavefordeler.service;

import dk.os2opgavefordeler.model.OrgUnit;
import dk.os2opgavefordeler.model.OrgUnit_;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

public class OrgUnitServiceImpl implements OrgUnitService {
	@Inject
	PersistenceService persistence;

	@Override
	public OrgUnit createOrgUnit(OrgUnit orgUnit) {
		persistence.persist(orgUnit);
		return orgUnit;
	}

	@Override
	public Optional<OrgUnit> getOrgUnit(int id) {
		final List<OrgUnit> results = persistence.criteriaFind(OrgUnit.class,
			(cb, cq, ou) -> cq.where(cb.equal(ou.get(OrgUnit_.id), id)
			)
		);

		return results.isEmpty() ?
			Optional.empty() :
			Optional.of(results.get(0));
	}
}
