package dk.osto.rest;

import dk.osto.data.TaskRouteMockProvider;
import dk.osto.model.TaskRoute;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.List;

/**
 * @author hlo@miracle.dk
 */
@Path("/taskRoutes")
public class TaskRouteRESTService {

	@GET
	@Produces("application/json")
	public List<TaskRoute> listRoutes() {
		return TaskRouteMockProvider.provideTaskRoutes();
	}


}
