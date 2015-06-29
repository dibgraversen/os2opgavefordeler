package dk.os2opgavefordeler.service;

import dk.os2opgavefordeler.model.Employment;
import dk.os2opgavefordeler.model.Employment_;
import dk.os2opgavefordeler.model.presentation.EmploymentPO;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class EmploymentServiceImpl implements EmploymentService {
	@Inject
	PersistenceService persistence;

	@Override
	public Optional<Employment> getEmployment(int id) {
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
	public Optional<EmploymentPO> getEmploymentPO(int id) {
		return getEmployment(id).map(EmploymentPO::new);
	}

	@Override
	public List<Employment> getAll() {
		return persistence.findAll(Employment.class);
	}

	@Override
	public List<EmploymentPO> getAllPO() {
		final List<Employment> employments = getAll();
		return employments.stream()
			.map(EmploymentPO::new)
			.collect(Collectors.toList());
	}
}
