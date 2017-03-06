package dk.os2opgavefordeler.rest;

import dk.os2opgavefordeler.auth.AdminRequired;
import dk.os2opgavefordeler.auth.AuthService;
import dk.os2opgavefordeler.auth.UserLoggedIn;
import dk.os2opgavefordeler.auth.openid.OpenIdAuthenticationFlow;
import dk.os2opgavefordeler.logging.AuditLogged;
import dk.os2opgavefordeler.model.Role;
import dk.os2opgavefordeler.model.presentation.SimpleMessage;
import dk.os2opgavefordeler.model.presentation.SubstitutePO;
import dk.os2opgavefordeler.repository.EmploymentRepository;
import dk.os2opgavefordeler.repository.UserRepository;
import dk.os2opgavefordeler.service.*;
import dk.os2opgavefordeler.util.Validate;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@UserLoggedIn
@AuditLogged
@Path("/roles")
public class RoleEndpoint {

	@Inject
	private Logger log;

	@Inject
	private UserService userService;

	@Inject
	private UserRepository userRepo;

	@Inject
//	private EmploymentService employmentService;
  private EmploymentRepository employmentRepo;

	@Inject
	private AuthService authService;

	@Inject
	private OpenIdAuthenticationFlow openIdAuthenticationFlow;

	private static final String INVALID_ROLE_ID = "Invalid roleId";
	private static final String INVALID_EMPLOYMENT_ID = "Invalid employmentId";

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	@NoCache
	public Response getRoles() {
		if (userService.isAdmin(authService.getAuthentication().getEmail())) {
			return Response.ok().entity(userService.getAllRoles()).build();
		}
		else {
			return Response.status(Response.Status.UNAUTHORIZED).entity("Not authorized").build();
		}

	}

	// TODO verify ownership of role.
	@DELETE
	@Path("/{roleId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Deprecated
	public Response deleteRole(@PathParam("roleId") Long roleId) {
		log.info("deleteRole({})", roleId);

		try {
			Validate.nonZero(roleId, INVALID_ROLE_ID);
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
			Validate.nonZero(roleId, INVALID_ROLE_ID);

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

	/**
	 * Creates a substitute role for the given role.
	 * @param roleId This is the role that is to be substituted.
	 * @param employmentId This employmentId specifies the substitute.
	 * @return A role that now substitutes given role, assigned to the user matching the given employmentId.
	 */
	@POST
	@Path("/{roleId}/substitutes/add")
	@Produces(MediaType.APPLICATION_JSON)
	public Response createSubstitute(@PathParam("roleId") Long roleId, @QueryParam("employmentId") Long employmentId) {
		log.info("createSubstitute({}, {})", roleId, employmentId);

		try {
			Validate.nonZero(roleId, INVALID_ROLE_ID);
			Validate.nonZero(employmentId, INVALID_EMPLOYMENT_ID);

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

	@POST
	@Path("/{roleId}/municipalityadmin/{valueId}")
	@Produces(MediaType.APPLICATION_JSON)
	@AdminRequired
	public Response setMunicipalityAdmin(@PathParam("roleId") Long roleId, @PathParam("valueId") Long valueId) {
		if (userService.isAdmin(authService.getAuthentication().getEmail())) {
			try {
				Validate.nonZero(roleId, INVALID_ROLE_ID);

				Optional<Role> role = userService.findRoleById(roleId);

				if (role.isPresent()) {
					Role updatedRole = role.get();
					updatedRole.setMunicipalityAdmin(valueId == 1);
					userService.updateRole(updatedRole);

					return Response.status(Response.Status.OK).entity(new SimpleMessage("OK")).build();
				}
				else {
					log.warn("Role #{} not found", roleId);
					return Response.status(Response.Status.NOT_FOUND).entity(new SimpleMessage("Role not found")).build();
				}
			}
			catch(BadRequestArgumentException e) {
				return Response.status(Response.Status.BAD_REQUEST).entity(new SimpleMessage(e.getMessage())).build();
			}
		}
		else {
			return Response.status(Response.Status.UNAUTHORIZED).entity(new SimpleMessage("Unauthorized")).build();
		}
	}

	@POST
	@Path("/{roleId}/admin/{valueId}")
	@Produces(MediaType.APPLICATION_JSON)
	@AdminRequired
	public Response setAdmin(@PathParam("roleId") Long roleId, @PathParam("valueId") Long valueId) {
		if (userService.isAdmin(authService.getAuthentication().getEmail())) {
			try {
				Validate.nonZero(roleId, INVALID_ROLE_ID);

				Optional<Role> role = userService.findRoleById(roleId);

				if (role.isPresent()) {
					Role updatedRole = role.get();
					updatedRole.setAdmin(valueId == 1);
					userService.updateRole(updatedRole);

					return Response.status(Response.Status.OK).entity(new SimpleMessage("OK")).build();
				}
				else {
					log.warn("Role #{} not found", roleId);
					return Response.status(Response.Status.NOT_FOUND).entity(new SimpleMessage("Role not found")).build();
				}
			}
			catch(BadRequestArgumentException e) {
				return Response.status(Response.Status.BAD_REQUEST).entity(new SimpleMessage(e.getMessage())).build();
			}
		}
		else {
			return Response.status(Response.Status.UNAUTHORIZED).entity(new SimpleMessage("Unauthorized")).build();
		}
	}

	@POST
	@Path("/{roleId}/kleAdmin/{valueId}")
	@Produces(MediaType.APPLICATION_JSON)
	@AdminRequired
	public Response setKleAdmin(@PathParam("roleId") Long roleId, @PathParam("valueId") Long valueId) {
		if (userService.isAdmin(authService.getAuthentication().getEmail())) {
			try {
				Validate.nonZero(roleId, INVALID_ROLE_ID);

				Optional<Role> role = userService.findRoleById(roleId);

				if (role.isPresent()) {
					Role updatedRole = role.get();
					updatedRole.setKleAssigner(valueId==1);
					userService.updateRole(updatedRole);

					return Response.status(Response.Status.OK).entity(new SimpleMessage("OK")).build();
				}
				else {
					log.warn("Role #{} not found", roleId);
					return Response.status(Response.Status.NOT_FOUND).entity(new SimpleMessage("Role not found")).build();
				}
			}
			catch(BadRequestArgumentException e) {
				return Response.status(Response.Status.BAD_REQUEST).entity(new SimpleMessage(e.getMessage())).build();
			}
		}
		else {
			return Response.status(Response.Status.UNAUTHORIZED).entity(new SimpleMessage("Unauthorized")).build();
		}
	}
}