package dk.os2opgavefordeler.auth;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.deltaspike.security.api.authorization.Secures;
import org.slf4j.Logger;

/**
 * Created by rro on 01-02-2017.
 */
@ApplicationScoped
public class AdminAuthorizer {

	@Inject
	private AuthService authService;

	@Inject
	Logger logger;

	@Secures
	@AdminRequired
	public boolean doAdminCheck() throws Exception {
		return authService.isAdmin();
	}
}
