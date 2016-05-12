package dk.os2opgavefordeler.service.impl;

import dk.os2opgavefordeler.distribution.DistributionRuleRepository;
import dk.os2opgavefordeler.model.*;
import dk.os2opgavefordeler.model.presentation.DistributionRulePO;
import dk.os2opgavefordeler.rest.DistributionRuleScope;
import dk.os2opgavefordeler.service.DistributionService;
import dk.os2opgavefordeler.service.OrgUnitService;
import dk.os2opgavefordeler.service.PersistenceService;

import org.apache.deltaspike.jpa.api.transaction.Transactional;

import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;

import javax.inject.Inject;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for dealing with Distribution Rules.
 * <p>
 * Implementation notes:
 * - must return concrete Lists, not Streams or computed views. The service is meant to be callable by things like
 * JAX-RS endpoints, which aren't invoked as EJB methods, and thus not wrapped in transactions.
 * Thus, not returning concrete List results in LazyLoadException.
 */
@ApplicationScoped
@Transactional
public class DistributionServiceImpl implements DistributionService {

    @Inject
    Logger log;

    @Inject
    OrgUnitService orgUnitService;

    @Inject
    private DistributionRuleRepository distributionRuleRepository;

    @Inject
    private EntityManager entityManager;

    @Inject
    private PersistenceService persistenceService;

    @Override
    public DistributionRule createDistributionRule(DistributionRule rule) {
        distributionRuleRepository.save(rule);
        return rule;
    }

    @Override
    public Optional<DistributionRule> getDistribution(long id) {
        return Optional.ofNullable(distributionRuleRepository.findBy(id));
    }

    @Override
    @SuppressWarnings("unchecked")
    @Transactional
    public List<DistributionRule> getDistributionsAll(long municipalityId) {
        Query query = entityManager.createQuery("SELECT r FROM DistributionRule r WHERE r.municipality.id = :municipalityId " +
                "AND LENGTH(r.kle.number) = 2 ORDER BY r.kle.number ASC");
        query.setParameter("municipalityId", municipalityId);

        return query.getResultList();
    }

