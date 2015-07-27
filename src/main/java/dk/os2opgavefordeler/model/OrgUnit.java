package dk.os2opgavefordeler.model;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Entity
public class OrgUnit implements Serializable, IHasChildren<OrgUnit>
{
	@Id @GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	private boolean isActive;

	private String businessKey;
	private String name;
	private String email;
	private String esdhId;
	private String esdhLabel;
	private String phone;

	@ManyToOne
	private OrgUnit parent;

	@OneToMany(mappedBy = "parent", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private List<OrgUnit> children;

	@OneToOne(cascade = CascadeType.ALL)
	private Employment manager;			// OneToOne since a person who's manager in multiple OrgUnits will result in multiple Employments.

	@OneToMany(mappedBy = "employedIn", cascade = CascadeType.ALL)
	private List<Employment> employees;


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

	public Optional<Employment> getManager() {
		return Optional.ofNullable(manager);
	}

	public String getBusinessKey() {
		return businessKey;
	}

	public String getName() {
		return name;
	}

	public String getEmail() {
		return email;
	}

	public String getEsdhId() {
		return esdhId;
	}

	public String getEsdhLabel() {
		return esdhLabel;
	}

	public String getPhone() {
		return phone;
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

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("id", id)
			.add("name", name)
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
	}
}
