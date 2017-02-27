package dk.os2opgavefordeler.model.presentation;

import dk.os2opgavefordeler.model.Municipality;
import dk.os2opgavefordeler.model.Role;
import dk.os2opgavefordeler.model.User;

/**
 * Presentation object used for user management
 */
public class UserRolePO {

	/*
	* Municipality information
	 */
	private Municipality municipality;

	/*
	* User information
	 */
	private long userId;

	private String name;
	private String email;

	/*
	* Role information
	 */
	private boolean roleSet;

	private long roleId;

	private boolean admin;
	private boolean municipalityAdmin;
	private boolean manager;
	private boolean kleAssigner;

	/**
	 * Creates a UserRolePO object with values set from the specified user and role
	 *
	 * @param user user to get values from
	 * @param role role to get values from
	 */
	public UserRolePO(User user, Role role) {
		this.municipality = user.getMunicipality();

		this.userId = user.getId();
		this.name = user.getName();
		this.email = user.getEmail();

		if (role != null) {
			this.roleSet = true;
			this.roleId = role.getId();
			this.admin = role.isAdmin();
			this.municipalityAdmin = role.isMunicipalityAdmin();
			this.manager = role.isManager();
			this.setKleAssigner(role.isKleAssigner());
		}
		else {
			this.roleSet = false;
		}
	}

	public Municipality getMunicipality() {
		return municipality;
	}

	public long getUserId() {
		return userId;
	}

	public String getName() {
		return name;
	}

	public String getEmail() {
		return email;
	}

	public boolean isRoleSet() {
		return roleSet;
	}

	public long getRoleId() {
		return roleId;
	}

	public boolean isAdmin() {
		return admin;
	}

	public boolean isMunicipalityAdmin() {
		return municipalityAdmin;
	}

	public boolean isManager() {
		return manager;
	}

	public boolean isKleAssigner() {
		return kleAssigner;
	}

	public void setKleAssigner(boolean kleAssigner) {
		this.kleAssigner = kleAssigner;
	}
}
