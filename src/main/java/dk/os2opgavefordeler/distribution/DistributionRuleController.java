package dk.os2opgavefordeler.distribution;

import dk.os2opgavefordeler.employment.EmploymentRepository;
import dk.os2opgavefordeler.employment.OrgUnitRepository;
import dk.os2opgavefordeler.model.*;
import org.apache.deltaspike.jpa.api.transaction.Transactional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
@Transactional
public class DistributionRuleController {

    @Inject
    private DistributionRuleRepository ruleRepository;

    @Inject
    private OrgUnitRepository orgUnitRepository;

    @Inject
    private EmploymentRepository employmentRepository;

    @Inject
    private EntityManager entityManager;

    @Inject
    private DistributionRuleFilterFactory filterFactory;

    public void createFilter(CprDistributionRuleFilterDTO dto) throws
            OrgUnitNotFoundException,
            RuleNotFoundException {

        DistributionRule rule = ruleRepository.findBy(dto.distributionRuleId);

        if (rule == null) {
            throw new RuleNotFoundException("No such rule" + dto.distributionRuleId);
        }

        OrgUnit orgUnit = orgUnitRepository.findBy(dto.assignedOrgId);
        if (orgUnit == null) {
            throw new OrgUnitNotFoundException("Organizational unit not found");
        }

        rule.addFilter(filterFactory.fromDto(dto));

        ruleRepository.save(rule);

    }

    public void updateFilter(long ruleId, long filterId, CprDistributionRuleFilterDTO dto) throws
            OrgUnitNotFoundException,
            RuleNotFoundException {
        DistributionRule rule = ruleRepository.findBy(ruleId);

        if (rule == null) {
            throw new RuleNotFoundException("No such rule" + dto.distributionRuleId);
        }

        OrgUnit orgUnit = orgUnitRepository.findBy(dto.assignedOrgId);
        if (orgUnit == null) {
            throw new OrgUnitNotFoundException("Organizational unit not found");
        }

        for(DistributionRuleFilter filter :  rule.getFilters()){

            CprDistributionRuleFilter f = (CprDistributionRuleFilter) filter;

            if(f.getId() != filterId){
                continue;
            }

            f.setName(dto.name);
            Employment e = employmentRepository.findBy(dto.assignedEmployeeId);
            if(e != null) {
                f.setAssignedEmployee(e);
            }
            OrgUnit o = orgUnitRepository.findBy(dto.assignedOrgId);
            if(o != null){
                f.setAssignedOrg(o);
            }

            f.setDays(dto.days);
            f.setMonths(dto.months);

            entityManager.merge(f);
        }


    }

    public void deleteFilter(long distributionRuleId, long filterId) {
        DistributionRule rule = ruleRepository.findBy(distributionRuleId);


        List<DistributionRuleFilter> filters = new ArrayList<>();

        for (DistributionRuleFilter filter : rule.getFilters()) {

            if (filter.getId() == filterId) {
                filter.setDistributionRule(null);
            } else {
                filters.add(filter);
            }
        }

        rule.setFilters(filters);
        ruleRepository.save(rule);
    }

    public class RuleNotFoundException extends Exception {
        public RuleNotFoundException(String msg) {
            super(msg);
        }

        public RuleNotFoundException(String msg, Throwable t) {
            super(msg, t);
        }
    }

    public class OrgUnitNotFoundException extends Exception {
        public OrgUnitNotFoundException(String msg) {
            super(msg);
        }

        public OrgUnitNotFoundException(String msg, Throwable t) {
            super(msg, t);
        }
    }

}
