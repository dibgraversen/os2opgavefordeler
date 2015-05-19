package dk.os2opgavefordeler.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
public class OrgUnit implements Serializable {
	@Id @GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	private boolean isActive;
	private String name;
	private String esdhId;

	@ManyToOne
	private OrgUnit parent;

	@OneToMany
	private List<OrgUnit> subOrgs;

	@OneToOne
	private Employment manager;			// OneToOne since a person who's manager in multiple OrgUnits will result in multiple Employments.

	@OneToMany
	private List<Employment> employees;


	public OrgUnit() {
	}

	private OrgUnit(Builder builder) {
		this();
		this.isActive = builder.isActive;
		this.name = builder.name;
		this.esdhId = builder.esdhId;
		this.employees = builder.employees;
	}

	public static Builder builder() {
		return new Builder();
	}


	public static class Builder {
		private boolean isActive;
		private String name;
		private String esdhId;
		private List<Employment> employees;

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
		public Builder employees(List<Employment> employees) {
			this.employees = employees;
			return this;
		}
	}
}
