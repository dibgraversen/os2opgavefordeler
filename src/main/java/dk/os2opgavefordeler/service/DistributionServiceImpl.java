package dk.os2opgavefordeler.service;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import dk.os2opgavefordeler.model.DistributionRule;
import dk.os2opgavefordeler.model.DistributionRule_;
import dk.os2opgavefordeler.model.presentation.DistributionRulePO;
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
		return persistence.criteriaFind(DistributionRule.class, new PersistenceService.CriteriaOp<DistributionRule>() {
			@Override
			public void apply(CriteriaBuilder cb, CriteriaQuery<DistributionRule> cq, Root<DistributionRule> rule) {
				cq.where(cb.equal(rule.get(DistributionRule_.responsibleOrg), orgId));
				if(includeUnassigned) {
//					cq.where(cb.or(rule.get(DistributionRule_.responsibleOrg).isNull()));	// when we move from int -> reference.
					cq.where(cb.or(
						cb.equal( rule.get(DistributionRule_.responsibleOrg), 0) )
					);
				}
			}
		});
	}

	@Override
	public List<DistributionRulePO> getPoDistributions(long orgId, boolean includeUnassigned) {
		List<DistributionRulePO> result = Lists.transform(getDistributionsForOrg(orgId, includeUnassigned),
				new Function<DistributionRule, DistributionRulePO>() {
					@Override
					public DistributionRulePO apply(DistributionRule rule) {
						return DistributionRulePO.from(rule);
					}
				}
		);

		return ImmutableList.copyOf(result);
	}
}
