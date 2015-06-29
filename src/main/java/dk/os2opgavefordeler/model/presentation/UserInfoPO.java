package dk.os2opgavefordeler.model.presentation;

import dk.os2opgavefordeler.model.User;

public class UserInfoPO {
	public static final UserInfoPO INVALID = new UserInfoPO(-1, false);

	private boolean loggedIn;
	private int id;


	public int getId() {
		return id;
	}

	public boolean isLoggedIn() {
		return loggedIn;
	}

	public UserInfoPO(int id, boolean isLoggedIn) {
		this.id = id;
		this.loggedIn = isLoggedIn;
	}

	public UserInfoPO(User user) {
		this.id = user.getId();
		this.loggedIn = true;
	}

}
