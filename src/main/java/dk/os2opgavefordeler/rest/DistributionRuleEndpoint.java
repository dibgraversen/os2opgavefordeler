package dk.os2opgavefordeler.rest;

import dk.os2opgavefordeler.model.DistributionRule;
import dk.os2opgavefordeler.model.presentation.DistributionRulePO;
import dk.os2opgavefordeler.service.DistributionService;
import org.slf4j.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.function.Consumer;

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
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
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
			result = distributionService.getPoDistributions(orgUnitId, false, true);
		}

		return Response.ok(result).build();
	}

	@POST
	@Path("/{distId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response updateResponsibleOrganization(@PathParam("distId") Integer distId, DistributionRulePO distribution)
	{
		if(distId == null || distribution == null) {
			log.info("updateResponsibleOrganization - bad request[{},{}]", distId, distribution);
			return Response.status(Response.Status.BAD_REQUEST).build();
		}

		//TODO: user/role access-check. Only Manager and Admin can modify.

		//TODO: multi-tenancy considerations. Do we pass municipality to service methods, or do we inject that in the
		//services? At any rate, make sure we can't get mess with other municipalities' data.
		return distributionService.getDistribution(distId)
			.map(existing -> doUpdateResponsibleOrganization(existing, distribution))
			.orElseGet(() -> {
					log.info("updateResponsibleOrganization - nonexisting distributionRule[{}]", distId);
					return Response.status(Response.Status.NOT_FOUND).build();
				}
			);
	}

	private Response doUpdateResponsibleOrganization(DistributionRule existing, DistributionRulePO updated) {
		//RULE: if a DistributionRule currently has no responsible, any manager can assign.
		//RULE: we can only change responsible if owned by <OrgUnit of logged-in user/role> or a subordinate.
		//RULE: if we own the DR, we can release ownership (set null), or give to *any* OrgUnit, not just subordinates.

		/*
		int currentUserOrgId = 1;									//TODO: get from logged-in user/role
		boolean isAdmin = false;									//TODO: get from logged-in user/role. Both municipality- and sysadmin qualify.
		boolean isManager = false;									//TODO: get from logged-in user/role
		int implicitOwner = 42;										//TODO: get implicit owning OrgUnit for existing.
		Set<Integer> subordinates = null;							//TODO: get subordinate Ids from implicitOwner;
		boolean unowned = existing.getResponsibleOrg() == 0;		//TODO: getResponsibleOrg() should be an Optional<OrgUnit>
		boolean canChange = isAdmin || (isManager && (unowned || currentUserOrgId == implicitOwner || subordinates.contains(currentUserOrgId)));
		//TODO: simplify 'canChange' expression - possible factor out authorization checks to service.
		//TODO: early-out if !isManager? Probably best to do all checks, and  do full logging of available information.
*/
		boolean canChange = true;									//TODO: when we have logged-in users, actually do checks.
		if(!canChange) {
			log.warn("User {} doesn't have permissions to update {}", "<WeDontHaveUsersYet>", existing);
			return Response.status(Response.Status.FORBIDDEN).build();
		}

		//TODO: verify that updated fields are valid. Should this be done up-front, or in the Consumer lambdas?
		//If in the lambdas, how do we handle invalid values? We'll need to either rollback transaction, or work on
		//a detached entity?

		//TODO: these updates should probably call service methods instead of setters. At some point, we might want to
		//calculate stuff and stuff with stuff on.
		possiblyUpdate(existing.getResponsibleOrg(),	updated.getResponsible(),	existing::setResponsibleOrg);
		possiblyUpdate(existing.getAssignedOrg(),		updated.getOrg(),			existing::setAssignedOrg);
		possiblyUpdate(existing.getAssignedEmp(),		updated.getEmployee(),		existing::setAssignedEmp);

		return Response.serverError().build();
	}

	private<T extends Comparable<T>> void possiblyUpdate(T oldVal, T newVal, Consumer<T> updater) {
		if(!newVal.equals(oldVal)) {
			updater.accept(newVal);
		}
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
