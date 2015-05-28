package dk.os2opgavefordeler.service;

import dk.os2opgavefordeler.model.OrgUnit;
import dk.os2opgavefordeler.model.OrgUnit_;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
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

	@Override
	public Optional<OrgUnit> getToplevelOrgUnit() {
		final List<OrgUnit> results = persistence.criteriaFind(OrgUnit.class,
			(cb, cq, ou) -> cq.where( cb.isNull(ou.get(OrgUnit_.parent)))
		);

		return results.isEmpty() ?
			Optional.empty() :
			Optional.of(results.get(0));
	}

	@Override
	public Optional<OrgUnit> getToplevelOrgUnitPO() {
		final Optional<OrgUnit> ou = getToplevelOrgUnit();

		//TODO: introduce PO class, transform

		if(ou.isPresent()) {
			touchChildren(ou.get().getChildren());
		}

		return ou;
	}


	private List<OrgUnit> touchChildren(List<OrgUnit> ou) {
		ou.forEach(k -> touchChildren(k.getChildren()));
		return ou;
	}
}
