package dk.os2opgavefordeler.rest;

import dk.os2opgavefordeler.model.presentation.RolePO;
import dk.os2opgavefordeler.model.presentation.UserInfoPO;
import dk.os2opgavefordeler.model.presentation.UserSettingsPO;
import dk.os2opgavefordeler.service.UsersService;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * @author hlo@miracle.dk
 */
@Path("/user")
public class UsersEndpoint {
	@Inject
	private Logger log;

	@Inject
	UsersService usersService;

	@GET
	@Path("/info")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUserInfo() {
		return Response.ok().entity(new UserInfoPO()).build();
	}

	@GET
	@Path("/{userId}/roles")
	@Produces(MediaType.APPLICATION_JSON)
	public List<RolePO> getRolesForUser(@PathParam("userId") long userId){
		List<RolePO> result = usersService.getRoles(userId);
		return result;
	}

	@GET
	@Path("/{userId}/settings")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSettingsForUser(@PathParam("userId") long userId){
		if(userId == 0){
			log.warn("invalid userId");
			return Response.status(Response.Status.BAD_REQUEST).entity("invalid userId").build();
		}
		return Response.ok(usersService.getSettings(userId)).build();
	}

	@POST
	@Path("/{userId}/settings")
	public void updateSettingsForUser(@PathParam("userId") long userId, UserSettingsPO settings){
		settings.setUserId(userId);
		usersService.updateSettings(settings);
	}
}
