package dk.os2opgavefordeler.auth.openid;

import com.google.common.base.Strings;
import com.nimbusds.oauth2.sdk.id.State;
import dk.os2opgavefordeler.employment.UserRepository;
import dk.os2opgavefordeler.model.IdentityProvider;
import dk.os2opgavefordeler.model.User;
import dk.os2opgavefordeler.model.presentation.IdentityProviderPO;
import dk.os2opgavefordeler.service.*;
import dk.os2opgavefordeler.service.oidc.OpenIdConnect;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class OpenIdAuthenticationFlowImpl implements OpenIdAuthenticationFlow {
	@Inject
	private Logger log;

	@Inject
	private UserService userService;

	@Inject
	private UserRepository userRepository;

	@Inject
	private OpenIdConnect openIdConnect;

	@Inject
	private OpenIdUserFactory openIdUserFactory;

	@Override
	public String generateCsrfToken() {
		return new State().toString();
	}

	@Override
	public URI beginAuthenticationFlow(IdentityProvider idp, String token, String callbackUrl)
	throws AuthenticationException
	{
		return openIdConnect.beginAuthenticationFlow(idp, token, callbackUrl);
	}

	@Override
	public User finalizeAuthenticationFlow(IdentityProvider idp, String token, String callbackUrl, URI requestUri)
	throws AuthenticationException
	{
		final String email = openIdConnect.finalizeAuthenticationFlow(idp, token, callbackUrl, requestUri);
		log.info("finalizeAuthenticationFlow: email is {}", email);

		if(Strings.isNullOrEmpty(email)) {
			throw new AuthenticationException("IDP returned empty email claim");
		}

		return findOrCreateUserFromEmail(email);
	}

	@Override
	public User findOrCreateUserFromEmail(String email) {
		return userService.findByEmail(email)
			.map(user -> {
				log.info("User found by email, returning");
				return user;
			})
			.orElseGet(() -> {
				log.info("User not found, creating");
				return openIdUserFactory.createUserFromOpenIdEmail(email);
			});
	}
}
