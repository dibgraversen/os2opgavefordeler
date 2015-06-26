package dk.os2opgavefordeler.service;

import com.nimbusds.oauth2.sdk.*;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.State;
import com.nimbusds.openid.connect.sdk.*;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
import dk.os2opgavefordeler.model.IdentityProvider;
import dk.os2opgavefordeler.model.User;
import dk.os2opgavefordeler.model.presentation.IdentityProviderPO;
import org.slf4j.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

@Stateless
public class AuthServiceImpl implements AuthService {
	@Inject
	private Logger log;

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

		/* Don't forget to check the state!
		 * The state in the received authentication response must match the state
		 * specified in the previous outgoing authentication request.
		*/
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
/*
		accessResources("https://www.googleapis.com/userinfo/v2/me", service, accessToken);
		accessResources("https://www.googleapis.com/oauth2/v3/userinfo", service, accessToken);

*/
		return null;
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
