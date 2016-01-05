package dk.os2opgavefordeler.service;

import dk.os2opgavefordeler.model.Role;

public interface AuthorizationService {
	void verifyIsAdmin() throws AuthorizationException;
	void verifyCanActAs(Role role) throws AuthorizationException;
}
