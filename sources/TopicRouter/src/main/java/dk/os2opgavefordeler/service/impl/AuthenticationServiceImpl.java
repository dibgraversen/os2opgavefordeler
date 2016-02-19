package dk.os2opgavefordeler.service.impl;

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
public class AuthenticationServiceImpl implements AuthenticationService {
	@Inject
	private Logger log;

	@Inject
	private UserService userService;

	@Inject
	private UserRepository userRepository;

	@Inject
	private OpenIdConnect openIdConnect;

	static private final Map<Long, IdentityProvider> providers = new HashMap<>();
	static {
		providers.put(1L, IdentityProvider.builder()
			.id(1).name("Google account")
			.url("https://accounts.google.com/")
			.clientId("89170361789-mg8l3t3f11vo0cf0hce4h85epi0qqq3q.apps.googleusercontent.com")
			.clientSecret("itCIp2JGR2NKBAu4Se9LCAjp")
			.build()
		);
		providers.put(2L, IdentityProvider.builder()
			.id(2).name("Kitos SSO")
			.url("https://os2saml.syddjurs.dk/")
			.clientId("suneclient")
			.clientSecret("secret")
			.build()
		);
	}

	@Override
	public String generateCsrfToken() {
		return new State().toString();
	}

	@Override
	public Optional<IdentityProvider> findProvider(long id) {
		return Optional.ofNullable(providers.get(id));
	}

	@Override
	public List<IdentityProvider> identityProviderList() {
		return new ArrayList(providers.values());
	}

	@Override
	public List<IdentityProviderPO> identityProviderPOList() {
		return identityProviderList().stream()
			.map(IdentityProviderPO::new)
			.collect(Collectors.toList());
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
				return userService.createUserFromOpenIdEmail(email);
			});
	}
}