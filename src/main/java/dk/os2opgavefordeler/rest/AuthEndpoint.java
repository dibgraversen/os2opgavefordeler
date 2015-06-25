package dk.os2opgavefordeler.rest;

import dk.os2opgavefordeler.model.IdentityProvider;
import dk.os2opgavefordeler.model.User;
import dk.os2opgavefordeler.service.AuthService;
import org.slf4j.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;


@Path("/auth")
@RequestScoped
public class AuthEndpoint {
	@Inject
	Logger log;

	@Inject
	AuthService authService;

	public static final String callback_url = "http://localhost:8080/TopicRouter/rest/auth/authenticate";
	public static final String home_url = "/";	//TODO: property?

	@GET
	@Path("/providers")
	@Produces(MediaType.APPLICATION_JSON)
	public Response listIdp() {
		return Response.ok().entity(authService.identityProviderPOList()).build();
	}

	@POST
	@Path("/providers/{providerId}/signin")
//	@Consumes( {"*/*", MediaType.APPLICATION_JSON })
//	@Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_HTML } )
	public Response beginAuthentication(@PathParam(value = "providerId") int providerId) {
		try {
			final IdentityProvider idp = authService.findProvider(providerId).orElseThrow(RuntimeException::new);
			final URI authReqURI = authService.beginAuthenticationFlow(idp, callback_url);

			log.info("beginAuthentication: redirecting to {}", authReqURI);

			return Response.temporaryRedirect(authReqURI).build();
		}
		catch(Throwable t) {
			log.error("error in beingAuthentication", t);
			return Response.serverError().build();
		}
	}

	@GET
	@Path("/authenticate")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response finishAuthentication(@Context UriInfo ui) {
		MultivaluedMap<String, String> qp = ui.getQueryParameters();

		log.info("finishAuthentication: {}, query parameters: {}", ui.getRequestUri(), qp);

		IdentityProvider idp = null;	// get from session token
		String expectedState = "42";	// get from session token
		try {
			final User user = authService.finalizeAuthenticationFlow(idp, expectedState);
			log.info("finishAuthentication: {} is now logged in, redirecting to {}", user, home_url);
			return Response.temporaryRedirect(URI.create(home_url)).build();
		}
		catch(Throwable t) {
			//TODO: clean up session token - cookies as well as entity.
			log.error("error in finishAuthentication", t);
			return Response.serverError().build();
		}
	}
}
