package dk.os2opgavefordeler.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

/**
 * @author hlo@miracle.dk
 */
@Entity
public class Role implements Serializable {
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@ManyToOne
	private User owner;

	private String name;

	@ManyToOne
	private Employment employment;

	private boolean manager;

	private boolean admin;

	private boolean municipalityAdmin;

	private boolean substitute;
	
	private boolean kleAssigner;

	public Role() {
	}

	public Role(Builder builder) {
		this.name = builder.name;
		this.employment = builder.employment;
		this.manager = builder.manager;
		this.admin = builder.admin;
		this.municipalityAdmin = builder.municipalityAdmin;
		this.substitute = builder.substitute;
		this.kleAssigner = builder.kleAssigner;
	}


	//--------------------------------------------------------------------------
	// Builder
	//--------------------------------------------------------------------------
	public static Builder builder() {
		return new Builder();
	}

	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	public static class Builder {
		private String name;
		private Employment employment;
		private boolean manager;
		private boolean admin;
		private boolean municipalityAdmin;
		private boolean substitute;
		private boolean kleAssigner;

		public Role build() {
			return new Role(this);
		}

		public Builder name(String name) {
			this.name = name;
			return this;
		}
		public Builder employment(Employment employment) {
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
		public Builder kleAssigner(boolean kleAssigner) {
			this.kleAssigner = kleAssigner;
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

	public Optional<Employment> getEmployment() {
		return Optional.ofNullable(employment);
	}

	public void setEmployment(Employment employment) {
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
				", kleAssigner=" + kleAssigner +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Role role = (Role) o;
		return Objects.equals(employment, role.employment) &&
			Objects.equals(manager, role.manager) &&
			Objects.equals(admin, role.admin) &&
			Objects.equals(municipalityAdmin, role.municipalityAdmin) &&
			Objects.equals(kleAssigner, role.kleAssigner) &&
			Objects.equals(substitute, role.substitute);
	}

	@Override
	public int hashCode() {
		return Objects.hash(employment, manager, admin, municipalityAdmin, substitute);
	}

	public void setKleAssigner(boolean kleAssigner) {
		this.kleAssigner = kleAssigner;
	}
	
	public boolean isKleAssigner() {
		return kleAssigner;
	}
}
