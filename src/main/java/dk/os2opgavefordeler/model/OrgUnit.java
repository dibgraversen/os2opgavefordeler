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
	private int id;

	private boolean isActive;

	private String name;
	private String esdhId;
	private String email;
	private String phone;

	@ManyToOne
	private OrgUnit parent;

	@OneToMany(mappedBy = "parent", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private List<OrgUnit> children;

//	@OneToOne
//	private Employment manager;			// OneToOne since a person who's manager in multiple OrgUnits will result in multiple Employments.
	private int manager;

//	@OneToMany
//	private List<Employment> employees;


	public OrgUnit() {
	}

	private OrgUnit(Builder builder) {
		this();
		this.isActive = builder.isActive;
		this.name = builder.name;
		this.esdhId = builder.esdhId;
		this.manager = builder.manager;
//		this.employees = builder.employees;		//TODO: set employedIn if we end up needing that field.

		if(builder.children != null) {
			this.children = builder.children;
			children.stream().forEach(child -> child.parent = this);
		}
	}

	public int getId() {
		return id;
	}

	public int getManager() {
		return manager;
	}

	public String getName() {
		return name;
	}

	public String getEmail() {
		return email;
	}

	public String getPhone() {
		return phone;
	}

	public String getEsdhId() {
		return esdhId;
	}


	public Optional<OrgUnit> getParent() {
		return Optional.ofNullable(parent);
	}

	public ImmutableList<OrgUnit> getChildren() {
		return ImmutableList.copyOf(children);
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
		private String name;
		private String esdhId;
		private String email;
		private String phone;

		private int manager;
		private List<Employment> employees;
		private List<OrgUnit> children = new ArrayList<>();

		public OrgUnit build() {
			return new OrgUnit(this);
		}

		public Builder isActive(boolean isActive) {
			this.isActive = isActive;
			return this;
		}
		public Builder name(String name) {
			this.name = name;
			return this;
		}
		public Builder esdhId(String esdhId) {
			this.esdhId = esdhId;
			return this;
		}
		public Builder manager(int manager) {
			this.manager = manager;
			return this;
		}
		public Builder employees(List<Employment> employees) {
			this.employees = employees;
			return this;
		}
		public Builder children(OrgUnit... children) {
			Collections.addAll(this.children, children);
			return this;
		}
		public Builder email(String email) {
			this.email = email;
			return this;
		}
		public Builder phone(String phone) {
			this.phone = phone;
			return this;
		}
	}
}
