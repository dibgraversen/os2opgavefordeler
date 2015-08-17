package dk.os2opgavefordeler.model;

import com.google.common.base.MoreObjects;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Defines ownership of / responsibility for a part of the KLE distribution tree + assignments.
 *
 * Would 'KleDistribution' be a better name?
 */
@Entity
public class DistributionRule implements Serializable {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@ManyToOne
	@JoinColumn
	private DistributionRule parent;

	@OneToMany(mappedBy = "parent", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private List<DistributionRule> children;


	@ManyToOne
	private OrgUnit responsibleOrg;


	// Optimization: instead of having a null responsibleOrg and having to look at parent(s), manage responsibleOrg and
	// isInherited all the way down the DistributionRole hierarchy. Consider/measure if necessary before implementing.
//	private boolean isInherited;


	@ManyToOne			// if we decide to let several municipalities share the base KLE entities.
//	@Column(nullable = false , unique = true) //TODO: add constraints when ready for them...
	private Kle kle;	// this can be a main group, subgroup or topic. Remodel model.kle, or "stringly typed" ref?

	// default assignment
	@ManyToOne
	private OrgUnit assignedOrg;

//	@ManyToOne
//	private Employment assignedEmp;
	private long assignedEmp;

	@NotNull
	@ManyToOne(fetch = FetchType.EAGER)
	private Municipality municipality;

	public Optional<OrgUnit> getResponsibleOrg() {
		return Optional.ofNullable(responsibleOrg);
	}

	public void setResponsibleOrg(OrgUnit responsibleOrg) {
		this.responsibleOrg = responsibleOrg;
	}

	public Optional<OrgUnit> getAssignedOrg() {
		return Optional.ofNullable(assignedOrg);
	}

	public void setAssignedOrg(OrgUnit assignedOrg) {
		this.assignedOrg = assignedOrg;
	}

	public long getAssignedEmp() {
		return assignedEmp;
	}

	public void setAssignedEmp(long assignedEmp) {
		this.assignedEmp = assignedEmp;
	}
//	@OneToMany
//	private List<DistributionAssignmentRule> assignments; // once we add support for additional, rule-based assignments.

	public DistributionRule() {

	}

	public DistributionRule(Builder builder) {
		this();
		this.responsibleOrg = builder.responsibleOrg;
		this.kle = builder.kle;
		this.municipality = builder.municipality;
		if(!builder.children.isEmpty()) {
			this.children = builder.children;
			children.stream().forEach(child -> child.parent = this);
		}
	}

	public Optional<DistributionRule> getParent() {
		return Optional.ofNullable(parent);
	}

	public void setParent(DistributionRule parent) {
		this.parent = parent;
	}

	public long getId() {
		return id;
	}

	public Kle getKle() {
		return kle;
	}

	public void setKle(Kle kle) {
		this.kle = kle;
	}

	public Municipality getMunicipality() {
		return municipality;
	}

	public void setMunicipality(Municipality municipality) {
		this.municipality = municipality;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this.getClass())
			.add("id", id)
			.add("responsibleOrg", responsibleOrg)
			.add("kle", kle)

			.toString();
	}

	//--------------------------------------------------------------------------
	// Builder
	//--------------------------------------------------------------------------
	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private OrgUnit responsibleOrg = null;
		private Kle kle = null;
		private Municipality municipality;
		private List<DistributionRule> children = new ArrayList<>();


		public DistributionRule build() {
			return new DistributionRule(this);
		}

		public Builder responsibleOrg(OrgUnit responsibleOrg) {
			this.responsibleOrg = responsibleOrg;
			return this;
		}

		public Builder kle(Kle kle) {
			this.kle = kle;
			return this;
		}

		public Builder municipality(Municipality municipality){
			this.municipality = municipality;
			return this;
		}

		public Builder children(DistributionRule... children) {
			Collections.addAll(this.children, children);
			return this;
		}
	}
}
