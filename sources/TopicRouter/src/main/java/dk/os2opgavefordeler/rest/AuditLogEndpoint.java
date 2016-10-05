package dk.os2opgavefordeler.rest;

import java.util.Optional;

import javax.enterprise.context.RequestScoped;

import javax.inject.Inject;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import dk.os2opgavefordeler.auth.AuthService;

import dk.os2opgavefordeler.model.User;

import dk.os2opgavefordeler.service.AuditLogService;
import dk.os2opgavefordeler.service.UserService;

/**
 * Endpoint for retrieving audit log information
 */
@Path("/auditlog")
@RequestScoped
public class AuditLogEndpoint extends Endpoint {

    @Inject
    private AuthService authService;

    @Inject
    UserService userService;

    @Inject
    AuditLogService auditLogService;

    private static final String NOT_LOGGED_IN = "Not logged in";
    private static final String NOT_AUTHORIZED = "Not authorized";
    private static final String USER_NOT_FOUND = "User not found";

    /**
     * Returns the full list of audit log entries for the user's municipality
     *
     * @return list of audit log entries in JSON format
     */
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response getLogEntries() {
        if (!authService.isAuthenticated()) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(NOT_LOGGED_IN).build();
        }

        Optional<User> user = userService.findByEmail(authService.getAuthentication().getEmail());

        if (user.isPresent()) {
            long userId = user.get().getId();

            if (userService.isAdmin(userId) || userService.isMunicipalityAdmin(userId) || userService.isManager(userId)) { // only managers and admins can update responsibility
                return ok(auditLogService.getAllLogEntries(user.get().getMunicipality().getId()));
            }
            else {
                return Response.status(Response.Status.UNAUTHORIZED).entity(NOT_AUTHORIZED).build();
            }


        }
        else {
            return Response.status(Response.Status.NOT_FOUND).entity(USER_NOT_FOUND).build();
        }


    }

}
