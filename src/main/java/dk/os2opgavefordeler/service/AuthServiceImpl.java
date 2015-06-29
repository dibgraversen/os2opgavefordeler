package dk.os2opgavefordeler.service;

import com.nimbusds.jwt.ReadOnlyJWTClaimsSet;
import com.nimbusds.oauth2.sdk.id.State;
import dk.os2opgavefordeler.model.Employment;
import dk.os2opgavefordeler.model.IdentityProvider;
import dk.os2opgavefordeler.model.User;
import dk.os2opgavefordeler.model.presentation.IdentityProviderPO;
import dk.os2opgavefordeler.service.oidc.OpenIdConnect;
import org.slf4j.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

@Stateless
public class AuthServiceImpl implements AuthService {
	@Inject
	private Logger log;

	@Inject
	private UsersService userService;

	@Inject
	private EmploymentService employmentService;

	@Inject
	private OpenIdConnect openIdConnect;

	static private final Map<Integer, IdentityProvider> providers = new HashMap<>();
	static {
		providers.put(1, IdentityProvider.builder()
			.id(1).name("Google account")
			.url("https://accounts.google.com/")
			.clientId("89170361789-mg8l3t3f11vo0cf0hce4h85epi0qqq3q.apps.googleusercontent.com")
			.clientSecret("itCIp2JGR2NKBAu4Se9LCAjp")
			.build()
		);
		providers.put(2, IdentityProvider.builder()
			.id(2).name("Kitos SSO")
			.url("https://kitos.roskilde.dk/gateway/")
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
	public Optional<IdentityProvider> findProvider(int id) {
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
		try {
			ReadOnlyJWTClaimsSet claims = openIdConnect.finalizeAuthenticationFlow(idp, token, callbackUrl, requestUri);

			final String email = claims.getStringClaim("email");
			log.info("Email from claim: {}", email);

			return userService.findByEmail(email)
				.map(user -> {
					log.info("User found by email, returning");
					return user;
				})
				.orElseGet(() -> {
					log.info("User not found, creating");
					return createUser(email);
				});
		}
		catch(java.text.ParseException e) {
			throw new AuthenticationException("Error parsing claims", e);
		}
	}


	private User createUser(String email) {
		// In order to create a User from an OpenID Connect login, we require the email to be present in a municipality.
		//
		// An email address can be used for several Employments. For instance, it's possible for a manager to also have
		// non-manager employment - so we create a role of each of the employment.
		//
		final List<Employment> employments = employmentService.findByEmail(email);
		if(employments.isEmpty()) {
			throw new RuntimeException("No employments found");				//TODO: proper exception. Unathorized.
		}

		final List<dk.os2opgavefordeler.model.Role> roles = createRolesFromEmployments(employments);
		final User user = new User(email, roles);

		log.info("Persising {} with roles={}", user, roles);
		return userService.createUser(user);
	}

	private List<dk.os2opgavefordeler.model.Role> createRolesFromEmployments(List<Employment> employments) {
		return employments.stream()
			.map(emp -> {
				dk.os2opgavefordeler.model.Role role = new dk.os2opgavefordeler.model.Role();

				role.setManager(emp.getEmployedIn().getChildren().equals(emp));
				role.setEmployment(emp.getId());
				role.setName(String.format("%s (%s)", emp.getName(), emp.getEmployedIn().getName()));

				return role;
			})
			.collect(Collectors.toList());
	}

	private boolean hasManagerRole(List<Employment> roles) {
		return roles.stream()
			.anyMatch(emp -> emp.getEmployedIn().getManager().equals(emp));
	}
}
