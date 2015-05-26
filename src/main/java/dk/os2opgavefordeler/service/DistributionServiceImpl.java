package dk.os2opgavefordeler.service;

import dk.os2opgavefordeler.model.DistributionRule;
import dk.os2opgavefordeler.model.DistributionRule_;
import dk.os2opgavefordeler.model.presentation.DistributionRulePO;
import org.slf4j.Logger;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.criteria.Predicate;
import java.util.List;
import java.util.stream.Collectors;

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
		return persistence.criteriaFind(DistributionRule.class, (cb, cq, rule) ->
		{
			final Predicate main = cb.equal(rule.get(DistributionRule_.responsibleOrg), orgId);
			final Predicate pred = includeUnassigned ?
				cb.or(main, cb.equal(rule.get(DistributionRule_.responsibleOrg), 0)) :
				main;

			cq.where(pred);
		});
	}

	@Override
	public List<DistributionRulePO> getPoDistributions(long orgId, boolean includeUnassigned) {
		return getDistributionsForOrg(orgId, includeUnassigned).stream()
			.map(DistributionRulePO::new)
			.collect(Collectors.toList());
	}
}
