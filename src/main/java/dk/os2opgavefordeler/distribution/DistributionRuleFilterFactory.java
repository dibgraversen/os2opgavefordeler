package dk.os2opgavefordeler.distribution;

import dk.os2opgavefordeler.employment.EmploymentRepository;
import dk.os2opgavefordeler.employment.OrgUnitRepository;
import dk.os2opgavefordeler.model.CprDistributionRuleFilter;
import dk.os2opgavefordeler.model.DistributionRule;
import dk.os2opgavefordeler.model.DistributionRuleFilter;
import dk.os2opgavefordeler.service.OrgUnitService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;

@ApplicationScoped
public class DistributionRuleFilterFactory {

    @Inject
    private EntityManager entityManager;

    @Inject
    private OrgUnitRepository orgUnitRepository;

    @Inject
    private EmploymentRepository employmentRepository;

    @Inject
    private DistributionRuleRepository repository;

    public DistributionRuleFilter fromDto(CprDistributionRuleFilterDTO dto) {
        // Try to create from existing
        DistributionRule rule = repository.findBy(dto.distributionRuleId);

        CprDistributionRuleFilter filter = new CprDistributionRuleFilter();

        if (rule.getFilterByName(dto.name) != null) {
            filter = (CprDistributionRuleFilter) rule.getFilterByName(dto.name);
        }

        filter.setName(dto.name);
        filter.setMonths(dto.months);
        filter.setDays(dto.days);
        filter.setAssignedEmployee(employmentRepository.findBy(dto.assignedEmployeeId));
        filter.setAssignedOrg(orgUnitRepository.findBy(dto.assignedOrgId));
        filter.setDistributionRule(rule);

        return filter;
    }

}
