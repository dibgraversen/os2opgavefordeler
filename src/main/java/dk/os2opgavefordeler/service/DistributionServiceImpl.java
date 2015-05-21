package dk.os2opgavefordeler.service;

import dk.os2opgavefordeler.model.DistributionRule;
import dk.os2opgavefordeler.model.DistributionRule_;
import org.slf4j.Logger;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class DistributionServiceImpl implements DistributionService {
	@Inject
	Logger log;

	@Inject
	PersistenceService persistence;

	@Override
	public DistributionRule createDistributionRule(DistributionRule rule) {
		persistence.persist(rule);
		return rule;
	}

	@Override
	public List<DistributionRule> getDistributionsForOrg(final long orgId, final boolean includeUnassigned) {
		return persistence.criteriaFind(DistributionRule.class, new PersistenceService.CriteriaOp() {
			@Override
			public void apply(CriteriaBuilder cb, CriteriaQuery cq) {
				final Root<DistributionRule> rule = cq.from(DistributionRule.class);

				cq.where(cb.equal(rule.get(DistributionRule_.responsibleOrg), orgId));
				if(includeUnassigned) {
					cq.where(cb.or(rule.get(DistributionRule_.responsibleOrg).isNull()));
				}
			}
		});
	}
}
