package dk.os2opgavefordeler.rest;

import com.google.common.base.Strings;
import dk.os2opgavefordeler.auth.ActiveUser;
import dk.os2opgavefordeler.auth.BasicAuthFilter;
import dk.os2opgavefordeler.model.IdentityProvider;
import dk.os2opgavefordeler.model.User;
import dk.os2opgavefordeler.model.presentation.SimpleMessage;
import dk.os2opgavefordeler.service.AuthenticationException;
import dk.os2opgavefordeler.service.AuthenticationService;
import dk.os2opgavefordeler.service.ConfigService;
import org.jboss.resteasy.annotations.cache.NoCache;
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
    public static final String S_AUTHENTICATED_USER = "authenticated-user";
    private static final String S_CSRF_TOKEN = "auth-csrf-token";
    private static final String S_IDP_ID = "auth-idp-id";
    @Inject
    private Logger log;

    @Inject
    private AuthenticationService authenticationService;

    @Context
    private HttpServletRequest request;

    @Inject
    private ConfigService config;

    @POST
    @Path("/logout")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response logout() {
        log.info("Logging out user");
        request.getSession().setAttribute(BasicAuthFilter.SESSION_ACTIVE_USER, null);
        return Response.ok().entity(new SimpleMessage("logged out")).build();
    }

    @GET
    @Path("/providers")
    @Produces(MediaType.APPLICATION_JSON)
    @NoCache
    public Response listIdp() {
        return Response.ok().entity(authenticationService.identityProviderPOList()).build();
    }

    @GET
    @Path("/providers/{providerId}/signin")
    @NoCache
    public Response beginAuthentication(@PathParam(value = "providerId") long providerId) {
        try {
            final HttpSession session = request.getSession();

            final IdentityProvider idp = authenticationService.findProvider(providerId).orElseThrow(RuntimeException::new);
            final String token = authenticationService.generateCsrfToken();
            final URI authReqURI = authenticationService.beginAuthenticationFlow(idp, token, config.getOpenIdCallbackUrl());

            session.setAttribute(S_CSRF_TOKEN, token);
            session.setAttribute(S_IDP_ID, providerId);

            log.info("beginAuthentication: redirecting to {}", authReqURI);

            return Response.temporaryRedirect(authReqURI).build();
        } catch (Throwable t) {
            log.error("error in beginAuthentication", t);
            return Response.serverError().build();
        }
    }

    @GET
    @Path("/authenticate")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @NoCache
    public Response finishAuthentication(@Context UriInfo ui) {
        final HttpSession session = request.getSession();

        MultivaluedMap<String, String> qp = ui.getQueryParameters();

        log.info("finishAuthentication: {}, query parameters: {}", ui.getRequestUri(), qp);

        try {
            long idpId = Optional.ofNullable((Long) session.getAttribute(S_IDP_ID))
                    .orElseThrow(() -> new AuthenticationException("S_IDP_ID not set"));
            String token = Optional.ofNullable((String) session.getAttribute(S_CSRF_TOKEN))
                    .orElseThrow(() -> new AuthenticationException("S_CSRF_TOKEN not set"));

            IdentityProvider idp = authenticationService.findProvider(idpId)
                    .orElseThrow(() -> new AuthenticationException("Invalid IDP id"));

            final User user = authenticationService.finalizeAuthenticationFlow(idp, token, config.getOpenIdCallbackUrl(), ui.getRequestUri());

            request.getSession().setAttribute(BasicAuthFilter.SESSION_ACTIVE_USER, new ActiveUser(user.getEmail(), true));

            log.info("finishAuthentication: {} is now logged in, redirecting to {}", user, config.getHomeUrl());
            return Response.temporaryRedirect(URI.create(config.getHomeUrl())).build();
        } catch (AuthenticationException ex) {
            log.error("Error authenticating user", ex);
            return Response.status(Response.Status.FORBIDDEN).build();
        } finally {
            session.removeAttribute(S_CSRF_TOKEN);
            session.removeAttribute(S_IDP_ID);
        }
    }

    @GET
    @Path("/iddqd")
    @Produces(MediaType.TEXT_HTML + "; charset=UTF-8")
    @NoCache
    public Response godModeLogin(@QueryParam(value = "email") String email) {
        if (!config.isGodModeLoginEnabled()) {
            log.warn("IDDQD Auth endpoint hit but not enabled!");
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if (Strings.isNullOrEmpty(email)) {
            return Response.status(Response.Status.BAD_REQUEST).entity("No email supplied").build();
        }

        log.info("IDDQD Auth - attempting to log in [{}]", email);
        final User user = authenticationService.findOrCreateUserFromEmail(email);
        request.getSession().setAttribute(BasicAuthFilter.SESSION_ACTIVE_USER, new ActiveUser(user.getEmail(), true));
        return Response.temporaryRedirect(URI.create(config.getHomeUrl())).build();
    }
}