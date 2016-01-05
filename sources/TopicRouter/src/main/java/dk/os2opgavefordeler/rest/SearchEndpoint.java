package dk.os2opgavefordeler.rest;

import dk.os2opgavefordeler.model.presentation.EmploymentPO;
import dk.os2opgavefordeler.model.search.EmploymentSearch;
import dk.os2opgavefordeler.model.search.SearchResult;
import dk.os2opgavefordeler.service.EmploymentService;
import org.slf4j.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author hlo@miracle.dk
 */
@Path("/search")
@RequestScoped
public class SearchEndpoint {

	@Inject
	Logger log;

	@Inject
	EmploymentService employmentService;

	@POST
	@Path("/employments")
	@Produces(MediaType.APPLICATION_JSON)
	public Response findEmployment(EmploymentSearch search){
		SearchResult<EmploymentPO> result = employmentService.search(search);
		return Response.ok().entity(result).build();
	}
}
