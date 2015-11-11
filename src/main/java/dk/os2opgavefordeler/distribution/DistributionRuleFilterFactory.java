package dk.os2opgavefordeler.distribution;

import dk.os2opgavefordeler.distribution.dto.CprDistributionRuleFilterDTO;
import dk.os2opgavefordeler.distribution.dto.DistributionRuleFilterDTO;
import dk.os2opgavefordeler.distribution.dto.TextDistributionRuleFilterDTO;
import dk.os2opgavefordeler.employment.EmploymentRepository;
import dk.os2opgavefordeler.employment.OrgUnitRepository;
import dk.os2opgavefordeler.model.CprDistributionRuleFilter;
import dk.os2opgavefordeler.model.DistributionRule;
import dk.os2opgavefordeler.model.DistributionRuleFilter;
import dk.os2opgavefordeler.model.TextDistributionRuleFilter;

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

    public DistributionRuleFilter fromDto(DistributionRuleFilterDTO dto) {
        // Try to create from existing
        DistributionRule rule = repository.findBy(dto.distributionRuleId);

        if (CprDistributionRuleFilterDTO.TYPE.equals(dto.type)) {

            CprDistributionRuleFilter filter = new CprDistributionRuleFilter();

            if (rule.getFilterById(dto.filterId) != null) {
                filter = (CprDistributionRuleFilter) rule.getFilterById(dto.filterId);
            }

            filter.setMonths(dto.months);
            filter.setDays(dto.days);

            filter.setName(dto.name);
            filter.setAssignedEmployee(employmentRepository.findBy(dto.assignedEmployeeId));
            filter.setAssignedOrg(orgUnitRepository.findBy(dto.assignedOrgId));
            filter.setDistributionRule(rule);

            return filter;
        } else if (TextDistributionRuleFilterDTO.TYPE.equals(dto.type)) {

            TextDistributionRuleFilter filter = new TextDistributionRuleFilter();

            if (rule.getFilterById(dto.filterId) != null) {
                filter = (TextDistributionRuleFilter) rule.getFilterById(dto.filterId);
            }

            filter.setText(dto.text);
            filter.setName(dto.name);
            filter.setAssignedEmployee(employmentRepository.findBy(dto.assignedEmployeeId));
            filter.setAssignedOrg(orgUnitRepository.findBy(dto.assignedOrgId));
            filter.setDistributionRule(rule);


        }
        throw new RuntimeException("Bad data");
    }

}
