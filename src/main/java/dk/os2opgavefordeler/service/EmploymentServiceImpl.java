package dk.os2opgavefordeler.service;

import dk.os2opgavefordeler.model.Employment;
import dk.os2opgavefordeler.model.Employment_;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

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
}
