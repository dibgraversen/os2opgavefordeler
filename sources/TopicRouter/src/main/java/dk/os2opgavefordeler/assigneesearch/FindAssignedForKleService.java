package dk.os2opgavefordeler.assigneesearch;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.Iterables;

import javax.enterprise.context.ApplicationScoped;

import javax.inject.Inject;

import dk.os2opgavefordeler.util.FilterHelper;
import org.slf4j.Logger;

import dk.os2opgavefordeler.distribution.DistributionRuleRepository;

import dk.os2opgavefordeler.model.*;

import dk.os2opgavefordeler.service.EmploymentService;

@ApplicationScoped
public class FindAssignedForKleService {

    @Inject
    private Logger logger;

    @Inject
    private DistributionRuleRepository repository;

    @Inject
    private EmploymentService employmentService;

	/**
	 * Returns the assignee for the specified KLE and municipality
	 *
	 * @param kle KLE to find assignee for
	 * @param municipality municipality to search within
	 * @return assignee for the KLE and municipality
	 */
	public Assignee findAssignedForKle(Kle kle, Municipality municipality) {
        return findAssignedForKle(kle, municipality, new HashMap<>());
    }

	/**
	 * Returns the assignee for the specified KLE and municipality using the given filter parameters
	 *
	 * @param kle KLE to find assignee for
	 * @param municipality municipality to search within
	 * @param filterParameters map of parameters to use when searching
	 * @return assignee for the KLE and municipality
	 */
    public Assignee findAssignedForKle(Kle kle, Municipality municipality, Map<String, String> filterParameters) {
        Iterable<DistributionRule> distributionRules = repository.findByKleAndMunicipality(kle, municipality);

        if (Iterables.isEmpty(distributionRules)) {
            return null;
        }

        if (Iterables.size(distributionRules) > 1) {
            // Non unique
            return null;
        }

	    DistributionRule distributionRule = Iterables.get(distributionRules, 0);

	    return findResponsible(distributionRule, filterParameters);
    }

    private Assignee createAssignee(DistributionRule rule, OrgUnit orgUnit, Optional<Employment> employment) {
        if (!employment.isPresent()) {
            return new Assignee(rule,orgUnit);
        }

        return new Assignee(rule, orgUnit, employment.get());
    }

    private Assignee matchByFilter(DistributionRule distributionRule, Map<String, String> filterParameters) {
	    Iterable<DistributionRuleFilter> filters = distributionRule.getFilters();

	    for (DistributionRuleFilter filter: filters) {
		    if (filter.matches(filterParameters)) {
			    return createAssignee(distributionRule, filter.getAssignedOrg(), Optional.ofNullable(filter.getAssignedEmployee()));
            }
        }

        return null;
    }

    private Assignee findResponsible(DistributionRule rule, Map<String, String> filterParameters) {
        Assignee byFilter = matchByFilter(rule, filterParameters);

	    if (byFilter != null) {
            return byFilter;
        }

        if (rule.getAssignedOrg().isPresent()) {
            return createAssignee(rule, rule.getAssignedOrg().get(), employmentService.getEmployment(rule.getAssignedEmp()));
        }
        else if (rule.getParent().isPresent()) {
            return findResponsible(rule.getParent().get(), filterParameters);
        }
        else {
            return null;
        }
    }

}
