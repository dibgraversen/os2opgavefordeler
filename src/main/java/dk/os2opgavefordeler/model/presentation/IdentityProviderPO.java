package dk.os2opgavefordeler.model.presentation;

import dk.os2opgavefordeler.model.IdentityProvider;

public class IdentityProviderPO {
	private int id;
	private String name;

	public IdentityProviderPO(IdentityProvider source) {
		this.id = source.getId();
		this.name = source.getName();
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}
}
