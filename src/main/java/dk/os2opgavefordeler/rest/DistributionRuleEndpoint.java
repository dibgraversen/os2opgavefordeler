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
	 * @param employmentId The employment for whom to look up TopicRoutes
	 * @param scope      The scope for which to get the TopicRoutes. Can be ALL, MINE or ALL_MINE.
	 * @return a list of TopicRoutePO's matching the employment and scope.
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response routesForEmployment(@QueryParam("employment") Integer employmentId, @QueryParam("scope") String scope) {
		//TODO: define scopes properly, define Enum.

		log.info("routesForEmployment[{},{}]", employmentId, scope);

		if (employmentId == null || scope == null) {
			return Response.status(Response.Status.BAD_REQUEST).build();
		}

		//TODO: what if employment != orgUnit.manager ?
		int orgUnitId = getOrgUnitFromEmploymentId(employmentId);


		final List<DistributionRulePO> result;
		if("ALL".equals(scope)) {
			result = distributionService.getPoDistributionsAll();
		} else {
			result = distributionService.getPoDistributions(orgUnitId, true);
		}

		return Response.ok(result).build();
	}

	private int getOrgUnitFromEmploymentId(int employmentId) {
		//TODO: perform proper lookup(employment -> orgunit)
		switch(employmentId) {
			case 1:
				return 1;

			case 2:
			case 3:
				return 2;
			case 4:
			case 5:
				return 3;
			case 6:
			case 7:
				return 4;

			default:
				return -1;
		}
	}
}
