package dk.os2opgavefordeler.auth.openid;

import com.google.common.base.Strings;
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
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.openid.connect.sdk.*;
import com.nimbusds.openid.connect.sdk.claims.UserInfo;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
import com.nimbusds.openid.connect.sdk.util.DefaultJWTDecoder;
import dk.os2opgavefordeler.model.IdentityProvider;
import dk.os2opgavefordeler.service.AuthenticationException;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Scanner;

public class OpenIdConnectImpl implements OpenIdConnect {
	public static final String OPENID_DISCOVERY_DOCUMENT_PATH = ".well-known/openid-configuration";
	@Inject
	private Logger log;

	@Override
	public URI beginAuthenticationFlow(IdentityProvider idp, String token, String callbackUrl)
	throws AuthenticationException
	{
		try {
			OIDCProviderMetadata providerMetadata = getProvider(idp);

			State state = new State(token);
			Scope scope = Scope.parse("openid all_claims");

			ClientID apiKey = new ClientID(idp.getClientId());
			URI callback = new URI(callbackUrl);

			// Compose the request
			AuthenticationRequest authenticationRequest = new AuthenticationRequest(
				providerMetadata.getAuthorizationEndpointURI(),
				new ResponseType(ResponseType.Value.CODE),
				scope, apiKey, callback, state, null);

			return authenticationRequest.toURI();
		}
		catch(URISyntaxException e) {
			throw new AuthenticationException("URI Syntax Error", e);
		}
		catch(SerializeException e) {
			throw new AuthenticationException("Error forming authentication request URL");
		}
	}

	@Override
	public String finalizeAuthenticationFlow(IdentityProvider idp, String token, String callbackUrl, URI requestUri)
	throws AuthenticationException
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

