package dk.os2opgavefordeler.auth;

import org.apache.deltaspike.security.api.authorization.Secures;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * @author hlo@miracle.dk
 */
@ApplicationScoped
public class LoggedInAuthorizer {

	@Inject
	private AuthService authService;

	@Inject
	Logger logger;

	@Secures
	@UserLoggedIn
	public boolean doSecuredCheck() throws Exception {
		return authService.isAuthenticated();
	}
}
