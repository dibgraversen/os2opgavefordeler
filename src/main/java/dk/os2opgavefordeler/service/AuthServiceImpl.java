package dk.os2opgavefordeler.service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.util.JSONObjectUtils;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.ReadOnlyJWTClaimsSet;
import com.nimbusds.oauth2.sdk.*;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.State;
import com.nimbusds.openid.connect.sdk.*;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
import com.nimbusds.openid.connect.sdk.util.DefaultJWTDecoder;
import dk.os2opgavefordeler.model.*;
import dk.os2opgavefordeler.model.presentation.IdentityProviderPO;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.slf4j.Logger;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
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
	throws Throwable
	{
		OIDCProviderMetadata providerMetadata = getProvider(idp);

		// Generate random state string for pairing the response to the request
		State state = new State(token);
		Nonce nonce = new Nonce();                // not required for CODE flow
		Scope scope = Scope.parse("openid email profile");

		ClientID apiKey = new ClientID(idp.getClientId());
		URI callback = new URI(callbackUrl);

		// Compose the request
		AuthenticationRequest authenticationRequest = new AuthenticationRequest(
			providerMetadata.getAuthorizationEndpointURI(),
			new ResponseType(ResponseType.Value.CODE),
			scope, apiKey, callback, state, nonce);

		return authenticationRequest.toURI();
	}

	@Override
	public User finalizeAuthenticationFlow(IdentityProvider idp, String token, String callbackUrl, URI requestUri)
	throws Throwable
	{
		//TODO: loads of cleanup, refactoring and error handling

		AuthenticationResponse authResp = null;
		try {
			authResp = AuthenticationResponseParser.parse(requestUri);
		} catch (ParseException e) {
			// TODO error handling
			log.error("Error parsing response");
			throw new RuntimeException();
		}

		if (authResp instanceof AuthenticationErrorResponse) {
			ErrorObject error = ((AuthenticationErrorResponse) authResp)
				.getErrorObject();
			throw new RuntimeException();
		}

		AuthenticationSuccessResponse successResponse = (AuthenticationSuccessResponse) authResp;

		if(!token.equals(successResponse.getState().getValue())) {
			log.info("Invalid CSRF token - {} vs {}", token, successResponse.getState().getValue());
			throw new RuntimeException();
		}

		AuthorizationCode authCode = successResponse.getAuthorizationCode();
		log.info("Auth code: {}", authCode);


		ClientID clientID = new ClientID(idp.getClientId());
		URI callback = new URI(callbackUrl);
		Secret clientSecret = new Secret(idp.getClientSecret());

		ClientAuthentication clientAuth = new ClientSecretBasic(clientID, clientSecret);

		OIDCProviderMetadata providerMetadata = getProvider(idp);

		TokenRequest tokenReq = new TokenRequest(
			providerMetadata.getTokenEndpointURI(),
			clientAuth, new AuthorizationCodeGrant(authCode,
			callback));

		HTTPResponse tokenHTTPResp = null;
		try {
			tokenHTTPResp = tokenReq.toHTTPRequest().send();
		} catch (SerializeException | IOException e) {
			log.error("token request error", e);
			throw new RuntimeException();
		}

		// Parse and check response
		TokenResponse tokenResponse = null;
		try {
			tokenResponse = OIDCTokenResponseParser.parse(tokenHTTPResp);
		} catch (ParseException e) {
			log.error("token parse error", e);
			throw new RuntimeException();
		}

		if (tokenResponse instanceof TokenErrorResponse) {
			ErrorObject error = ((TokenErrorResponse) tokenResponse).getErrorObject();
			log.error("token something error {}/{}", error, error.getDescription());

			throw new RuntimeException();
		}

		OIDCAccessTokenResponse accessTokenResponse = (OIDCAccessTokenResponse) tokenResponse;

		log.info("Access token: {}", accessTokenResponse.getAccessToken());
		log.info("id token      {}", accessTokenResponse.getIDTokenString());

		log.info("Verifying id token");
		ReadOnlyJWTClaimsSet claims = verifyIdToken(accessTokenResponse.getIDToken(), providerMetadata);
		log.info("Verified, claims: {}", claims);

		/*
		final String email = getEmailFromToken(accessTokenResponse);

		return userService.findByEmail(email)
			.map(user -> {
				log.info("User found by email, returning");
				return user;
			})
			.orElseGet(() -> {
				log.info("User not found, creating");
				return createUser(email);
			});
			*/

		return null;
	}




	private ReadOnlyJWTClaimsSet verifyIdToken(JWT idToken, OIDCProviderMetadata providerMetadata) {
		String keyId = (String) idToken.getHeader().toJSONObject().get("kid");
		log.info("looking for key {}", keyId);


		RSAPublicKey providerKey = null;
		try {
			//TODO: extract kid from idToken
			JSONObject key = getProviderRSAJWK(providerMetadata.getJWKSetURI().toURL().openStream(), keyId);
			providerKey = RSAKey.parse(key).toRSAPublicKey();
		} catch (NoSuchAlgorithmException | InvalidKeySpecException
			| IOException | java.text.ParseException e) {
			//TODO: error handling
			log.error("verifyIdToken: error parsing", e);
			throw new RuntimeException();
		}

		DefaultJWTDecoder jwtDecoder = new DefaultJWTDecoder();
		jwtDecoder.addJWSVerifier(new RSASSAVerifier(providerKey));
		ReadOnlyJWTClaimsSet claims = null;
		try {
			claims = jwtDecoder.decodeJWT(idToken);
		} catch (JOSEException | java.text.ParseException e) {
			//TODO: error handling
			log.error("verifyIdToken: error decoding", e);
			throw new RuntimeException();
		}

		return claims;
	}

	private JSONObject getProviderRSAJWK(InputStream is, String kid) throws java.text.ParseException {
		// Read all data from stream
		StringBuilder sb = new StringBuilder();
		try (Scanner scanner = new Scanner(is);) {
			while (scanner.hasNext()) {
				sb.append(scanner.next());
			}
		}

		// Parse the data as json
		String jsonString = sb.toString();
		JSONObject json = JSONObjectUtils.parseJSONObject(jsonString);

		// Find the RSA signing key
		JSONArray keyList = (JSONArray) json.get("keys");
		for (Object key : keyList) {
			JSONObject k = (JSONObject) key;
			if (k.get("use").equals("sig") && k.get("kty").equals("RSA") && kid.equals(k.get("kid"))) {
				return k;
			}
		}
		return null;
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

	private String getEmailFromToken(OIDCAccessTokenResponse accessTokenResponse) {
		throw new NotImplementedException();
	}


	private OIDCProviderMetadata getProvider(IdentityProvider idp) throws URISyntaxException, IOException, ParseException {
		//TODO: cache?
		log.info("Getting provider metadata for {}", idp.getName());

		URI idpUrl = new URI(idp.getIdpUrl());
		URI provider = idpUrl.resolve(".well-known/openid-configuration");

		URL providerConfigurationURL = provider.toURL();

		String providerInfo = null;
		try {
			log.info("Trying to open stream {}", providerConfigurationURL);
			InputStream stream = providerConfigurationURL.openStream();

			log.info("Opened stream, trying to scan...");
			try (java.util.Scanner s = new java.util.Scanner(stream)) {
				providerInfo = s.useDelimiter("\\A").hasNext() ? s.next() : "";
			}
		}
		catch(IOException ex) {
			log.error("We got an IO exception", ex);

		}

		log.info("got the medatadada");

		return OIDCProviderMetadata.parse(providerInfo);
	}
}
