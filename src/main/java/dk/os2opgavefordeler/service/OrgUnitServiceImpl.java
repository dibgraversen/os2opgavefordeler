package dk.os2opgavefordeler.service;

import dk.os2opgavefordeler.model.OrgUnit;
import dk.os2opgavefordeler.model.OrgUnit_;
import dk.os2opgavefordeler.model.presentation.OrgUnitPO;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
	public void importOrganization(OrgUnit orgUnit) {
		fixRelations(orgUnit);
		createOrgUnit(orgUnit);
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

		if(results.isEmpty()) {
			return Optional.empty();
		} else {
			touchChildren(results);
			return Optional.of(results.get(0));
		}
	}

	@Override
	public List<OrgUnit> findByName(String name) {
		final List<OrgUnit> results = persistence.criteriaFind(OrgUnit.class,
			(cb, cq, ou) -> cq.where( cb.like(ou.get(OrgUnit_.name), name))
		);

		return results;
	}

//	public List<Employment> getSubordinateManagers(OrgUnit ou) {
//		return ou.flattened().map(OrgUnit::getManager).collect(Collectors.toList());
//	}


	@Override
	public List<OrgUnitPO> getToplevelOrgUnitPO() {
		final Optional<OrgUnit> ou = getToplevelOrgUnit();

		return ou.isPresent() ?
			ou.get().flattened().map(OrgUnitPO::new).collect(Collectors.toList()) :
			Collections.emptyList();
	}

	@Override
	public Optional<OrgUnitPO> getOrgUnitPO(int id) {
		return getOrgUnit(id).map(OrgUnitPO::new);
	}

	private List<OrgUnit> touchChildren(List<OrgUnit> ou) {
		ou.forEach(child -> {
			child.getEmployees().size();
			touchChildren(child.getChildren());
		});
		return ou;
	}

	private void fixRelations(OrgUnit input) {
		input.getEmployees().forEach( emp -> emp.setEmployedIn(input) );
		input.getChildren().forEach(child -> {
			child.setParent(input);
			fixRelations(child);
		});
	}
}
