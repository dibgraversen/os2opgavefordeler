package dk.os2opgavefordeler.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

/**
 * Defines ownership of / responsibility for a part of the KLE distribution tree + assignments.
 *
 * Would 'KleDistribution' be a better name?
 */
@Entity
public class DistributionRule implements Serializable {
	@Id @GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@ManyToOne
	private OrgUnit responsibleOrg;

	// Optimization: instead of having a null responsibleOrg and having to look at parent(s), manage responsibleOrg and
	// isInherited all the way down the DistributionRole hierarchy. Consider/measure if necessary before implementing.
//	private boolean isInherited;


//	Embedded?			if we decide to fully instantiate per municipality
//	ManyToOne?			if we decide to let several municipalities share the base KLE entities.
//	private Kle kle;	//	this can be a main group, subgroup or topic. Remodel model.kle, or "stringly typed" ref?

	@ManyToOne
	private DistributionRule parentRule;

	@OneToMany
	private List<DistributionRule> childRules;

	@OneToMany
	private List<DistributionAssignment> assignments;



	public DistributionRule() {

	}
}
