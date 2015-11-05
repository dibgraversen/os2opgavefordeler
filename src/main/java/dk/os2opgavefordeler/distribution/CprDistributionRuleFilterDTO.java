package dk.os2opgavefordeler.distribution;

/**
 * DTO to transfer cpr distribution rules
 */
public class CprDistributionRuleFilterDTO {

    public long distributionRuleId;
    public long assignedOrgId;
    public long assignedEmployeeId;
    public String name;
    public String days;
    public String months;

}
