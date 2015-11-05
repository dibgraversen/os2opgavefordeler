package dk.os2opgavefordeler.distribution;

import dk.os2opgavefordeler.model.CprDistributionRuleFilter;
import dk.os2opgavefordeler.model.DistributionRule;
import dk.os2opgavefordeler.model.DistributionRuleFilter;
import dk.os2opgavefordeler.model.OrgUnit;
import dk.os2opgavefordeler.service.OrgUnitService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class DistributionRuleController {

    @Inject
    private DistributionRuleRepository repository;

    @Inject
    private OrgUnitService orgUnitService;

    @Inject
    private EntityManager entityManager;

    public void createFilter(CprDistributionRuleFilterDTO dto) throws
            OrgUnitNotFoundException,
            DistributionRule.AlreadyHaveFilterWithNameException {

        DistributionRule rule = repository.findBy(dto.distributionRuleId);
        Optional<OrgUnit> orgUnit = orgUnitService.getOrgUnit(dto.assignedOrgId);

        if (!orgUnit.isPresent()) {
            throw new OrgUnitNotFoundException("Organizational unit not found");
        }

        CprDistributionRuleFilter filter = new CprDistributionRuleFilter();
        filter.setName(dto.name);
        filter.setMonths(dto.months);
        filter.setDays(dto.days);
        filter.setAssignedEmp(dto.assignedEmployeeId);
        filter.setAssignedOrg(orgUnit.get());
        filter.setDistributionRule(rule);

        rule.addFilter(filter);
        repository.save(rule);

    }

    public void deleteFilter(long distributionRuleId, String name) {
        DistributionRule rule = repository.findBy(distributionRuleId);
        rule.removeFiltersWithName(name);
        List<DistributionRuleFilter> filters = new ArrayList<>();

        for (DistributionRuleFilter filter : rule.getFilters()) {

            if (filter.getName().equals(name)) {
                filter.setDistributionRule(null);
            } else {
                filters.add(filter);
            }
        }

        rule.setFilters(filters);


        repository.save(rule);
    }

    public class OrgUnitNotFoundException extends Exception {
        public OrgUnitNotFoundException(String msg) {
            super(msg);
        }
    }

}
