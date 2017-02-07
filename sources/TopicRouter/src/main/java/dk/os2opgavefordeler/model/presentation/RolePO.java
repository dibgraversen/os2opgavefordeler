package dk.os2opgavefordeler.model.presentation;

import dk.os2opgavefordeler.model.Role;

/**
 * @author hlo@miracle.dk
 */
public class RolePO {
	/**
	 * Id of the role.
	 */
	private long id;

	/**
	 * Specifies the user whom this object belongs to.
	 */
	private long userId;

	/**
	 * Name of the person with the role.
	 */
	private String name;

	/**
	 * This identifies the employment to which the role is connected.
	 * Can be blank.
	 */
	private long employment;

	/**
	 * Whether or not the role is for a manager.
	 */
	private boolean manager;

	/**
	 * Specifies whether this role enables system admin features.
	 */
	private boolean admin;

	/**
	 * Specifies whether this role enables admin features for a municipality.
	 */
	private boolean municipalityAdmin;
	
	/**
	 * Does the role allow assigning KLE values to OrgUnits
	 */
	private boolean kleAssigner;

	/**
	 * Specifies whether this role is a substitute role for another employment.
	 */
	private boolean substitute;

	public RolePO() {
	}

	public RolePO(Role role) {
		id = role.getId();
		userId = role.getUserId();
		name = role.getName();
		employment = role.getEmployment().map(e -> e.getId()).orElse(-1L);
		manager = role.isManager();
		admin = role.isAdmin();
		municipalityAdmin = role.isMunicipalityAdmin();
		substitute = role.isSubstitute();
		kleAssigner = role.isKleAssigner();
	}

	private RolePO(Builder builder) {
		this();
		id = builder.id;
		userId = builder.userId;
		name = builder.name;
		employment = builder.employment;
		manager = builder.manager;
		admin = builder.admin;
		municipalityAdmin = builder.municipalityAdmin;
		substitute = builder.substitute;
		kleAssigner = builder.kleAssigner;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getEmployment() {
		return employment;
	}

	public void setEmployment(long employment) {
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
	
	public void setKleAssigner(boolean kleAssigner) {
		this.kleAssigner = kleAssigner;
	}
	
	public boolean isKleAssigner() {
		return kleAssigner;
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

	@Override
	public String toString() {
		return "RolePO{" +
				"id=" + id +
				", userId=" + userId +
				", name='" + name + '\'' +
				", employment=" + employment +
				", manager=" + manager +
				", admin=" + admin +
				", municipalityAdmin=" + municipalityAdmin +
				", substitute=" + substitute +
				", kleAssigner=" + kleAssigner +
				'}';
	}

	//--------------------------------------------------------------------------
	// Builder
	//--------------------------------------------------------------------------
	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private long id;
		private long userId;
		private String name;
		private long employment;
		private boolean manager;
		private boolean admin;
		private boolean municipalityAdmin;
		private boolean substitute;
		private boolean kleAssigner;

		public RolePO build() { return new RolePO(this); }

		public Builder id(long id) {
			this.id = id;
			return this;
		}

		public Builder userId(long userId) {
			this.userId = userId;
			return this;
		}

		public Builder name(String name) {
			this.name = name;
			return this;
		}

		public Builder employment(long employment) {
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
}