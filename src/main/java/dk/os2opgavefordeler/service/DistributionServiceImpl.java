package dk.os2opgavefordeler.service;

import dk.os2opgavefordeler.model.DistributionRule;
import dk.os2opgavefordeler.model.DistributionRule_;
import dk.os2opgavefordeler.model.presentation.DistributionRulePO;
import org.slf4j.Logger;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
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
			cq.where(cb.equal(rule.get(DistributionRule_.responsibleOrg), orgId));
			if(includeUnassigned) {
//					cq.where(cb.or(rule.get(DistributionRule_.responsibleOrg).isNull()));	// when we move from int -> reference.
				cq.where(cb.or(
					cb.equal( rule.get(DistributionRule_.responsibleOrg), 0) )
				);
			}
		});
	}

	@Override
	public List<DistributionRulePO> getPoDistributions(long orgId, boolean includeUnassigned) {
		return getDistributionsForOrg(orgId, includeUnassigned).stream()
			.map(DistributionRulePO::new)
			.collect(Collectors.toList());
	}
}
