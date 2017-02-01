package dk.os2opgavefordeler.rest;

import dk.os2opgavefordeler.auth.AuthService;
import dk.os2opgavefordeler.auth.UserLoggedIn;
import dk.os2opgavefordeler.repository.MunicipalityRepository;
import dk.os2opgavefordeler.repository.UserRepository;
import dk.os2opgavefordeler.model.Municipality;
import dk.os2opgavefordeler.model.User;
import dk.os2opgavefordeler.model.presentation.*;
import dk.os2opgavefordeler.service.AuthorizationException;
import dk.os2opgavefordeler.service.ResourceNotFoundException;
import dk.os2opgavefordeler.service.UserService;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;

/**
 * @author hlo@miracle.dk
 */
@UserLoggedIn
@Path("/users")
public class UserEndpoint {
	@Inject
	private Logger log;

	@Inject
	private UserService userService;

	@Context
	private HttpServletRequest request;

	@Inject
	private UserRepository userRepository;

	@Inject
	private MunicipalityRepository municipalityRepository;

	@Inject
	private AuthService authService;

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	@NoCache
	public Response getUsers() {
		Optional<User> user = userService.findByEmail(authService.getAuthentication().getEmail());
		if (user.isPresent()) {
			if (userService.isAdmin(user.get().getId())) {
				return Response.ok().entity(userService.getAllUsers()).build();
			} else {
				return Response.status(Response.Status.UNAUTHORIZED).entity("Not authorized").build();
			}
		} else {
			return Response.status(Response.Status.NOT_FOUND).entity("User not found").build();
		}
	}

	@GET
	@Path("/me")
	@Produces(MediaType.APPLICATION_JSON)
	@NoCache
	public Response getUserInfo() {
		log.info("Returning user info for {}", authService.getAuthentication());
		try {
			return Response.ok().entity(new UserInfoPO(userRepository.findByEmail(authService.getAuthentication().getEmail())))
					.build();
		} catch (NoResultException nre){
			log.warn("Found no user with email: {}", authService.getAuthentication().getEmail());
			return Response.status(Response.Status.NOT_FOUND).entity("User not found").build();
		}
	}

	@POST
	@Path("/")
	@Consumes("application/json")
	@Produces("application/json")
	public Response create(User user) {
		// TODO admin functionality
		log.info("Creating user: {}", user.toString());

		Municipality municipality = municipalityRepository.findBy(user.getMunicipality().getId());
		user.setMunicipality(municipality);

		userService.createOrUpdateUser(user);

		return Response.ok().build();
	}

	@DELETE
	@Path("/{userId}")
	@Produces("application/json")
	public Response delete(@PathParam("userId") long userId) {
		// TODO admin functionality
		Optional<User> user = userService.findByEmail(authService.getAuthentication().getEmail());

		if (user.isPresent()) {
			if (userService.isAdmin(user.get().getId())) {
				User userToDelete = userRepository.findBy(userId);

				try {
					userService.removeUser(userToDelete);
				} catch (ResourceNotFoundException e) {
					return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
				} catch (AuthorizationException e) {
					return Response.status(Response.Status.UNAUTHORIZED).entity("Not authorized").build();
				}

				return Response.ok().build();
			} else {
				return Response.status(Response.Status.UNAUTHORIZED).entity("Not authorized").build();
			}
		} else {
			return Response.status(Response.Status.NOT_FOUND).entity("User not found").build();
		}
	}

	@GET
	@Path("/{userId}/roles")
	@Produces(MediaType.APPLICATION_JSON)
	@NoCache
	public List<RolePO> getRolesForUser(@PathParam("userId") long userId) {
		return userService.getRoles(userId);
	}

	@GET
	@Path("/{userId}/settings")
	@Produces(MediaType.APPLICATION_JSON)
	@NoCache
	public Response getSettingsForUser(@PathParam("userId") long userId) {
		if (userId == 0) {
			log.warn("invalid userId");
			return Response.status(Response.Status.BAD_REQUEST).entity("invalid userId").build();
		}
		return Response.ok(userService.getSettingsPO(userId)).build();
	}

	@POST
	@Path("/{userId}/settings")
	public void updateSettingsForUser(@PathParam("userId") long userId, UserSettingsPO settings) {
		settings.setUserId(userId);
		userService.updateSettings(settings);
	}
}