    /**
     * Finds DistributionRules for that are top level and unassigned.
     *
     * @param municipalityId restricts DistributionRules to given municipality
     * @return List of top level, unassigned DistributionRules.
     */
    @SuppressWarnings("unchecked")
    private List<DistributionRule> getUnassignedDistributionRules(long municipalityId) {
        Query query = entityManager.createQuery("SELECT r FROM DistributionRule r WHERE r.municipality.id = :municipalityId " +
                "AND LENGTH(r.kle.number) = 2 AND r.responsibleOrg IS NULL ORDER BY r.kle.number ASC");
        query.setParameter("municipalityId", municipalityId);

        return query.getResultList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<DistributionRule> getDistributionsForOrg(long orgId, long municipalityId, boolean includeImplicit) {
        List<DistributionRule> results = persistenceService.criteriaFind(DistributionRule.class, (cb, cq, root) ->
        {
            // Implicit querying currently needs to include not directly owned rules, since their (possible) implicit
            // ownership will be determined by Java code.
            List<Predicate> predicates = new ArrayList<Predicate>();

            // filter by municipality.
            Join<DistributionRule, Municipality> ruleMunicipalityJoin = root.join(DistributionRule_.municipality);
            Join<DistributionRule, Kle> ruleKleJoin = root.join(DistributionRule_.kle);

            final Predicate inMunicipality = cb.equal(ruleMunicipalityJoin.get(Municipality_.id), municipalityId);
            predicates.add(inMunicipality);

            final Predicate main = cb.equal(root.get(DistributionRule_.responsibleOrg), orgId);

            if (includeImplicit) {
                Optional<OrgUnit> org = orgUnitService.getOrgUnit(orgId);

                if (org.isPresent()) {
                    Optional<Employment> manager = orgUnitService.getActualManager(orgId);

                    if (manager.isPresent()) {
                        List<OrgUnit> managedUnits = orgUnitService.getManagedOrgUnits(municipalityId, manager.get().getId());
                        final Predicate implicit = root.get(DistributionRule_.responsibleOrg).in(managedUnits);
                        predicates.add(implicit);
                    }
                }
            }
            else {
                predicates.add(main);
            }

            cq.where(predicates.toArray(new Predicate[predicates.size()]));
            cq.orderBy(cb.asc(ruleKleJoin.get(Kle_.number)));
        });

        Map<Long, DistributionRule> resultsMap = new HashMap<>();

        for (DistributionRule result : results) {
            resultsMap.put(result.getId(), result);

            // ensure that both the immediate parent and top level parent is included to avoid breaking inherited values
            if (result.getParent().isPresent()) {
                // include immediate parent, if present
                if (!results.contains(result.getParent().get())) {
                    resultsMap.put(result.getParent().get().getId(), result.getParent().get());
                }

                // include top-level parent, if present
                if (result.getParent().get().getParent().isPresent() && !results.contains(result.getParent().get().getParent().get())) {
                    resultsMap.put(result.getParent().get().getParent().get().getId(), result.getParent().get().getParent().get());
                }
            }
        }

        List<DistributionRule> unassigned = getUnassignedDistributionRules(municipalityId);

        for (DistributionRule unassignedRule : unassigned) {
            resultsMap.put(unassignedRule.getId(), unassignedRule);
        }

        return resultsMap.values().stream()
                .sorted((o1, o2) -> o1.getKle().getNumber().compareTo(o2.getKle().getNumber()))
                .collect(Collectors.toList());
    }

    @Override
    public List<DistributionRulePO> getPoDistributions(OrgUnit orgUnit, DistributionRuleScope scope) {
        final List<DistributionRule> distributions;

        switch (scope) {
            case INHERITED:
                distributions = getDistributionsForOrg(orgUnit.getId(), orgUnit.getMunicipality().get().getId(), true);
                break;
            case RESPONSIBLE:
                distributions = getDistributionsForOrg(orgUnit.getId(), orgUnit.getMunicipality().get().getId(), false);
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
        createMissingDistributionRules(municipalityId);
        updateParentsForDistributionRules(municipalityId);
    }

    @Override
    public DistributionRule findAssigned(Kle kle, Municipality municipality) {
        Query responsibleQuery = entityManager.createQuery("SELECT r FROM DistributionRule r WHERE r.kle = :kle AND r.municipality = :municipality");
        responsibleQuery.setParameter("kle", kle);
        responsibleQuery.setParameter("municipality", municipality);

        try {
            DistributionRule rule = (DistributionRule) responsibleQuery.getSingleResult();
            return findResponsible(rule);
        }
        catch (NoResultException nre) {
            // TODO make one?
            log.warn("Did not find a DistributionRule for kle: {} and municipality: {}", kle, municipality);
        }
        catch (NonUniqueResultException nure) {
            log.error("Too many distributionrules for kle: {} and municipality: {}", kle, municipality);
        }
        return null;
    }

    private DistributionRule findResponsible(DistributionRule rule) {
        if (rule.getAssignedOrg().isPresent()) {
            return rule;
        }
        else if (rule.getParent().isPresent()) {
            return findResponsible(rule.getParent().get());
        }
        else {
            return null;
        }
    }

    /**
     * Checks for KLE's for which the given municipality does not have any matching rule, and creates it.
     *
     * @param municipalityId the id of the municipality for which this operation is done.
     */
    @SuppressWarnings("unchecked")
    private void createMissingDistributionRules(long municipalityId) {
        Query municipalityQuery = entityManager.createQuery("SELECT m FROM Municipality m WHERE m.id = :municipalityId");
        municipalityQuery.setParameter("municipalityId", municipalityId);

        try {
            // find municipality
            Municipality municipality = (Municipality) municipalityQuery.getSingleResult();
            Query query = entityManager.createQuery("SELECT k FROM Kle k WHERE k.id NOT IN " +
                    "( SELECT dr.kle.id FROM DistributionRule dr WHERE municipality = :municipality) " +
                    "AND (k.municipality IS NULL OR k.municipality = :municipality)"); // municipality specific KLE handling.
            query.setParameter("municipality", municipality);

            // find kle's for which there is no rule...
            List<Kle> klesWithNoRule = query.getResultList();
            if (klesWithNoRule != null && !klesWithNoRule.isEmpty()) {
                // create rules
                for (Kle kle : klesWithNoRule) {
                    DistributionRule rule = new DistributionRule();
                    rule.setKle(kle);
                    rule.setMunicipality(municipality);
                    log.debug("creating rule: {}", rule);
                    distributionRuleRepository.save(rule);
                    createDistributionRule(rule);
                }
            }
        } catch (NonUniqueResultException nonUniqueResultException) {
            log.error("duplicate result on municipalityId lookup", nonUniqueResultException);
        }
    }

    /**
     * Utility method in case non-top level rules do not have a parent assigned.
     *
     * @param municipalityId id of municipality for which to do the update.
     */
    @SuppressWarnings("unchecked")
    private void updateParentsForDistributionRules(long municipalityId) {
        Query getOrphanedRuleIdsQuery = entityManager.createQuery("SELECT rule FROM DistributionRule rule " +
                "WHERE rule.municipality.id = :municipalityId AND rule.parent IS NULL AND LENGTH(rule.kle.number) > 2");
        getOrphanedRuleIdsQuery.setParameter("municipalityId", municipalityId);
        List<DistributionRule> orphanedRuleIds = getOrphanedRuleIdsQuery.getResultList();

        if (orphanedRuleIds != null && !orphanedRuleIds.isEmpty()) {
            for (DistributionRule orphanedRule : orphanedRuleIds) {
                Query findParentQuery = entityManager
                        .createQuery("SELECT parent FROM DistributionRule parent WHERE parent.municipality.id = :municipalityId AND parent.kle.number = :parentNumber");
                findParentQuery.setParameter("municipalityId", municipalityId);
                String parentNumber = orphanedRule.getKle().getNumber().substring(0, orphanedRule.getKle().getNumber().length() - 3);
                findParentQuery.setParameter("parentNumber", parentNumber);
                List<DistributionRule> parents = findParentQuery.getResultList();

                if (parents.size() == 1) {
                    orphanedRule.setParent(parents.get(0));
                    entityManager.merge(orphanedRule);
                }
                else {
                    log.warn("parent size NOT 1 for: {}", orphanedRule);

                    for (DistributionRule parent : parents) {
                        log.warn("parent found: {} for orphan: {}", parent, orphanedRule);
                    }
                }
            }
        }
    }

    @Override
    public Optional<Employment> findResponsibleEmployee(DistributionRule rule) {
        if (rule.getAssignedEmp() > 0l) {
            return getEmployment(rule.getAssignedEmp());
        }
        else if (rule.getParent().isPresent()) {
            return findResponsibleEmployee(rule.getParent().get());
        }
        return Optional.empty();
    }

    /**
     * Fetches children for a given DistributionRule.
     *
     * @param ruleId The id of the parent rule.
     * @param orgUnit The organizational unit for the user
     * @param scope The scope for fetching children (e.g. only rules that the user is responsible for)
     * @return A list of children rules, matching the given parent.
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<DistributionRule> getChildren(Long ruleId, OrgUnit orgUnit, DistributionRuleScope scope) {
        if (ruleId == null || orgUnit == null) {
            return new ArrayList<>();
        }
        else {
            Query query;

            switch (scope) {
                case INHERITED:
                    query = entityManager.createQuery("SELECT rule FROM DistributionRule rule WHERE rule.parent.id = :parentId");
                    query.setParameter("parentId", ruleId);

                    break;
                case RESPONSIBLE:
                    /*query = entityManager.createQuery("SELECT rule FROM DistributionRule rule WHERE rule.parent.id = :parentId");
                    query.setParameter("parentId", ruleId);*/

                    query = entityManager.createQuery("SELECT rule FROM DistributionRule rule WHERE rule.parent.id = :parentId and rule.responsibleOrg.id = :orgId");
                    query.setParameter("parentId", ruleId);
                    query.setParameter("orgId", orgUnit.getId());

                    break;
                case ALL:
                default:/* intentional fallthrough */
                    query = entityManager.createQuery("SELECT rule FROM DistributionRule rule WHERE rule.parent.id = :parentId");
                    query.setParameter("parentId", ruleId);
            }

            return query.getResultList();
        }
    }

    @Override
    public DistributionRule createDistributionRule(Kle kle) {
        DistributionRule.Builder builder = DistributionRule.builder();
        builder.municipality(kle.getMunicipality()).kle(kle);
        DistributionRule rule = builder.build();

        Query parentQuery = entityManager.createQuery("SELECT r from DistributionRule r WHERE r.kle = :parentKle AND r.municipality = :municipality");
        parentQuery.setParameter("parentKle", kle.getParent());
        parentQuery.setParameter("municipality", kle.getMunicipality());

        DistributionRule parentRule = (DistributionRule) parentQuery.getSingleResult();
        rule.setParent(parentRule);
        entityManager.persist(rule);

        return rule;
    }

    //--------------------------------------------------------------------------
    // Helpers
    //--------------------------------------------------------------------------

    /**
     * Employment lookup to avoid injecting employment service.
     *
     * @param id of employment
     * @return employment if found, otherwise empty Optional.
     */
    private Optional<Employment> getEmployment(long id) {
        try {
            Employment employment = entityManager.find(Employment.class, id);
            return Optional.of(employment);
        }
        catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Returns a predicate suitable for post-query filtering of DistributionRules.
     * @param orgId the orgId we're querying for
     * @param includeImplicit whether to include implicitly owned DistributionRules
     * @return
     */
//	private java.util.function.Predicate<DistributionRule> getFilter(long orgId, boolean includeImplicit) {
//		if (includeImplicit) {
//			return dr -> getImplicitOwner(dr).map(id -> id == orgId).orElse(false);
//		} else {
//			 Include if explicitly owned by orgId, filter out implicitly owned, leave root-unowned.
//			return dr -> ( orgId == dr.getResponsibleOrg().map(OrgUnit::getId).orElse(-1L)) || !getImplicitOwner(dr).isPresent();
//		}
//	}

    /**
     * Returns the implicit owner of a DistributionRule.
     * @param dr DisitrbutionRule to find implicit owner for.
     * @return implicit owner, or Optional.empty for dr with no top-level owner.
     */
//	private Optional<Long> getImplicitOwner(DistributionRule dr) {
//		if(dr == null) {
//			return Optional.empty();
//		} else if(dr.getResponsibleOrg().isPresent()) {
//			return dr.getResponsibleOrg().map(OrgUnit::getId);
//		} else {
//			return getImplicitOwner(dr.getParent().orElse(null));
//		}
//	}
}
