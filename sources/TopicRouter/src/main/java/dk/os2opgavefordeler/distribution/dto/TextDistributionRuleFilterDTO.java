package dk.os2opgavefordeler.distribution.dto;

import dk.os2opgavefordeler.model.TextDistributionRuleFilter;

public class TextDistributionRuleFilterDTO extends DistributionRuleFilterDTO{

    private static final String FILTER_TYPE = "text";

    public TextDistributionRuleFilterDTO() {
        type = FILTER_TYPE;
    }

    public TextDistributionRuleFilterDTO(TextDistributionRuleFilter filter) {
        filterId = filter.getId();
        distributionRuleId = filter.getDistributionRule().getId();
        text = filter.getText();

        if (filter.getAssignedOrg() != null) {
            assignedOrgId = filter.getAssignedOrg().getId();
            assignedOrgName = filter.getAssignedOrg().getName();
        }

        if (filter.getAssignedEmployee() != null) {
            assignedEmployeeId = filter.getAssignedEmployee().getId();
            assignedEmployeeName = filter.getAssignedEmployee().getName();
        }

        name = filter.getName();
        type = FILTER_TYPE;
    }

}
