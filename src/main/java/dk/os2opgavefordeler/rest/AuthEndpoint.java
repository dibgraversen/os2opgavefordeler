package dk.os2opgavefordeler.rest;

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
import dk.os2opgavefordeler.service.AuthService;
import org.slf4j.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;


@Path("/auth")
@RequestScoped
public class AuthEndpoint {
	@Inject
	Logger log;

	@Inject
	AuthService authService;


	public static final String callback_url = "http://localhost:8080/TopicRouter/rest/auth/authenticate";

/*
	private static final String idp_url = "https://accounts.google.com";
	private static final String client_id = "89170361789-mg8l3t3f11vo0cf0hce4h85epi0qqq3q.apps.googleusercontent.com";
	private static final String client_secret = "itCIp2JGR2NKBAu4Se9LCAjp";
*/

	private static final String idp_url = "https://kitos.roskilde.dk/gateway/";
	private static final String client_id = "suneclient";
	private static final String client_secret = "secret"; // "2bb80d537b1da3e38bd30361aa855686bde0eacd7162fef6a25fe97bf527a25b";  //"secret";

	@Path("/idp")
	@Produces(MediaType.APPLICATION_JSON)
	public Response listIdp() {
		return Response.ok().entity(authService.identityProviderPOList()).build();
	}



	@GET
	@Path("/idp/{providerId}")
	@Consumes( {"*/*", MediaType.APPLICATION_JSON })
	@Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_HTML } )
	public Response beginAuthentication(@PathParam(value = "providerId") int providerId) {
		try {
			final IdentityProvider idp = authService.findProvider(providerId).orElseThrow(RuntimeException::new);
			final URI authReqURI = authService.beginAuthenticationFlow(idp);

			log.info("Redirecting to {}", authReqURI);

			return Response.temporaryRedirect(authReqURI).build();

			/*
			OIDCProviderMetadata providerMetadata = getProvider(idp);

			// Generate random state string for pairing the response to the request
			State state = new State();
			Nonce nonce = new Nonce();                // not required for CODE flow
			Scope scope = Scope.parse("openid email");

			ClientID apiKey = new ClientID(idp.getClientId());
			URI callback = new URI(callback_url);

			// Compose the request
			AuthenticationRequest authenticationRequest = new AuthenticationRequest(
				providerMetadata.getAuthorizationEndpointURI(),
				new ResponseType(ResponseType.Value.CODE),
				scope, apiKey, callback, state, nonce);

			URI authReqURI = authenticationRequest.toURI();
			*/
		}
		catch(Throwable t) {
			log.error("error in beingAuthentication", t);
			return Response.serverError().build();
		}
	}

	private OIDCProviderMetadata getProvider(IdentityProvider idp) throws URISyntaxException, IOException, ParseException {
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

	@GET
	@Path("/authenticate")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response authenticate(@Context UriInfo ui) throws URISyntaxException, IOException, ParseException {
		log.info("Hey, we got called at {}", ui.getAbsolutePath());

		MultivaluedMap<String, String> qp = ui.getQueryParameters();

		log.info("Authenticate - got these QPs: {}", qp);

		// 1) Extract state parameter - get from cookie?
		// 2) Verify state parameter matches one from query string, or throw 401-unauthorized

		// 3) Extract 'code', exchange for access + id tokens.
/*
		AuthenticationResponse authResp = null;
		try {
			authResp = AuthenticationResponseParser.parse(ui.getRequestUri());
		} catch (ParseException e) {
			// TODO error handling
			log.error("Error parsing response");
			return Response.serverError().build();
		}

		if (authResp instanceof AuthenticationErrorResponse) {
			ErrorObject error = ((AuthenticationErrorResponse) authResp)
				.getErrorObject();
			return Response.serverError().build();
		}

		AuthenticationSuccessResponse successResponse = (AuthenticationSuccessResponse) authResp;

		/* Don't forget to check the state!
		 * The state in the received authentication response must match the state
		 * specified in the previous outgoing authentication request.
		*/
//		if (!verifyState(successResponse.getState())) {
//			// TODO proper error handling
//		}
/*
		AuthorizationCode authCode = successResponse.getAuthorizationCode();
		log.info("Auth code: {}", authCode);


		ClientID clientID = new ClientID(client_id);
		URI callback = new URI(callback_url);
		Secret clientSecret = new Secret(client_secret);

		ClientAuthentication clientAuth = new ClientSecretBasic(clientID, clientSecret);

		OIDCProviderMetadata providerMetadata = getProvider(TODO);

		TokenRequest tokenReq = new TokenRequest(
			providerMetadata.getTokenEndpointURI(),
			clientAuth, new AuthorizationCodeGrant(authCode,
			callback));

		HTTPResponse tokenHTTPResp = null;
		try {
			tokenHTTPResp = tokenReq.toHTTPRequest().send();
		} catch (SerializeException | IOException e) {
			log.error("token request error", e);
			return Response.serverError().build();
		}

		// Parse and check response
		TokenResponse tokenResponse = null;
		try {
			tokenResponse = OIDCTokenResponseParser.parse(tokenHTTPResp);
		} catch (ParseException e) {
			log.error("token parse error", e);
			return Response.serverError().build();
		}

		if (tokenResponse instanceof TokenErrorResponse) {
			ErrorObject error = ((TokenErrorResponse) tokenResponse).getErrorObject();
			log.error("token something error {}/{}", error, error.getDescription());

			return Response.serverError().build();
		}

		OIDCAccessTokenResponse accessTokenResponse = (OIDCAccessTokenResponse) tokenResponse;

		log.info("Access token: {}", accessTokenResponse.getAccessToken());
		log.info("id token      {}", accessTokenResponse.getIDTokenString());
*/
/*
		accessResources("https://www.googleapis.com/userinfo/v2/me", service, accessToken);
		accessResources("https://www.googleapis.com/oauth2/v3/userinfo", service, accessToken);
*/

		return Response.ok(ui.getQueryParameters()).build();
	}
}
