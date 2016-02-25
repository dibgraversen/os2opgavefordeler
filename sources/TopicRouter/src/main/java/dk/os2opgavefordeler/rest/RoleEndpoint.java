package dk.os2opgavefordeler.rest;

import dk.os2opgavefordeler.auth.openid.OpenIdAuthenticationFlow;
import dk.os2opgavefordeler.model.Role;
import dk.os2opgavefordeler.model.presentation.SimpleMessage;
import dk.os2opgavefordeler.model.presentation.SubstitutePO;
import dk.os2opgavefordeler.service.*;
import dk.os2opgavefordeler.util.Validate;
import org.jboss.resteasy.annotations.cache.NoCache;
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
	private OpenIdAuthenticationFlow openIdAuthenticationFlow;



	@DELETE
	@Path("/{roleId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteRole(@PathParam("roleId") Long roleId) {
		log.info("deleteRole({})", roleId);
		try {
			Validate.nonZero(roleId, "Invalid roleId");
			userService.removeRole(roleId);

			return Response.noContent().build();
		}
		catch(ResourceNotFoundException e) {
			return Response.status(Response.Status.NOT_FOUND).entity(new SimpleMessage(e.getMessage())).build();
		}
		catch (AuthorizationException e) {
			log.error("current user not authorized to act as role#{}", roleId);
			return Response.status(Response.Status.UNAUTHORIZED).entity(new SimpleMessage("Unauthorized")).build();
		}
		catch(BadRequestArgumentException e) {
			return Response.status(Response.Status.BAD_REQUEST).entity(new SimpleMessage(e.getMessage())).build();
		}
	}

	@GET
	@Path("/{id}/substitutes")
	@Produces(MediaType.APPLICATION_JSON)
	@NoCache
	public Response getSubstitutes(@PathParam("id") Long roleId) {
		log.info("getSubstitutes({})", roleId);
		try {
			Validate.nonZero(roleId, "Invalid roleId");

			final List<SubstitutePO> substitutes = userService.findSubstitutesFor(roleId);
			return Response.status(Response.Status.OK).entity(substitutes).build();
		}
		catch (ResourceNotFoundException ex) {
			log.warn("role#{} not found", roleId);
			return Response.status(Response.Status.NOT_FOUND).entity(new SimpleMessage("Role not found")).build();
		}
		catch (AuthorizationException e) {
			log.error("current user not authorized to act as role#{}", roleId);
			return Response.status(Response.Status.UNAUTHORIZED).entity(new SimpleMessage("Unauthorized")).build();
		}
		catch(BadRequestArgumentException e) {
			return Response.status(Response.Status.BAD_REQUEST).entity(new SimpleMessage(e.getMessage())).build();
		}
	}

	@POST
	@Path("/{roleId}/substitutes/add")
	@Produces(MediaType.APPLICATION_JSON)
	public Response createSubstitute(@PathParam("roleId") Long roleId, @QueryParam("employmentId") Long employmentId) {
		log.info("createSubstitute({}, {})", roleId, employmentId);
		try {
			Validate.nonZero(roleId, "Invalid roleId");
			Validate.nonZero(employmentId, "Invalid employmentId");

			final Role role = userService.createSubstituteRole(employmentId, roleId);

			return Response.status(Response.Status.OK).entity(new SubstitutePO(role.getOwner().getName(), role.getId())).build();
		}
		catch(ResourceNotFoundException e) {
			return Response.status(Response.Status.NOT_FOUND).entity(new SimpleMessage(e.getMessage())).build();
		}
		catch(AuthorizationException e) {
			return Response.status(Response.Status.UNAUTHORIZED).entity(new SimpleMessage(e.getMessage())).build();
		}
		catch(BadRequestArgumentException e) {
			return Response.status(Response.Status.BAD_REQUEST).entity(new SimpleMessage(e.getMessage())).build();
		}
	}
}
