package dk.os2opgavefordeler.distribution.dto;

import dk.os2opgavefordeler.model.TextDistributionRuleFilter;

public class TextDistributionRuleFilterDTO extends DistributionRuleFilterDTO{

    public static String TYPE = "text";

    public TextDistributionRuleFilterDTO() {
        type = TYPE;
    }

    public TextDistributionRuleFilterDTO(TextDistributionRuleFilter filter) {
        filterId = filter.getId();
        distributionRuleId = filter.getDistributionRule().getId();
        text = filter.getText();
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
