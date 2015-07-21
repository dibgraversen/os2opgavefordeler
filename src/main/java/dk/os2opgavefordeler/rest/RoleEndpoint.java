package dk.os2opgavefordeler.rest;

import dk.os2opgavefordeler.model.Role;
import dk.os2opgavefordeler.model.User;
import dk.os2opgavefordeler.service.*;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/roles")
public class RoleEndpoint {
	@Inject
	private Logger log;

	@Context
	private HttpServletRequest request;

	@Inject
	private UserService userService;

	@Inject
	private AuthService authenticationService;

	@Inject
	private AuthorizationService auth;

	@GET
	@Path("/{id}/substitutes")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSubstitutes(@PathParam("id") long roleId) {
		final User currentUser = authenticationService.getCurrentUser();
		try {
			final Role role = userService.findRoleById(roleId).orElseThrow(() -> new ResourceNotFoundException("Role not found"));
			auth.verifyCanActAs(role);
			return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
		}
		catch (ResourceNotFoundException ex) {
			log.warn("role#{} not found", roleId);
			return Response.status(Response.Status.NOT_FOUND).entity("Role not found").build();
		}
		catch (AuthorizationException e) {
			log.error("user {} unauthorized to act as role#{}", currentUser, roleId);
			return Response.status(Response.Status.UNAUTHORIZED).entity("Unauthorized").build();
		}
	}
}
