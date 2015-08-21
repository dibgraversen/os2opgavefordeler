package dk.os2opgavefordeler.service.impl;

import dk.os2opgavefordeler.model.Employment;
import dk.os2opgavefordeler.model.Employment_;
import dk.os2opgavefordeler.model.OrgUnit;
import dk.os2opgavefordeler.model.presentation.EmploymentPO;
import dk.os2opgavefordeler.service.EmploymentService;
import dk.os2opgavefordeler.service.OrgUnitService;
import dk.os2opgavefordeler.service.PersistenceService;
import org.slf4j.Logger;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class EmploymentServiceImpl implements EmploymentService {
	@Inject
	PersistenceService persistence;

	@Inject
	OrgUnitService orgUnitService;

	@Inject
	Logger log;

	@Override
	public Optional<Employment> getEmployment(long id) {
		final List<Employment> results = persistence.criteriaFind(Employment.class,
			(cb, cq, ou) -> cq.where(cb.equal(ou.get(Employment_.id), id)
			)
		);

		return results.isEmpty() ?
			Optional.empty() :
			Optional.of(results.get(0));
	}

	@Override
	public List<Employment> findByEmail(String email) {
		final List<Employment> results = persistence.criteriaFind(Employment.class,
			(cb, cq, ou) -> cq.where(cb.equal(ou.get(Employment_.email), email)
			)
		);

		return results;
	}

	@Override
	public Optional<EmploymentPO> getEmploymentPO(long id) {
		return getEmployment(id).map(EmploymentPO::new);
	}

	private List<Employment> getManaged(long municipalityId, long employmentId){
		List<Employment> result = new ArrayList<>();
		Optional<Employment> managerMaybe = getEmployment(employmentId);
		if(managerMaybe.isPresent()){
			Employment manager = managerMaybe.get();
			List<OrgUnit> managedOrgunits = new ArrayList<>();
			OrgUnit top = manager.getEmployedIn();
			managedOrgunits.add(top);
			List<OrgUnit> managedOrgs = orgUnitService.getManagedOrgUnits(municipalityId, employmentId);
			managedOrgunits.addAll(managedOrgs);
			for (OrgUnit managedOrgunit : managedOrgunits) {
				// Add manager.
				if(managedOrgunit.getManager().isPresent()){
					result.add(managedOrgunit.getManager().get());
				}
				// Add employees.
				if(!managedOrgunit.getChildren().isEmpty()){
					result.addAll(managedOrgunit.getEmployees());
				} else {
				}
			}
		}
		return result;
	}

	@Override
	public List<EmploymentPO> getManagedAsPO(long municipalityId, long employmentId){
		final List<Employment> employments = getManaged(municipalityId, employmentId);
		return employments.stream()
				.map(emp -> {
					EmploymentPO newEmp = new EmploymentPO(emp);
					newEmp.setSubordinate(true);
					return newEmp;
				})
				.collect(Collectors.toList());
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Employment> getAll(long municipalityId) {
		Query query = persistence.getEm().createQuery("SELECT emp FROM Employment emp WHERE emp.employedIn.municipality.id = :municipalityId");
		query.setMaxResults(100);
		query.setParameter("municipalityId", municipalityId);
		return query.getResultList();
	}

	@Override
	public List<EmploymentPO> getAllPO(long municipalityId, long employmentId) {
		final List<Employment> employments = getAll(municipalityId);
		final List<Employment> managed = getManaged(municipalityId, employmentId);
		List<EmploymentPO> result = employments.stream()
				.map(emp -> {
					EmploymentPO newEmp = new EmploymentPO(emp);
					newEmp.setSubordinate(managed.contains(emp));
					return newEmp;
				})
				.collect(Collectors.toList());

		return result;
	}


}
