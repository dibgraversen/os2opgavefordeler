package dk.os2opgavefordeler.model.presentation;

import dk.os2opgavefordeler.model.IdentityProvider;

public class IdentityProviderPO {
	private long id;
	private String name;

	public IdentityProviderPO(IdentityProvider source) {
		this.id = source.getId();
		this.name = source.getName();
	}

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}
}
