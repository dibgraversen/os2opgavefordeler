package dk.os2opgavefordeler.model;

import com.google.common.base.MoreObjects;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * Defines ownership of / responsibility for a part of the KLE distribution tree + assignments.
 *
 * Would 'KleDistribution' be a better name?
 */
@Entity
public class DistributionRule implements Serializable {
	@Id @GeneratedValue(strategy = GenerationType.AUTO)
	private int id;

	@ManyToOne
	@JoinColumn
	private DistributionRule parent;

	@OneToMany(mappedBy = "parent", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private List<DistributionRule> children;


	//	@ManyToOne
//	private OrgUnit responsibleOrg;
	int responsibleOrg;


	// Optimization: instead of having a null responsibleOrg and having to look at parent(s), manage responsibleOrg and
	// isInherited all the way down the DistributionRole hierarchy. Consider/measure if necessary before implementing.
//	private boolean isInherited;


	@ManyToOne			// if we decide to let several municipalities share the base KLE entities.
//	@Column(nullable = false , unique = true) //TODO: add constraints when ready for them...
	private Kle kle;	// this can be a main group, subgroup or topic. Remodel model.kle, or "stringly typed" ref?

	// default assignment
//	@ManyToOne
//	private OrgUnit assignedOrg;
	private int assignedOrg;

//	@ManyToOne
//	private Employment assignedEmp;
	private int assignedEmp;

	public int getResponsibleOrg() {
		return responsibleOrg;
	}

	public void setResponsibleOrg(int responsibleOrg) {
		this.responsibleOrg = responsibleOrg;
	}

	public int getAssignedOrg() {
		return assignedOrg;
	}

	public void setAssignedOrg(int assignedOrg) {
		this.assignedOrg = assignedOrg;
	}

	public int getAssignedEmp() {
		return assignedEmp;
	}

	public void setAssignedEmp(int assignedEmp) {
		this.assignedEmp = assignedEmp;
	}
//	@OneToMany
//	private List<DistributionAssignmentRule> assignments; // once we add support for additional, rule-based assignments.

	public DistributionRule() {

	}

	public Optional<DistributionRule> getParent() {
		return Optional.ofNullable(parent);
	}

	public int getId() {
		return id;
	}

	public Kle getKle() {
		return kle;
	}

	public void setKle(Kle kle) {
		this.kle = kle;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this.getClass())
			.add("id", id)
			.add("responsibleOrg", responsibleOrg)
			.add("kle", kle)

			.toString();
	}
}