		try {
			String email = claims.getStringClaim("email");
			if(Strings.isNullOrEmpty(email)) {
				log.info("email claim empty - trying UserInfo endpoint");
				return getEmailFromUserInfoEndpoint(pmd, accessTokenResponse);
			} else {
				return email;
			}
		} catch(java.text.ParseException ex) {
			throw new AuthenticationException("Error parsing OpenID claims", ex);
		}
	}

	private String getEmailFromUserInfoEndpoint(OIDCProviderMetadata providerMetadata, OIDCAccessTokenResponse accessToken)
	throws AuthenticationException
	{
		//TODO: is there a way to get this information as a signed JWT?
		final UserInfoRequest userInfoReq = new UserInfoRequest(
			providerMetadata.getUserInfoEndpointURI(),
			(BearerAccessToken) accessToken.getAccessToken() );

		final HTTPResponse userInfoHTTPResp;
		try {
			userInfoHTTPResp = userInfoReq.toHTTPRequest().send();
		} catch (SerializeException | IOException e) {
			throw new AuthenticationException("UserInfo endpoint error", e);
		}

		final UserInfoResponse userInfoResponse;
		try {
			userInfoResponse = UserInfoResponse.parse(userInfoHTTPResp);
		} catch (ParseException e) {
			throw new AuthenticationException("UserInfo parsing error", e);
		}

		if (userInfoResponse instanceof UserInfoErrorResponse) {
			ErrorObject error = ((UserInfoErrorResponse) userInfoResponse).getErrorObject();
			throw new AuthenticationException(error.getDescription());
		}

		final UserInfoSuccessResponse sresp = (UserInfoSuccessResponse) userInfoResponse;
		final UserInfo userInfo = sresp.getUserInfo();

		return userInfo.getEmail().toString();
	}

	private void validateReponseToken(AuthenticationSuccessResponse successResponse, String token)
	throws AuthenticationException
	{
		if(!token.equals(successResponse.getState().getValue())) {
			throw new AuthenticationException("Invalid CSRF token");
		}
	}

	private AuthenticationSuccessResponse parseResponse(URI requestUri)
	throws AuthenticationException
	{
		try {
			final AuthenticationResponse authResp = AuthenticationResponseParser.parse(requestUri);
			if (authResp instanceof AuthenticationErrorResponse) {
				final ErrorObject error = ((AuthenticationErrorResponse) authResp).getErrorObject();
				throw new AuthenticationException(error.getDescription());
			}
			return (AuthenticationSuccessResponse) authResp;
		} catch (ParseException e) {
			throw new AuthenticationException("Error parsing IDP callback");
		}
	}

	private OIDCAccessTokenResponse requestTokens(IdentityProvider idp, OIDCProviderMetadata providerMetadata, String callbackUrl, AuthorizationCode authCode)
	throws AuthenticationException
	{
		try {
			final ClientID clientID = new ClientID(idp.getClientId());
			final URI callback = new URI(callbackUrl);
			final Secret clientSecret = new Secret(idp.getClientSecret());

			final ClientAuthentication clientAuth = new ClientSecretBasic(clientID, clientSecret);

			final TokenRequest tokenReq = new TokenRequest(
				providerMetadata.getTokenEndpointURI(),
				clientAuth, new AuthorizationCodeGrant(authCode, callback));
				//Note: repeating 'scope' shouldn't be necessary in the token request. Doesn't matter for google, and
				//doesn't seem to make a difference for IdentityServer3.

			final HTTPResponse tokenHTTPResp = tokenReq.toHTTPRequest().send();

			final TokenResponse tokenResponse = OIDCTokenResponseParser.parse(tokenHTTPResp);
			if (tokenResponse instanceof TokenErrorResponse) {
				log.error("Authentication failed - token response error: {}", (((TokenErrorResponse) tokenResponse).toJSONObject()).toString());

				ErrorObject error = ((TokenErrorResponse) tokenResponse).getErrorObject();
				throw new AuthenticationException(error.getDescription());
			}

			OIDCAccessTokenResponse accessTokenResponse = (OIDCAccessTokenResponse) tokenResponse;
			return accessTokenResponse;
		}
		catch (URISyntaxException e) {
			throw new AuthenticationException("Error in IDP URI", e);
		}
		catch (SerializeException | IOException e) {
			throw new AuthenticationException("Error requesting token", e);
		}
		catch (ParseException e) {
			throw new AuthenticationException("Error parsing token", e);
		}
	}

	private ReadOnlyJWTClaimsSet verifyIdToken(JWT idToken, OIDCProviderMetadata providerMetadata)
	throws AuthenticationException
	{
		final String keyId = (String) idToken.getHeader().toJSONObject().get("kid");

		final RSAPublicKey providerKey = lookupKey(providerMetadata, keyId);
		final ReadOnlyJWTClaimsSet claims = verifyClaims(idToken, providerKey);

		return claims;
	}

	private RSAPublicKey lookupKey(OIDCProviderMetadata providerMetadata, String keyId)
	throws AuthenticationException
	{
		log.info("looking for key {}", keyId);
		RSAPublicKey providerKey = null;
		try {
			JSONObject key = getProviderRSAJWK(providerMetadata.getJWKSetURI().toURL().openStream(), keyId);
			providerKey = RSAKey.parse(key).toRSAPublicKey();
		} catch (InvalidKeySpecException | NoSuchAlgorithmException | IOException | java.text.ParseException e) {
			throw new AuthenticationException("Error parsing JWT", e);
		}
		return providerKey;
	}

	private ReadOnlyJWTClaimsSet verifyClaims(JWT idToken, RSAPublicKey providerKey)
	throws AuthenticationException
	{
		DefaultJWTDecoder jwtDecoder = new DefaultJWTDecoder();
		jwtDecoder.addJWSVerifier(new RSASSAVerifier(providerKey));
		ReadOnlyJWTClaimsSet claims = null;
		try {
			claims = jwtDecoder.decodeJWT(idToken);
		} catch (JOSEException | java.text.ParseException e) {
			throw new AuthenticationException("Error decoding JWT", e);
		}
		return claims;
	}

	private JSONObject getProviderRSAJWK(InputStream is, String kid)
	throws java.text.ParseException {
		//TODO: this code is a bit fast-and-loose, and doesn't support the HMAC scheme - fix.
		// Read all data from stream
		final StringBuilder sb = new StringBuilder();
		try (Scanner scanner = new Scanner(is);) {
			while (scanner.hasNext()) {
				sb.append(scanner.next());
			}
		}

		final String jsonString = sb.toString();
		final JSONObject json = JSONObjectUtils.parseJSONObject(jsonString);

		// Find the RSA signing key
		final JSONArray keyList = (JSONArray) json.get("keys");
		for (Object key : keyList) {
			JSONObject k = (JSONObject) key;
			if (k.get("use").equals("sig") && k.get("kty").equals("RSA") && kid.equals(k.get("kid"))) {
				log.info("Key found");
				return k;
			}
		}

		log.warn("Key not found!");
		return null;
	}

	private OIDCProviderMetadata getProvider(IdentityProvider idp)
	throws AuthenticationException
	{
		//TODO: should we cache the provider info?
		//Probably not necessary - during normal operation, the user is authenticated by a session token,
		// and even during the OIDC flow we should only be doing two fetches.
		log.info("Getting provider metadata for {}", idp.getName());

		try {
			final URI idpUrl = new URI(idp.getIdpUrl());
			final URI provider = idpUrl.resolve(OPENID_DISCOVERY_DOCUMENT_PATH);
			final URL providerConfigurationURL = provider.toURL();

			final InputStream stream = providerConfigurationURL.openStream();

			String providerInfo = null;
			try (Scanner s = new Scanner(stream)) {
				providerInfo = s.useDelimiter("\\A").hasNext() ? s.next() : "";
			}

			return OIDCProviderMetadata.parse(providerInfo);
		}
		catch(URISyntaxException | MalformedURLException e) {
			throw new AuthenticationException("Error in IDP URL", e);
		}
		catch(IOException e) {
			throw new AuthenticationException("Error fetching IDP discovery document", e);
		}
		catch(ParseException e) {
			throw new AuthenticationException("Error parsing discovery document", e);
		}
	}
}
