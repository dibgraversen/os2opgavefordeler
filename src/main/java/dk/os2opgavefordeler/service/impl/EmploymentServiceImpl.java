package dk.os2opgavefordeler.service.impl;

import dk.os2opgavefordeler.model.Employment;
import dk.os2opgavefordeler.model.Employment_;
import dk.os2opgavefordeler.model.presentation.EmploymentPO;
import dk.os2opgavefordeler.service.EmploymentService;
import dk.os2opgavefordeler.service.PersistenceService;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.Query;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class EmploymentServiceImpl implements EmploymentService {
	@Inject
	PersistenceService persistence;

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

	@Override
	@SuppressWarnings("unchecked")
	public List<Employment> getAll(long municipalityId) {
		Query query = persistence.getEm().createQuery("SELECT emp FROM Employment emp WHERE emp.employedIn.municipality.id = :municipalityId");
		query.setMaxResults(200);
		query.setParameter("municipalityId", municipalityId);
		return query.getResultList();
	}

	@Override
	public List<EmploymentPO> getAllPO(long municipalityId) {
		final List<Employment> employments = getAll(municipalityId);
		return employments.stream()
			.map(EmploymentPO::new)
			.collect(Collectors.toList());
	}
}
