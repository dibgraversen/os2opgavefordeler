package dk.os2opgavefordeler.model.presentation;

import dk.os2opgavefordeler.model.User;

public class UserInfoPO {
	public static final UserInfoPO INVALID = new UserInfoPO(-1, "Anonymous", false);

	private long id;
	private String name;
	private boolean loggedIn;

	public UserInfoPO(int id, String name, boolean isLoggedIn) {
		this.id = id;
		this.name = name;
		this.loggedIn = isLoggedIn;
	}

	public UserInfoPO(User user) {
		this.id = user.getId();
		this.name = user.getName();
		this.loggedIn = true;
	}

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public boolean isLoggedIn() {
		return loggedIn;
	}
}
