package dk.os2opgavefordeler.model.presentation;

public class SubstitutePO {
	private String userName;
	private long roleId;

	public SubstitutePO(String userName, long roleId) {
		this.userName = userName;
		this.roleId = roleId;
	}

	public String getUserName() {
		return userName;
	}

	public long getRoleId() {
		return roleId;
	}
}
