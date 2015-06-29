package dk.os2opgavefordeler.service.oidc;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.util.JSONObjectUtils;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.ReadOnlyJWTClaimsSet;
import com.nimbusds.oauth2.sdk.*;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.State;
import com.nimbusds.openid.connect.sdk.*;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
import com.nimbusds.openid.connect.sdk.util.DefaultJWTDecoder;
import dk.os2opgavefordeler.model.IdentityProvider;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Scanner;

public class OpenIdConnectImpl implements OpenIdConnect {
	@Inject
	private Logger log;

	@Override
	public URI beginAuthenticationFlow(IdentityProvider idp, String token, String callbackUrl)
	throws Throwable
	{
		OIDCProviderMetadata providerMetadata = getProvider(idp);

		State state = new State(token);
		Scope scope = Scope.parse("openid email profile");

		ClientID apiKey = new ClientID(idp.getClientId());
		URI callback = new URI(callbackUrl);

		// Compose the request
		AuthenticationRequest authenticationRequest = new AuthenticationRequest(
			providerMetadata.getAuthorizationEndpointURI(),
			new ResponseType(ResponseType.Value.CODE),
			scope, apiKey, callback, state, null);

		return authenticationRequest.toURI();
	}

	@Override
	public ReadOnlyJWTClaimsSet finalizeAuthenticationFlow(IdentityProvider idp, String token, String callbackUrl, URI requestUri)
	throws Throwable
	{
		//TODO: loads of cleanup, refactoring and error handling
		//TODO: expose our own Claims type instead of leaking nimbusds.

		AuthenticationSuccessResponse successResponse = parseResponse(requestUri);
		validateReponseToken(successResponse, token);
		AuthorizationCode authCode = successResponse.getAuthorizationCode();

		OIDCProviderMetadata pmd = getProvider(idp);

		OIDCAccessTokenResponse accessTokenResponse = requestTokens(idp, pmd, callbackUrl, authCode);

		log.info("Access token: {}", accessTokenResponse.getAccessToken());
		log.info("id token      {}", accessTokenResponse.getIDTokenString());

		log.info("Verifying id token");
		ReadOnlyJWTClaimsSet claims = verifyIdToken(accessTokenResponse.getIDToken(), pmd);
		log.info("Verified, claims: {}", claims);

		return claims;
	}

	private void validateReponseToken(AuthenticationSuccessResponse successResponse, String token) {
		if(!token.equals(successResponse.getState().getValue())) {
			log.info("Invalid CSRF token - {} vs {}", token, successResponse.getState().getValue());
			throw new RuntimeException();
		}
	}

	private AuthenticationSuccessResponse parseResponse(URI requestUri) {
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
		return successResponse;
	}

	private OIDCAccessTokenResponse requestTokens(IdentityProvider idp, OIDCProviderMetadata providerMetadata, String callbackUrl, AuthorizationCode authCode)
	throws Throwable
	{
		ClientID clientID = new ClientID(idp.getClientId());
		URI callback = new URI(callbackUrl);
		Secret clientSecret = new Secret(idp.getClientSecret());

		ClientAuthentication clientAuth = new ClientSecretBasic(clientID, clientSecret);

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
		return accessTokenResponse;
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
			try (Scanner s = new Scanner(stream)) {
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
