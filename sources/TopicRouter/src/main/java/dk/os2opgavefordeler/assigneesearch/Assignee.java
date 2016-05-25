package dk.os2opgavefordeler.assigneesearch;

import java.util.Optional;

import dk.os2opgavefordeler.model.DistributionRule;
import dk.os2opgavefordeler.model.Employment;
import dk.os2opgavefordeler.model.OrgUnit;

/**
 * Convenience class for an assignee
 */
public class Assignee {

    private DistributionRule rule;
    private OrgUnit orgUnit;
    private Employment employment;

	/**
	 * Creates a new assignee with the specified rule and organisational unit
	 *
	 * @param rule the rule that the assignee is assigned to
	 * @param orgUnit the organisational unit that the assignee belongs to
	 */
    public Assignee(DistributionRule rule, OrgUnit orgUnit) {
        this.rule = rule;
        this.orgUnit = orgUnit;
    }

	/**
	 * Creates a new assignee with the specified rule, organisational unit and employment
	 *
	 * @param rule the rule that the assignee is assigned to
	 * @param orgUnit the organisational unit that the assignee belongs to
	 * @param employment the employment that the assignee has
	 */
    public Assignee(DistributionRule rule, OrgUnit orgUnit, Employment employment) {
        this.rule = rule;
        this.orgUnit = orgUnit;
        this.employment = employment;
    }

    public OrgUnit getOrgUnit() {
        return orgUnit;
    }

    public Optional<Employment> getEmployment() {
        return Optional.ofNullable(employment);
    }

    public DistributionRule getRule(){
        return rule;
    }
}
