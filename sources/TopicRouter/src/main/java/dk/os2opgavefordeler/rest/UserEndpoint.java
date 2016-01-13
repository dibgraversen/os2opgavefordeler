package dk.os2opgavefordeler.rest;

import dk.os2opgavefordeler.auth.CurrentUser;
import dk.os2opgavefordeler.auth.LoginController;
import dk.os2opgavefordeler.model.User;
import dk.os2opgavefordeler.model.presentation.RolePO;
import dk.os2opgavefordeler.model.presentation.UserInfoPO;
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
import java.util.List;

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
	private LoginController loginController;

	@Inject
	@CurrentUser
	private User currentUser;

	@GET
	@Path("/me")
	@Produces(MediaType.APPLICATION_JSON)
	@NoCache
	public Response getUserInfo() {
		if(currentUser == null){
			return Response.ok().entity(UserInfoPO.INVALID).build();
		}
		return Response.ok().entity(new UserInfoPO(currentUser)).build();
	}

	@GET
	@Path("/{userId}/roles")
	@Produces(MediaType.APPLICATION_JSON)
	@NoCache
	public List<RolePO> getRolesForUser(@PathParam("userId") long userId) {
		List<RolePO> result = userService.getRoles(userId);
		return result;
	}

	@GET
	@Path("/{userId}/settings")
	@Produces(MediaType.APPLICATION_JSON)
	@NoCache
	public Response getSettingsForUser(@PathParam("userId") long userId) {
		if(userId == 0) {
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
