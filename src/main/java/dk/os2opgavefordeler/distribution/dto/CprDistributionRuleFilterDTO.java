package dk.os2opgavefordeler.distribution.dto;

import dk.os2opgavefordeler.model.CprDistributionRuleFilter;

/**
 * DTO to transfer cpr distribution rules
 */
public class CprDistributionRuleFilterDTO extends DistributionRuleFilterDTO{

    public static String TYPE = "cpr";

    public CprDistributionRuleFilterDTO() {
        type = TYPE;
    }

    public CprDistributionRuleFilterDTO(CprDistributionRuleFilter filter) {
        filterId = filter.getId();
        distributionRuleId = filter.getDistributionRule().getId();
        days = filter.getDays();
        months = filter.getMonths();
        if(filter.getAssignedOrg() != null) {
            assignedOrgId = filter.getAssignedOrg().getId();
            assignedOrgName = filter.getAssignedOrg().getName();
        }
        if(filter.getAssignedEmployee() != null) {
            assignedEmployeeId = filter.getAssignedEmployee().getId();
            assignedEmployeeName = filter.getAssignedEmployee().getName();
        }
        name = filter.getName();
        type = TYPE;
    }

}
