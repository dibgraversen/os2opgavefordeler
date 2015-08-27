package dk.os2opgavefordeler.service.impl;

import dk.os2opgavefordeler.model.*;
import dk.os2opgavefordeler.model.presentation.DistributionRulePO;
import dk.os2opgavefordeler.rest.DistributionRuleScope;
import dk.os2opgavefordeler.service.DistributionService;
import dk.os2opgavefordeler.service.EmploymentService;
import dk.os2opgavefordeler.service.PersistenceService;
import org.slf4j.Logger;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
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

	@Inject
	EmploymentService employmentService;

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
	public Optional<DistributionRule> getDistribution(long id) {
		final List<DistributionRule> result = persistence.criteriaFind(DistributionRule.class,
			(cb, cq, ent) -> cq.where(cb.equal(ent.get(DistributionRule_.id), id))
		);

		return result.isEmpty() ?
			Optional.empty() :
			Optional.of(result.get(0));
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<DistributionRule> getDistributionsAll(long municipalityId) {
		log.info("here with muncipalityId: {}", municipalityId);
		Query query = persistence.getEm().createQuery("SELECT r FROM DistributionRule r WHERE r.municipality.id = :municipalityId ORDER BY r.kle.number ASC");
		query.setParameter("municipalityId", municipalityId);
//		query.setMaxResults(300);
		List<DistributionRule> result = query.getResultList();
		log.info("result size: {}", result.size());
		return result;
	}

	@Override
	public List<DistributionRule> getDistributionsForOrg(long orgId, long municipalityId, boolean includeUnassigned) {
		return getDistributionsForOrg(orgId, municipalityId, includeUnassigned, false);
	}

	@Override
	public List<DistributionRule> getDistributionsForOrg(long orgId, long municipalityId, boolean includeUnassigned, boolean includeImplicit) {
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
			if (includeUnassigned || includeImplicit) {
				// TODO stop including all with no responsible org.
				final Predicate mainOrUnassigned = cb.or(main, cb.isNull(root.get(DistributionRule_.responsibleOrg)));
				predicates.add(mainOrUnassigned);
			} else {
				predicates.add(main);
			}
			cq.where(predicates.toArray(new Predicate[]{}));
			Join<DistributionRule, Kle> ruleKleJoin = root.join(DistributionRule_.kle);
			cq.orderBy(cb.asc(ruleKleJoin.get("number")));
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

	@Override
	public void buildRulesForMunicipality(long municipalityId) {
		log.info("Building rules");
		createMissingDistributionRules(municipalityId);
		updateParentsForDistributionRules(municipalityId);
	}

	@Override
	public DistributionRule findAssigned(Kle kle, Municipality municipality) {
		Query responsibleQuery = persistence.getEm().createQuery("SELECT r FROM DistributionRule r WHERE r.kle = :kle AND r.municipality = :municipality");
		responsibleQuery.setParameter("kle", kle);
		responsibleQuery.setParameter("municipality", municipality);
		try {
			DistributionRule rule = (DistributionRule) responsibleQuery.getSingleResult();
			return findResponsible(rule);
		} catch (NoResultException nre) {
			// TODO make one?
			log.warn("Did not find a DistributionRule for kle: {} and municipality: {}", kle, municipality);
		} catch (NonUniqueResultException nure) {
			log.error("Too many distributionrules for kle: {} and municipality: {}", kle, municipality);
		}
		return null;
	}

	private DistributionRule findResponsible(DistributionRule rule){
		if(rule.getAssignedOrg().isPresent()){
			return rule;
		} else if (rule.getParent().isPresent()) {
			return findResponsible(rule.getParent().get());
		} else {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	private void createMissingDistributionRules(long municipalityId){
		Query municipalityQuery = persistence.getEm().createQuery("SELECT m FROM Municipality m WHERE m.id = :municipalityId");
		municipalityQuery.setParameter("municipalityId", municipalityId);
		try {
			// find municipality
			Municipality municipality = (Municipality) municipalityQuery.getSingleResult();
			Query query = persistence.getEm().createQuery("SELECT k FROM Kle k WHERE k.id NOT IN ( SELECT dr.kle.id FROM DistributionRule dr WHERE municipality.id = :municipalityId)");
			query.setParameter("municipalityId", municipalityId);
			// find kle's for which there is no rule...
			List<Kle> klesWithNoRule = query.getResultList();
			if(klesWithNoRule != null && klesWithNoRule.size() > 0){
				// create rules.
				for (Kle kle : klesWithNoRule) {
					DistributionRule rule = new DistributionRule();
					rule.setKle(kle);
					rule.setMunicipality(municipality);
					log.info("creating rule: {}", rule);
					createDistributionRule(rule);
				}
			}
		} catch	(NonUniqueResultException nonUniqueResultException) {
			log.error("duplicate result on municipalityId lookup", nonUniqueResultException);
		}
	}

	@SuppressWarnings("unchecked")
	private void updateParentsForDistributionRules(long municipalityId){
		Query getOrphanedRuleIdsQuery = persistence.getEm().createQuery("SELECT rule FROM DistributionRule rule WHERE rule.municipality.id = :municipalityId AND rule.parent IS NULL AND LENGTH(rule.kle.number) > 2");
		getOrphanedRuleIdsQuery.setParameter("municipalityId", municipalityId);
		List<DistributionRule> orphanedRuleIds = getOrphanedRuleIdsQuery.getResultList();
		if(orphanedRuleIds != null && orphanedRuleIds.size() > 0){
			for (DistributionRule orphanedRule : orphanedRuleIds) {
				Query findParentQuery = persistence.getEm()
						.createQuery("SELECT parent FROM DistributionRule parent WHERE parent.municipality.id = :municipalityId AND parent.kle.number = :parentNumber");
				findParentQuery.setParameter("municipalityId", municipalityId);
				String parentNumber = orphanedRule.getKle().getNumber().substring(0, orphanedRule.getKle().getNumber().length() - 3);
				findParentQuery.setParameter("parentNumber", parentNumber);
				List<DistributionRule> parents = findParentQuery.getResultList();
				if(parents.size() == 1){
					orphanedRule.setParent(parents.get(0));
					persistence.getEm().merge(orphanedRule);
				} else {
					log.warn("parent size NOT 1 for: {}", orphanedRule);
					for (DistributionRule parent : parents) {
						log.warn("parent found: {} for orphan: {}", parent, orphanedRule);
					}

				}
			}
		}
	}

	public Optional<Employment> findResponsibleEmployee(DistributionRule rule){
		if(rule.getAssignedEmp() > 0l){
			return employmentService.getEmployment(rule.getAssignedEmp());
		} else if(rule.getParent().isPresent()){
			return findResponsibleEmployee(rule.getParent().get());
		}
		return Optional.empty();
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
	private java.util.function.Predicate<DistributionRule> getFilter(long orgId, boolean includeUnassigned, boolean includeImplicit) {
		if (includeImplicit) {
			// Include if implicitly owned by orgId, filter root-unowned based on 'includeUnassigned'
			return dr -> getImplicitOwner(dr).map(id -> id == orgId).orElse(includeUnassigned);
		} else {
			// Include if explicitly owned by orgId, filter out implicitly owned, leave root-unowned.
			return dr -> ( orgId == dr.getResponsibleOrg().map(OrgUnit::getId).orElse(-1L)) || !getImplicitOwner(dr).isPresent();
		}
	}

	/**
	 * Returns the implicit owner of a DistributionRule.
	 * @param dr DisitrbutionRule to find implicit owner for.
	 * @return implicit owner, or Optional.empty for dr with no top-level owner.
	 */
	private Optional<Long> getImplicitOwner(DistributionRule dr) {
		if(dr == null) {
			return Optional.empty();
		} else if(dr.getResponsibleOrg().isPresent()) {
			return dr.getResponsibleOrg().map(OrgUnit::getId);
		} else {
			return getImplicitOwner(dr.getParent().orElse(null));
		}
	}
}
