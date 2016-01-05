package dk.os2opgavefordeler.service.impl;

import dk.os2opgavefordeler.model.Role;
import dk.os2opgavefordeler.service.AuthorizationException;
import dk.os2opgavefordeler.service.AuthorizationService;

public class AuthorizationServiceImpl implements AuthorizationService {
	@Override
	public void verifyIsAdmin() throws AuthorizationException {
//		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public void verifyCanActAs(Role role) throws AuthorizationException {
//		throw new UnsupportedOperationException("Not implemented yet");
	}
}
