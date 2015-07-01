package dk.osto.rest;

import dk.osto.data.TaskRouteMockProvider;
import dk.osto.data.TopicRouteMockProvider;
import dk.osto.model.TaskRoute;
import dk.osto.model.presentation.TopicRoutePO;

import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * @author hlo@miracle.dk
 */
@Path("/taskRoutes")
public class TaskRouteRESTService {
	private static final String ALL = "all";
	private static final String MINE = "mine";
	private static final String ALL_MINE = "allMine";

	/**
	 *
	 * @param employment The employment for whom to look up TopicRoutes
	 * @param scope The scope for which to get the TopicRoutes. Can be ALL, MINE or ALL_MINE.
	 * @return a list of TopicRoutePO's matching the employment and scope.
	 */
	@GET
	@Produces("application/json")
	public List<TopicRoutePO> employmentRoutes(@QueryParam("employment")int employment, @QueryParam("scope")String scope){
		return new TopicRouteMockProvider().employmentRoutes(employment, scope);
	}

	@OPTIONS
	@Produces("application/json")
	public Response getOptions(){
		Response response = Response.ok().build();
//		response.getMetadata().putSingle("Access-Control-Allow-Origin", "*");
//		response.getMetadata().putSingle("Access-Control-Allow-Headers", "Content-Type, X-Requested-With");
//		response.getMetadata().putSingle("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
		return response;
	}

	// ret distribution rule.
	  // liste af orgs.
	  // liste af brugere.
	// ret ansvar
	  // liste af orgs.

	// REST interface
	  // enkelt opslag baseret på KLE men scopet af kommune.

	// settings
	// kommune interface.
	  // CRUD org.
	// sysadm interface.
	  // CRUD kommuner.
	  // Upload KLE struktur.


	/**
	 * Example endpoint
	 * @return a list of TaskRoutes.
	 */
//	@GET
//	@Produces("application/json")
//	public List<TaskRoute> listRoutes() {
//		return TaskRouteMockProvider.provideTaskRoutes();
//	}


}
