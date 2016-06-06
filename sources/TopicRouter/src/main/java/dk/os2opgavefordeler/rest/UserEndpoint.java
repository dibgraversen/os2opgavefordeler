package dk.os2opgavefordeler.rest;

import dk.os2opgavefordeler.auth.AuthService;
import dk.os2opgavefordeler.employment.MunicipalityRepository;
import dk.os2opgavefordeler.employment.UserRepository;
import dk.os2opgavefordeler.model.Municipality;
import dk.os2opgavefordeler.model.Role;
import dk.os2opgavefordeler.model.User;
import dk.os2opgavefordeler.model.presentation.RolePO;
import dk.os2opgavefordeler.model.presentation.UserInfoPO;
import dk.os2opgavefordeler.model.presentation.UserRolePO;
import dk.os2opgavefordeler.model.presentation.UserSettingsPO;
import dk.os2opgavefordeler.service.UserService;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author hlo@miracle.dk
 */
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
		if (!authService.isAuthenticated()) {
			return Response.status(Response.Status.UNAUTHORIZED).entity("Not logged in").build();
		}

		Optional<User> user = userService.findByEmail(authService.getAuthentication().getEmail());

		if (user.isPresent()) {
			if (userService.isAdmin(user.get().getId())) {
				log.info("Returning user list");

				return Response.ok().entity(userService.getAllUsers()).build();
			}
			else {
				log.warn("Unauthorized attempt to get user list");

				return Response.status(Response.Status.UNAUTHORIZED).entity("Not authorized").build();
			}
		}
		else {
			return Response.status(Response.Status.NOT_FOUND).entity("User not found").build();
		}
	}



    @GET
    @Path("/me")
    @Produces(MediaType.APPLICATION_JSON)
    @NoCache
    public Response getUserInfo() {
        log.info("Returning user info for {}", authService.getAuthentication());
        if (!authService.isAuthenticated()) {
            return Response.ok().entity(UserInfoPO.INVALID).build();
        }
        return Response.ok().entity(new UserInfoPO(userRepository.findByEmail(authService.getAuthentication().getEmail())))
        .build();
    }

    @POST
    @Path("/")
    @Consumes("application/json")
    @Produces("application/json")
    public User create(User user) {
        Municipality municipality = municipalityRepository.findBy(user.getMunicipality().getId());
        user.setMunicipality(municipality);

        return userRepository.save(user);
    }

    @DELETE
    @Path("/{userId}")
    @Produces("application/json")
    public Response delete(@PathParam("userId") long userId) {
        User user = userRepository.findBy(userId);
        for (Role r : user.getRoles()) {
            user.removeRole(r);
        }
        userRepository.removeAndFlush(user);
        return Response.ok().build();
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
