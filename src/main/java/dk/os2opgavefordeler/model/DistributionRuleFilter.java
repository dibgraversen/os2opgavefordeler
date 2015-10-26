package dk.os2opgavefordeler.model;

import javax.persistence.*;
import java.io.Serializable;

@Entity(name = "distribution_rule_filter")
public class DistributionRuleFilter implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String name;

}
