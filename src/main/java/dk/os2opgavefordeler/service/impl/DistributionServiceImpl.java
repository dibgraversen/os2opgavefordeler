package dk.os2opgavefordeler.service.impl;

import dk.os2opgavefordeler.model.*;
import dk.os2opgavefordeler.model.presentation.DistributionRulePO;
import dk.os2opgavefordeler.rest.DistributionRuleScope;
import dk.os2opgavefordeler.service.DistributionService;
import dk.os2opgavefordeler.service.PersistenceService;
import org.slf4j.Logger;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.Query;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
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
	public DistributionRule merge(DistributionRule rule) {
		return persistence.merge(rule);
	}

	@Override
	public Optional<DistributionRule> getDistribution(int id) {
		final List<DistributionRule> result = persistence.criteriaFind(DistributionRule.class,
			(cb, cq, ent) -> cq.where(cb.equal(ent.get(DistributionRule_.id), id))
		);

		return result.isEmpty() ?
			Optional.empty() :
			Optional.of(result.get(0));
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<DistributionRule> getDistributionsAll(int municipalityId) {
		Query query = persistence.getEm().createQuery("SELECT r FROM DistributionRule r LEFT JOIN r.kle LEFT JOIN r.municipality WHERE r.municipality.id = :municipalityId ORDER BY r.kle.id ASC");
		query.setParameter("municipalityId", municipalityId);
		return query.getResultList();
	}

	@Override
	public List<DistributionRule> getDistributionsForOrg(int orgId, int municipalityId, boolean includeUnassigned) {
		return getDistributionsForOrg(orgId, municipalityId, includeUnassigned, false);
	}

	@Override
	public List<DistributionRule> getDistributionsForOrg(int orgId, int municipalityId, boolean includeUnassigned, boolean includeImplicit) {
		//TODO: implement a query-only solution instead of the current mix of query and subsequent collection filtering.
		List<DistributionRule> results = persistence.criteriaFind(DistributionRule.class, (cb, cq, root) ->
		{
			// Implicit querying currently needs to include not directly owned rules, since their (possible) implicit
			// ownership will be determined by Java code.
			List<Predicate> predicates = new ArrayList<Predicate>();
			// filter by municipality.
			Join<DistributionRule, Municipality> ruleMunicipalityJoin = root.join(DistributionRule_.municipality);
			final Predicate inMunicipality = cb.equal(ruleMunicipalityJoin.get(Municipality_.id), municipalityId);
			predicates.add(inMunicipality);

			final Predicate main = cb.equal(root.get(DistributionRule_.responsibleOrg), orgId);
			if(includeUnassigned || includeImplicit){
				// TODO stop including all with no responsible org.
				final Predicate mainOrUnassigned = cb.or(main, cb.isNull( root.get(DistributionRule_.responsibleOrg)));
				predicates.add(mainOrUnassigned);
			} else {
				predicates.add(main);
			}

			cq.where(predicates.toArray(new Predicate[]{}));
		});

		return results.stream()
			.filter(getFilter(orgId, includeUnassigned, includeImplicit))
			.collect(Collectors.toList());
	}

	@Override
	public List<DistributionRulePO> getPoDistributions(OrgUnit orgUnit, DistributionRuleScope scope) {
		final List<DistributionRule> distributions;
		switch(scope) {
			case INHERITED:
				distributions = getDistributionsForOrg(orgUnit.getId(), orgUnit.getMunicipality().get().getId(), true, true);
				break;
			case RESPONSIBLE:
				distributions = getDistributionsForOrg(orgUnit.getId(), orgUnit.getMunicipality().get().getId(), true, false);
				break;
			case ALL:
			default:/* intentional fallthrough */
				distributions = getDistributionsAll(orgUnit.getMunicipality().get().getId());
		}

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
			return dr -> ( orgId == dr.getResponsibleOrg().map(OrgUnit::getId).orElse(-1)) || !getImplicitOwner(dr).isPresent();
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
		} else if(dr.getResponsibleOrg().isPresent()) {
			return dr.getResponsibleOrg().map(OrgUnit::getId);
		} else {
			return getImplicitOwner(dr.getParent().orElse(null));
		}
	}
}
