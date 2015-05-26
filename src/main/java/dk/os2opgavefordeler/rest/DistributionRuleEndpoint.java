package dk.os2opgavefordeler.rest;

import dk.os2opgavefordeler.model.presentation.DistributionRulePO;
import dk.os2opgavefordeler.service.DistributionService;
import org.slf4j.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/distribution-rules")
@RequestScoped
public class DistributionRuleEndpoint {
	@Inject
	Logger log;

	@Inject
	DistributionService distributionService;

	/**
	 * @param employment The employment for whom to look up TopicRoutes
	 * @param scope      The scope for which to get the TopicRoutes. Can be ALL, MINE or ALL_MINE.
	 * @return a list of TopicRoutePO's matching the employment and scope.
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response routesForEmployment(@QueryParam("employment") Integer employment, @QueryParam("scope") String scope) {
		//TODO: should we perform a lookup(employment -> orgunit), or should the Angular frontend pass OU instead of Emp?
		//TODO: define scopes properly, define Enum.

		log.info("routesForEmployment[{},{}]", employment, scope);

		if (employment == null || scope == null) {
			return Response.status(Response.Status.BAD_REQUEST).build();
		}

		boolean unassigned = "ALL".equals(scope);

		final List<DistributionRulePO> result = distributionService.getPoDistributions(employment, unassigned);

		return Response.ok(result).build();
	}
}
