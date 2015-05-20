package dk.os2opgavefordeler.rest;

import dk.os2opgavefordeler.model.presentation.FilterScope;
import dk.os2opgavefordeler.model.presentation.UserSettingsPO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author hlo@miracle.dk
 */
@Path("/settings")
@Deprecated
public class SettingsEndpoint {
	private final Logger log = LoggerFactory.getLogger(getClass());

	@GET
	@Path("/{userId}")
	@Produces(MediaType.APPLICATION_JSON)
	public UserSettingsPO getSettings(@PathParam("userId") int userId){
		log.warn("userId: "+userId);
		UserSettingsPO result = new UserSettingsPO();
		result.setScope(FilterScope.ALL);
		result.setShowResponsible(false);
		result.setShowExpandedOrg(false);
		return result;
	}

	@POST
	@Path("/{userId}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateSettings(@PathParam("userId") int userId, UserSettingsPO settingsPO){
		log.warn("userId: " + userId);
		log.warn("settingsPO: " + settingsPO);
		return Response.ok().build();
	}

	@OPTIONS
	@Produces(MediaType.APPLICATION_JSON)
	public Response getOptions(){
		return Response.ok().build();
	}
}
