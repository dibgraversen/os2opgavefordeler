package dk.os2opgavefordeler.rest;

import dk.os2opgavefordeler.model.Role;
import dk.os2opgavefordeler.model.User;
import dk.os2opgavefordeler.service.AuthService;
import dk.os2opgavefordeler.service.AuthorizationException;
import dk.os2opgavefordeler.service.ResourceNotFoundException;
import dk.os2opgavefordeler.service.UserService;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/roles")
public class RoleEndpoint {
	@Inject
	private Logger log;

	@Inject
	private UserService userService;

	@Inject
	private AuthService authenticationService;

	@GET
	@Path("/{id}/substitutes")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSubstitutes(@PathParam("id") Long roleId) {
		if(roleId == null || roleId == 0) {
			return Response.status(Response.Status.BAD_REQUEST).entity("Invalid roleId").build();
		}

		final User currentUser = authenticationService.getCurrentUser();
		try {
			final List<Role> substitutes = userService.findSubstitutesFor(roleId);
			return Response.status(Response.Status.OK).entity(substitutes).build();
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

	@POST
	@Path("/{roleId}/substitutes/{employmentId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response createSubstitute() {
		return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
	}
}
