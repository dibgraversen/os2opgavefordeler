package dk.os2opgavefordeler.assigneesearch;

import com.google.common.collect.Iterables;
import dk.os2opgavefordeler.distribution.DistributionRuleRepository;
import dk.os2opgavefordeler.model.*;
import dk.os2opgavefordeler.service.EmploymentService;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class FindAssignedForKleService {

    @Inject
    private Logger logger;

    @Inject
    private DistributionRuleRepository repository;

    @Inject
    private EmploymentService employementService;

    public Assignee findAssignedForKle(Kle kle, Municipality municipality) {
        return findAssignedForKle(kle, municipality, new HashMap<>());
    }

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

    private Assignee createAssignee(OrgUnit orgUnit, long employmentId) {
        if (employmentId == 0) {
            return new Assignee(orgUnit);
        }
        Optional<Employment> employment = employementService.getEmployment(employmentId);
        return new Assignee(orgUnit, employment.get());
    }

    private Assignee matchByFilter(DistributionRule distributionRule, Map<String, String> filterParameters) {
        Iterable<DistributionRuleFilter> filters = distributionRule.getFilters();
        for (DistributionRuleFilter filter : filters) {
            if (filter.matches(filterParameters)) {
                return createAssignee(filter.getAssignedOrg(), filter.getAssignedEmp());
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
            return createAssignee(rule.getAssignedOrg().get(), rule.getAssignedEmp());
        } else if (rule.getParent().isPresent()) {
            return findResponsible(rule.getParent().get(), filterParameters);
        } else {
            return null;
        }
    }

}
