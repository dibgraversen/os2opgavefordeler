package dk.os2opgavefordeler.assigneesearch;

import dk.os2opgavefordeler.model.Employment;
import dk.os2opgavefordeler.model.OrgUnit;

import java.util.Optional;

public class Assignee {

    private OrgUnit orgUnit;
    private Employment employment;

    public Assignee(OrgUnit orgUnit) {
        this.orgUnit = orgUnit;
    }

    public Assignee(OrgUnit orgUnit, Employment employment) {

        this.orgUnit = orgUnit;
        this.employment = employment;
    }

    public OrgUnit getOrgUnit() {
        return orgUnit;
    }

    public Optional<Employment> getEmployment() {
        return Optional.ofNullable(employment);
    }
}
