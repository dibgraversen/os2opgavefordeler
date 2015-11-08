package dk.os2opgavefordeler.assigneesearch;

import dk.os2opgavefordeler.model.DistributionRule;
import dk.os2opgavefordeler.model.Employment;
import dk.os2opgavefordeler.model.OrgUnit;

import java.util.Optional;

public class Assignee {

    private DistributionRule rule;
    private OrgUnit orgUnit;
    private Employment employment;

    public Assignee(DistributionRule rule, OrgUnit orgUnit) {
        this.rule = rule;
        this.orgUnit = orgUnit;
    }

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
