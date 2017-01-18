package dk.os2opgavefordeler.service;

import dk.os2opgavefordeler.model.Role;

public interface AuthorizationService {
	@Deprecated void verifyIsAdmin() throws AuthorizationException;
	@Deprecated void verifyCanActAs(Role role) throws AuthorizationException;
}
