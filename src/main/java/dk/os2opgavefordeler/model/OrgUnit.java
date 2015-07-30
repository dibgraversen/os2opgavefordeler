package dk.os2opgavefordeler.model;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Entity
public class OrgUnit implements Serializable, IHasChildren<OrgUnit> {
	@Id @GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	private boolean isActive;

	@NotNull
	private String businessKey;

	private String name;
	private String email;
	private String esdhId;
	private String esdhLabel;
	private String phone;

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
		return ImmutableList.copyOf(children);
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

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("id", id)
			.add("name", name)
			.add("municipality", municipality)
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
}
