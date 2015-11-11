package dk.os2opgavefordeler.model;

import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Map;

@Entity
public abstract class DistributionRuleFilter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    private DistributionRule distributionRule;

    @NotBlank
    private String name;

    @ManyToOne
    private OrgUnit assignedOrg;

    @ManyToOne()
    @JoinColumn(name = "assignedEmp")
    private Employment assignedEmp;

    public DistributionRuleFilter() {

    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public DistributionRule getDistributionRule() {
        return distributionRule;
    }

    public void setDistributionRule(DistributionRule distributionRule) {
        this.distributionRule = distributionRule;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public OrgUnit getAssignedOrg() {
        return assignedOrg;
    }

    public void setAssignedOrg(OrgUnit assignedOrg) {
        this.assignedOrg = assignedOrg;
    }

    public Employment getAssignedEmployee() {
        return assignedEmp;
    }

    public void setAssignedEmployee(Employment assignedEmp) {
        this.assignedEmp = assignedEmp;
    }

    public abstract boolean matches(Map<String, String> parameters);

}
