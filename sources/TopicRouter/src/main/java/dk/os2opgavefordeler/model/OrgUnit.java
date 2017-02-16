package dk.os2opgavefordeler.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;

import dk.os2opgavefordeler.model.presentation.KleAssignmentType;

@Entity
public class OrgUnit implements Serializable, IHasChildren<OrgUnit> {
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	private boolean isActive;

	@NotNull
	private String businessKey;

	private String name;
	private String email;
	private String esdhId;
	private String esdhLabel;
	private String phone;
	private String pNumber;

	@ManyToOne
	private OrgUnit parent;

	@OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
	private List<OrgUnit> children;

	@OneToOne
	private Employment manager;			// OneToOne since a person who's manager in multiple OrgUnits will result in multiple Employments.

	@OneToMany(mappedBy = "employedIn")
	private List<Employment> employees;

	@NotNull
	@ManyToOne(fetch = FetchType.EAGER)
	private Municipality municipality;

	@OneToMany(mappedBy = "assignedOrg", cascade = CascadeType.REMOVE)
	private List<DistributionRuleFilter> responsibleForFilters;

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private List<OuKleAssignment> kles = new ArrayList<>();

	public OrgUnit() {
		children = new ArrayList<>();
		employees = new ArrayList<>();
	}

	private OrgUnit(Builder builder) {
		this();
		this.isActive = builder.isActive;
		this.name = builder.name;
		this.esdhId = builder.esdhId;
		this.manager = builder.manager;
		this.businessKey = builder.businessKey;
		this.email = builder.email;
		this.phone = builder.phone;
		this.pNumber = builder.pNumber;
		if(builder.employees != null) {
			this.employees = builder.employees;
			this.employees.stream().forEach(emp -> emp.setEmployedIn(this));
		}

		if(builder.children != null) {
			this.children = builder.children;
			this.children.stream().forEach(child -> child.parent = this);
		}
		this.municipality = builder.municipality;
	}

	public void addEmployee(Employment employee) {
		if(!employees.contains(employee)) {
			employee.setEmployedIn(this);
			employees.add(employee);
		}
	}

	public void removeEmployee(Employment employee) {
		final int index = employees.indexOf(employee);

		if(index == -1) {
			// throw
		} else {
			employees.remove(index);
			employee.setEmployedIn(null);
		}
	}

	public boolean isActive(){
		return isActive;
	}

	public void setIsActive(boolean isActive){
		this.isActive = isActive;
	}


	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Optional<Employment> getManager() {
		return Optional.ofNullable(manager);
	}

	public void setManager(Employment manager) {
		this.manager = manager;
	}

	public String getBusinessKey() {
		return businessKey;
	}

	public void setBusinessKey(String businessKey) {
		this.businessKey = businessKey;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getEsdhId() {
		return esdhId;
	}

	public void setEsdhId(String esdhId) {
		this.esdhId = esdhId;
	}

	public String getEsdhLabel() {
		return esdhLabel;
	}

	public void setEsdhLabel(String esdhLabel) {
		this.esdhLabel = esdhLabel;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getpNumber() { return pNumber; }

	public void setpNumber(String pNumber) { this.pNumber = pNumber; }

	public Optional<OrgUnit> getParent() {
		return Optional.ofNullable(parent);
	}

	public void setParent(OrgUnit parent) {
		if(this.parent != null && this.parent != parent) {
			this.parent.children.remove(this);
		}
		this.parent = parent;
	}

	public ImmutableList<OrgUnit> getChildren() {
		List<OrgUnit> c = new ArrayList<>();
		for(OrgUnit child : children){
			if(!child.isActive){
				continue;
			}
			c.add(child);
		}
		return ImmutableList.copyOf(c);
	}

	public ImmutableList<Employment> getEmployees() {
		return ImmutableList.copyOf(employees);
	}

	public void setEmployees(List<Employment> employees) {
		this.employees = employees;
	}

	public Optional<Municipality> getMunicipality() {
		return Optional.ofNullable(municipality);
	}

	public void setMunicipality(Municipality municipality) {
		this.municipality = municipality;
	}

	public List<Kle> getKles(KleAssignmentType assignmentType) {
		return ImmutableList.copyOf(kles.stream().filter(x->x.getAssignmentType().equals(assignmentType)).map(OuKleAssignment::getKle).collect(Collectors.toList()));
	}

	public void addKle(Kle kle, KleAssignmentType assignmentType) {
		if (this.kles.stream().anyMatch(x->x.getAssignmentType().equals(assignmentType) && x.getKle().equals(kle))) {
			; // do nothing, it is already added
		} else {
			OuKleAssignment oka = new OuKleAssignment(this,kle,assignmentType);
			this.kles.add(oka);
		}
	}

	public void removeKle(Kle kle, KleAssignmentType assignmentType){
		this.kles.removeIf(x->x.getAssignmentType().equals(assignmentType) && x.getKle().equals(kle));
	}

	public boolean hasKles() {		
		return !kles.isEmpty();
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("id", id)
				.add("name", name)
				.add("email", email)
				.add("phone", phone)
				.add("pNumber", pNumber)
				.add("businessKey", businessKey)
				.add("municipality", municipality)
				.add("kles", kles)
				.toString();
	}

	//--------------------------------------------------------------------------
	// Builder
	//--------------------------------------------------------------------------
	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private boolean isActive;
		private String businessKey;
		private String name;
		private String email;
		private String esdhId;
		private String esdhLabel;
		private String phone;
		private String pNumber;

		private Employment manager;
		private List<Employment> employees = new ArrayList<>();
		private List<OrgUnit> children = new ArrayList<>();

		private Municipality municipality;

		public OrgUnit build() {
			return new OrgUnit(this);
		}

		public Builder isActive(boolean isActive) {
			this.isActive = isActive;
			return this;
		}
		public Builder businessKey(String businessKey) {
			this.businessKey = businessKey;
			return this;
		}
		public Builder name(String name) {
			this.name = name;
			return this;
		}
		public Builder email(String email) {
			this.email = email;
			return this;
		}
		public Builder esdhId(String esdhId) {
			this.esdhId = esdhId;
			return this;
		}
		public Builder esdhLabel(String esdhLabel) {
			return this;
		}
		public Builder phone(String phone) {
			this.phone = phone;
			return this;
		}
		public Builder pNumber(String pNumber){
			this.pNumber = pNumber;
			return this;
		}
		public Builder manager(Employment manager) {
			this.manager = manager;
			return this;
		}
		public Builder employees(Employment... employees) {
			Collections.addAll(this.employees, employees);
			return this;
		}
		public Builder children(OrgUnit... children) {
			Collections.addAll(this.children, children);
			return this;
		}

		public Builder municipality(Municipality municipality){
			this.municipality = municipality;
			return this;
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		OrgUnit orgUnit = (OrgUnit) o;

		if (id != orgUnit.id) return false;
		if (businessKey != null ? !businessKey.equals(orgUnit.businessKey) : orgUnit.businessKey != null) return false;
		return municipality != null ? municipality.equals(orgUnit.municipality) : orgUnit.municipality == null;
	}

	@Override
	public int hashCode() {
		int result = (int) (id ^ (id >>> 32));
		result = 31 * result + (businessKey != null ? businessKey.hashCode() : 0);
		result = 31 * result + (municipality != null ? municipality.hashCode() : 0);
		return result;
	}

}
