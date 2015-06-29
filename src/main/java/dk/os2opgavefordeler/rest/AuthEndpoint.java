package dk.os2opgavefordeler.rest;

import dk.os2opgavefordeler.model.IdentityProvider;
import dk.os2opgavefordeler.model.User;
import dk.os2opgavefordeler.service.AuthService;
import dk.os2opgavefordeler.service.AuthenticationException;
import org.slf4j.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.*;
import java.net.URI;
import java.util.Optional;


@Path("/auth")
@RequestScoped
public class AuthEndpoint {
	private static final String S_CSRF_TOKEN = "auth-csrf-token";
	private static final String S_IDP_ID = "auth-idp-id";

	@Inject
	Logger log;

	@Inject
	AuthService authService;

	@Context
	private HttpServletRequest request;

	public static final String callback_url = "http://localhost:8080/TopicRouter/rest/auth/authenticate";
	public static final String home_url = "http://localhost:9001/";	//TODO: property? Or should we pick it up from the original request referer?

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
			final URI authReqURI = authService.beginAuthenticationFlow(idp, token, callback_url);

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
				.orElseThrow(RuntimeException::new);
			String token = Optional.ofNullable((String) session.getAttribute(S_CSRF_TOKEN))
				.orElseThrow(RuntimeException::new);

			IdentityProvider idp = authService.findProvider(idpId)
				.orElseThrow(RuntimeException::new);

			final User user = authService.finalizeAuthenticationFlow(idp, token, callback_url, ui.getRequestUri());
			request.getSession().setAttribute("authenticated-user", user);

			//TODO: keep this user logged in. Session state? Persisted token + cookie?

			log.info("finishAuthentication: {} is now logged in, redirecting to {}", user, home_url);
			return Response.temporaryRedirect(URI.create(home_url)).build();
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
