package dk.os2opgavefordeler.service.impl;

import dk.os2opgavefordeler.model.Employment;
import dk.os2opgavefordeler.model.Employment_;
import dk.os2opgavefordeler.model.OrgUnit;
import dk.os2opgavefordeler.model.OrgUnit_;
import dk.os2opgavefordeler.model.presentation.OrgUnitPO;
import dk.os2opgavefordeler.service.OrgUnitService;
import dk.os2opgavefordeler.service.PersistenceService;

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
		final Optional<OrgUnit> orgUnit = getToplevelOrgUnit();

		return orgUnit.map( ou -> ou.flattened().map(OrgUnitPO::new).collect(Collectors.toList()) )
			.orElse(Collections.emptyList());
	}

	@Override
	public Optional<OrgUnitPO> getOrgUnitPO(int id) {
		return getOrgUnit(id).map(OrgUnitPO::new);
	}

	@Override
	public Optional<Employment> getEmployment(int id) {
		final List<Employment> results = persistence.criteriaFind(Employment.class,
			(cb, cq, ou) -> cq.where(cb.equal(ou.get(Employment_.id), id))
		);

		return results.isEmpty() ?
			Optional.empty() :
			Optional.of(results.get(0));
	}

	@Override
	public Optional<Employment> getEmploymentByName(String name) {
		final List<Employment> results = persistence.criteriaFind(Employment.class,
			(cb, cq, ou) -> cq.where(cb.equal(ou.get(Employment_.name), name))
		);

		return results.isEmpty() ?
			Optional.empty() :
			Optional.of(results.get(0));
	}

	private List<OrgUnit> touchChildren(List<OrgUnit> ou) {
		ou.forEach(child -> {
			child.getEmployees().size();
			touchChildren(child.getChildren());
		});
		return ou;
	}

	private void fixRelations(OrgUnit input) {
		input.getManager().ifPresent(man -> man.setEmployedIn(input));
		input.getEmployees().forEach( emp -> emp.setEmployedIn(input) );
		input.getChildren().forEach(child -> {
			child.setParent(input);
			fixRelations(child);
		});
	}
}
