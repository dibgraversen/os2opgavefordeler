package dk.os2opgavefordeler.rest;

import dk.os2opgavefordeler.model.Role;
import dk.os2opgavefordeler.model.presentation.RolePO;
import dk.os2opgavefordeler.model.presentation.SubstitutePO;
import dk.os2opgavefordeler.service.*;
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
	private EmploymentService employmentService;

	@Inject
	private AuthenticationService authenticationService;

	@GET
	@Path("/{id}/substitutes")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSubstitutes(@PathParam("id") Long roleId) {
		try {
			validateNonzero(roleId, "Invalid roleId");

			final List<SubstitutePO> substitutes = userService.findSubstitutesFor(roleId);
			return Response.status(Response.Status.OK).entity(substitutes).build();
		}
		catch (ResourceNotFoundException ex) {
			log.warn("role#{} not found", roleId);
			return Response.status(Response.Status.NOT_FOUND).entity("Role not found").build();
		}
		catch (AuthorizationException e) {
			log.error("current user unauthorized to act as role#{}", roleId);
			return Response.status(Response.Status.UNAUTHORIZED).entity("Unauthorized").build();
		}
		catch(BadRequestArgumentException e) {
			return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@POST
	@Path("/{roleId}/substitutes/add")
	@Produces(MediaType.APPLICATION_JSON)
	public Response createSubstitute(@PathParam("roleId") Long roleId, @QueryParam("employmentId") Long employmentId) {
		try {
			validateNonzero(roleId, "Invalid roleId");
			validateNonzero(employmentId, "Invalid employmentId");

			log.info("createSubstitute {}->{}", roleId, employmentId);

			final Role role = userService.createSubstituteRole(employmentId, roleId);

			return Response.status(Response.Status.OK).entity(new RolePO(role)).build();
		}
		catch(ResourceNotFoundException e) {
			return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
		}
		catch(AuthorizationException e) {
			return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
		}
		catch(BadRequestArgumentException e) {
			return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	private static void validateNonzero(Long value, String message) throws BadRequestArgumentException
	{
		if(value == null || value == 0) {
			throw new BadRequestArgumentException(message);
		}
	}
}
