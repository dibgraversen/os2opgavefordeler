package dk.os2opgavefordeler.distribution;

import dk.os2opgavefordeler.model.CprDistributionRuleFilter;

/**
 * DTO to transfer cpr distribution rules
 */
public class CprDistributionRuleFilterDTO {

    public String type = "cpr";
    public long filterId;
    public long distributionRuleId;
    public long assignedOrgId;
    public String assignedOrgName;
    public long assignedEmployeeId;
    public String assignedEmployeeName;
    public String name;
    public String days;
    public String months;


    public CprDistributionRuleFilterDTO() {
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
    }

}
