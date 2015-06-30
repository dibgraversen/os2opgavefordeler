package dk.os2opgavefordeler.model;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author hlo@miracle.dk
 */
@Entity
public class Role implements Serializable {
	@Id @GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@ManyToOne
	private User owner;

	private String name;

	private int employment;

	private boolean manager;

	private boolean admin;

	private boolean municipalityAdmin;

	private boolean substitute;

	public Role() {
	}

	public Role(Builder builder) {
		this.name = builder.name;
		this.employment = builder.employment;
		this.manager = builder.manager;
		this.admin = builder.admin;
		this.municipalityAdmin = builder.municipalityAdmin;
		this.substitute = builder.substitute;
	}


	//--------------------------------------------------------------------------
	// Builder
	//--------------------------------------------------------------------------
	public static Builder builder() {
		return new Builder();
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	public static class Builder {
		private String name;
		private int employment;
		private boolean manager;
		private boolean admin;
		private boolean municipalityAdmin;
		private boolean substitute;

		public Role build() {
			return new Role(this);
		}

		public Builder name(String name) {
			this.name = name;
			return this;
		}
		public Builder employment(int employment) {
			this.employment = employment;
			return this;
		}
		public Builder manager(boolean manager) {
			this.manager = manager;
			return this;
		}
		public Builder admin(boolean admin) {
			this.admin = admin;
			return this;
		}
		public Builder municipalityAdmin(boolean municipalityAdmin) {
			this.municipalityAdmin = municipalityAdmin;
			return this;
		}
		public Builder substitute(boolean substitute) {
			this.substitute = substitute;
			return this;
		}
	}


	//--------------------------------------------------------------------------
	// Getter/setters
	//--------------------------------------------------------------------------
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getUserId() {
		return owner.getId();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getEmployment() {
		return employment;
	}

	public void setEmployment(int employment) {
		this.employment = employment;
	}

	public boolean isManager() {
		return manager;
	}

	public void setManager(boolean manager) {
		this.manager = manager;
	}

	public boolean isAdmin() {
		return admin;
	}

	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

	public boolean isMunicipalityAdmin() {
		return municipalityAdmin;
	}

	public void setMunicipalityAdmin(boolean municipalityAdmin) {
		this.municipalityAdmin = municipalityAdmin;
	}

	public boolean isSubstitute() {
		return substitute;
	}

	public void setSubstitute(boolean substitute) {
		this.substitute = substitute;
	}


	//--------------------------------------------------------------------------
	// toString
	//--------------------------------------------------------------------------
	@Override
	public String toString() {
		return "Role{" +
				"id=" + id +
				", name='" + name + '\'' +
				", employment=" + employment +
				", manager=" + manager +
				", admin=" + admin +
				", municipalityAdmin=" + municipalityAdmin +
				", substitute=" + substitute +
				'}';
	}
}
