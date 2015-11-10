package dk.os2opgavefordeler.model.presentation;

import dk.os2opgavefordeler.model.Municipality;
import dk.os2opgavefordeler.model.User;

public class UserInfoPO {
	public static final UserInfoPO INVALID = new UserInfoPO(-1, "Anonymous", false, null);

	private long id;
	private String name;
	private boolean loggedIn;
	private Municipality municipality;

	public UserInfoPO(long id, String name, boolean isLoggedIn, Municipality municipality) {
		this.id = id;
		this.name = name;
		this.loggedIn = isLoggedIn;
		this.municipality = municipality;
	}

	public UserInfoPO(User user) {
		this.id = user.getId();
		this.name = user.getName();
		this.loggedIn = true;
		this.municipality = user.getMunicipality();
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

	public Municipality getMunicipality(){
		return municipality;
	}
}
