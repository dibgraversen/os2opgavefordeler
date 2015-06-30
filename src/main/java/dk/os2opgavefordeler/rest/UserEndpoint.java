package dk.os2opgavefordeler.rest;

import dk.os2opgavefordeler.model.User;
import dk.os2opgavefordeler.model.presentation.RolePO;
import dk.os2opgavefordeler.model.presentation.UserInfoPO;
import dk.os2opgavefordeler.model.presentation.UserSettingsPO;
import dk.os2opgavefordeler.service.UserService;
import org.slf4j.Logger;

import javax.inject.Inject;
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
@Path("/users")
public class UserEndpoint {
	@Inject
	private Logger log;

	@Inject
	UserService usersService;

	@Context
	private HttpServletRequest request;

	@GET
	@Path("/me")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUserInfo() {
		//Since we do the following lookup-from-db-and-verify dance (to avoid stale sessions), we might perhaps as well
		//store just the userid. This is going the be changed to access tokens anyway, so leave be for now.

		final User user = (User) request.getSession().getAttribute("authenticated-user");
		final Optional<User> verifiedUser = (user == null) ? Optional.empty() : usersService.findById(user.getId());

		log.info("User from session: {}, db-verified: {}" , user, verifiedUser);

		final boolean valid = verifiedUser.map(u -> u.equals(user)).orElse(false);

		return valid ?
			Response.ok().entity(new UserInfoPO(user)).build() :
			Response.ok().entity(UserInfoPO.INVALID).build();
	}

	@GET
	@Path("/{userId}/roles")
	@Produces(MediaType.APPLICATION_JSON)
	public List<RolePO> getRolesForUser(@PathParam("userId") long userId) {
		List<RolePO> result = usersService.getRoles(userId);
		return result;
	}

	@GET
	@Path("/{userId}/settings")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSettingsForUser(@PathParam("userId") long userId) {
		if(userId == 0) {
			log.warn("invalid userId");
			return Response.status(Response.Status.BAD_REQUEST).entity("invalid userId").build();
		}
		return Response.ok(usersService.getSettingsPO(userId)).build();
	}

	@POST
	@Path("/{userId}/settings")
	public void updateSettingsForUser(@PathParam("userId") long userId, UserSettingsPO settings) {
		settings.setUserId(userId);
		usersService.updateSettings(settings);
	}
}
