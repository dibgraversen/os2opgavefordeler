package dk.osto.data;

import dk.osto.model.TaskRoute;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hlo@miracle.dk
 */
public class TaskRouteMockProvider {
	public static List<TaskRoute> provideTaskRoutes(){
		List<TaskRoute> result = new ArrayList<TaskRoute>();
		result.add(createTaskRoute(1L, "Første opgavefordeling"));
		result.add(createTaskRoute(2L, "Anden opgavefordeling"));
		return result;
	}

	private static TaskRoute createTaskRoute(Long id, String name){
		TaskRoute result = new TaskRoute();
		result.setId(id);
		result.setName(name);
		return result;
	}
}
