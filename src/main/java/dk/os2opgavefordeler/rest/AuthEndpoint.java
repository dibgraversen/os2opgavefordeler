package dk.os2opgavefordeler.rest;

import dk.os2opgavefordeler.model.IdentityProvider;
import dk.os2opgavefordeler.model.User;
import dk.os2opgavefordeler.model.presentation.SimpleMessage;
import dk.os2opgavefordeler.service.AuthService;
import dk.os2opgavefordeler.service.AuthenticationException;
import dk.os2opgavefordeler.service.ConfigService;
import org.slf4j.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.util.Optional;


@Path("/auth")
@RequestScoped
public class AuthEndpoint {
	private static final String S_CSRF_TOKEN = "auth-csrf-token";
	private static final String S_IDP_ID = "auth-idp-id";
	public static final String S_AUTHENTICATED_USER = "authenticated-user";


	@Inject
	Logger log;

	@Inject
	AuthService authService;

	@Context
	private HttpServletRequest request;

	@Inject
	ConfigService config;

	@POST
	@Path("/logout")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response logout() {
		log.info("Logging out user");
		request.getSession().removeAttribute(S_AUTHENTICATED_USER);
		return Response.ok().entity(new SimpleMessage("logged out")).build();
//		return Response.serverError().entity(new SimpleMessage("error logging out")).build();
	}

	@GET
	@Path("/providers")
	@Produces(MediaType.APPLICATION_JSON)
	public Response listIdp() {
		return Response.ok().entity(authService.identityProviderPOList()).build();
	}

	@GET
	@Path("/providers/{providerId}/signin")
	public Response beginAuthentication(@PathParam(value = "providerId") int providerId) {
		try {
			final HttpSession session = request.getSession();

			final IdentityProvider idp = authService.findProvider(providerId).orElseThrow(RuntimeException::new);
			final String token = authService.generateCsrfToken();
			final URI authReqURI = authService.beginAuthenticationFlow(idp, token, config.getOpenIdCallbackUrl());

			session.setAttribute(S_CSRF_TOKEN, token);
			session.setAttribute(S_IDP_ID, providerId);

			log.info("beginAuthentication: redirecting to {}", authReqURI);

			return Response.temporaryRedirect(authReqURI).build();
		}
		catch(Throwable t) {
			log.error("error in beginAuthentication", t);
			return Response.serverError().build();
		}
	}

	@GET
	@Path("/authenticate")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response finishAuthentication(@Context UriInfo ui) {
		final HttpSession session = request.getSession();

		MultivaluedMap<String, String> qp = ui.getQueryParameters();

		log.info("finishAuthentication: {}, query parameters: {}", ui.getRequestUri(), qp);

		try {
			int idpId = Optional.ofNullable((Integer) session.getAttribute(S_IDP_ID))
				.orElseThrow( () -> new AuthenticationException("S_IDP_ID not set"));
			String token = Optional.ofNullable((String) session.getAttribute(S_CSRF_TOKEN))
				.orElseThrow( () -> new AuthenticationException("S_CSRF_TOKEN not set") );

			IdentityProvider idp = authService.findProvider(idpId)
				.orElseThrow( () -> new AuthenticationException("Invalid IDP id"));

			final User user = authService.finalizeAuthenticationFlow(idp, token, config.getOpenIdCallbackUrl(), ui.getRequestUri());
			request.getSession().setAttribute(S_AUTHENTICATED_USER, user);

			//TODO: keep this user logged in. Session state? Persisted token + cookie?

			log.info("finishAuthentication: {} is now logged in, redirecting to {}", user, config.getHomeUrl());
			return Response.temporaryRedirect(URI.create(config.getHomeUrl())).build();
		}
		catch(AuthenticationException ex) {
			log.error("Error authenticating user", ex);
			return Response.status(Response.Status.FORBIDDEN).build();
		}
		finally {
			session.removeAttribute(S_CSRF_TOKEN);
			session.removeAttribute(S_IDP_ID);
		}
	}
}
