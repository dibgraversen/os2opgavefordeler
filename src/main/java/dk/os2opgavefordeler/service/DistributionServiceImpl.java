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
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for dealing with Distribution Rules.
 *
 * Implementation notes:
 * - must return concrete Lists, not Streams or computed views. The service is meant to be callable by things like
 *   JAX-RS endpoints, which aren't invoked as EJB methods, and thus not wrapped in transactions.
 *   Thus, not returning concrete List results in LazyLoadException.
 */
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
	public List<DistributionRule> getDistributionsAll() {
		return persistence.findAll(DistributionRule.class);
	}

	@Override
	public List<DistributionRule> getDistributionsForOrg(int orgId, boolean includeUnassigned) {
		return getDistributionsForOrg(orgId, includeUnassigned, false);
	}

	@Override
	public List<DistributionRule> getDistributionsForOrg(int orgId, boolean includeUnassigned, boolean includeImplicit) {
		//TODO: implement a query-only solution instead of the current mix of query and subsequent collection filtering.

		List<DistributionRule> results = persistence.criteriaFind(DistributionRule.class, (cb, cq, rule) ->
		{
			// Implicit querying currently needs to include not directly owned rules, since their (possible) implicit
			// ownership will be determined by Java code.
			final Predicate main = cb.equal(rule.get(DistributionRule_.responsibleOrg), orgId);
			final Predicate pred = (includeUnassigned || includeImplicit) ?
				cb.or(main, cb.equal(rule.get(DistributionRule_.responsibleOrg), 0)) :
				main;

			cq.where(pred);
		});

		return results.stream()
			.filter(getFilter(orgId, includeUnassigned, includeImplicit))
			.collect(Collectors.toList());
	}

	@Override
	public List<DistributionRulePO> getPoDistributionsAll() {
		List<DistributionRule> distributions = getDistributionsAll();
		return distributions.stream()
			.map(DistributionRulePO::new)
			.collect(Collectors.toList());
	}

	@Override
	public List<DistributionRulePO> getPoDistributions(int orgId, boolean includeUnassigned, boolean includeImplicit) {
		List<DistributionRule> distributions = getDistributionsForOrg(orgId, includeUnassigned, includeImplicit);
		return distributions.stream()
			.map(DistributionRulePO::new)
			.collect(Collectors.toList());
	}

	//--------------------------------------------------------------------------
	// Helpers
	//--------------------------------------------------------------------------

	/**
	 * Returns a predicate suitable for post-query filtering of DistributionRules.
	 * @param orgId the orgId we're querying for
	 * @param includeUnassigned whether to include unassigned DistributionRules
	 * @param includeImplicit whether to include implicitly owned DistributionRules
	 * @return
	 */
	private java.util.function.Predicate<DistributionRule> getFilter(int orgId, boolean includeUnassigned, boolean includeImplicit) {
		if (includeImplicit) {
			// Include if implicitly owned by orgId, filter root-unowned based on 'includeUnassigned'
			return dr -> getImplicitOwner(dr).map(id -> id == orgId).orElse(includeUnassigned);
		} else {
			// Include if explicitly owned by orgId, filter out implicitly owned, leave root-unowned.
			return dr -> (orgId == dr.getResponsibleOrg()) || !getImplicitOwner(dr).isPresent();
		}
	}

	/**
	 * Returns the implicit owner of a DistributionRule.
	 * @param dr DisitrbutionRule to find implicit owner for.
	 * @return implicit owner, or Optional.empty for dr with no top-level owner.
	 */
	private Optional<Integer> getImplicitOwner(DistributionRule dr) {
		if(dr == null) {
			return Optional.empty();
		} else if(dr.getResponsibleOrg() != 0) {
			return Optional.of(dr.getResponsibleOrg());
		} else {
			return getImplicitOwner(dr.getParent().orElse(null));
		}
	}
}
