package dk.os2opgavefordeler.rest;

import dk.os2opgavefordeler.model.DistributionRule;
import dk.os2opgavefordeler.model.Employment;
import dk.os2opgavefordeler.model.OrgUnit;
import dk.os2opgavefordeler.model.Role;
import dk.os2opgavefordeler.model.presentation.DistributionRulePO;
import dk.os2opgavefordeler.service.DistributionService;
import dk.os2opgavefordeler.service.OrgUnitService;
import dk.os2opgavefordeler.service.PersistenceService;
import dk.os2opgavefordeler.service.UserService;
import org.slf4j.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Path("/distribution-rules")
@RequestScoped
public class DistributionRuleEndpoint extends Endpoint {
	@Inject
	Logger log;

	@Inject
	PersistenceService persistenceService;

	@Inject
	DistributionService distributionService;

	@Inject
	OrgUnitService orgUnitService;

	@Inject
	UserService userService;

	/**
	 * @param employmentId The employment for whom to look up TopicRoutes
	 * @param scope      The scope for which to get the TopicRoutes.
	 * @return a list of TopicRoutePO's matching the employment and scope.
	 */
	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response routesForEmployment(@QueryParam("role") Long roleId, @QueryParam("scope") DistributionRuleScope scope) {
		//TODO: define scopes properly, define Enum.

		log.info("routesForEmployment[{},{}]", roleId, scope);

		if (roleId == null || scope == null) {
			return badRequest("need role and scope");
		}

		final Optional<Role> role = userService.findRoleById(roleId);

		if (role.isPresent()) {
			final Optional<Employment> employment = role.get().getEmployment();

			if (employment.isPresent()) {
				final Optional<OrgUnit> orgUnit = employment.map(Employment::getEmployedIn);

				if (orgUnit.isPresent()) {
					// only display results if the user is the manager of the given organisation unit or the scope is ALL
					if (role.get().isManager() || scope == DistributionRuleScope.ALL) {
						return ok(distributionService.getPoDistributions(orgUnit.get(), scope));
					}
					else {
						return ok(new ArrayList<DistributionRulePO>());
					}
				}
				else {
					return badRequest("Kunne ikke finde ansættelsessted for bruger");
				}
			}
			else {
				return badRequest("Kunne ikke finde ansættelse for bruger");
			}
		}
		else {
			return badRequest("Kunne ikke finde rolle for bruger");
		}
	}

	@POST
	@Path("/{distId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response updateResponsibleOrganization(@PathParam("distId") Long distId, DistributionRulePO distribution)
	{
		if(distId == null || distribution == null) {
			log.info("updateResponsibleOrganization - bad request[{},{}]", distId, distribution);
			return badRequest("need distId and distribution object");
		}

		//TODO: user/role access-check. Only Manager and Admin can modify.

		//TODO: multi-tenancy considerations. Do we pass municipality to service methods, or do we inject that in the
		//services? At any rate, make sure we can't get mess with other municipalities' data.
		return distributionService.getDistribution(distId)
			.map(existing -> {
				log.info("updateResponsibleOrganization - updating resource");
				return doUpdateResponsibleOrganization(existing, distribution);
			})
			.orElseGet(() -> {
						log.info("updateResponsibleOrganization - nonexisting distributionRule[{}]", distId);
						return Response.status(Response.Status.NOT_FOUND).build();
					}
			);
	}

	@GET
	@Path("/{distId}/children")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getChildren(@PathParam("distId") Long distId, @QueryParam("employment") Long employmentId, @QueryParam("scope") DistributionRuleScope scope){
		if (distId == null || employmentId == null || scope == null) {
			log.info("#getChildren with no distId, employmentId and scope");
			return badRequest("You need to specify valid distributionId, employmentId and scope as part of the url.");
		}
		else {
			final Optional<Employment> employment = orgUnitService.getEmployment(employmentId);
			final Optional<OrgUnit> orgUnit = employment.map(Employment::getEmployedIn);

			if (orgUnit.isPresent()) {
				List<DistributionRulePO> result = distributionService.getChildren(distId, orgUnit.get(), scope)
						.stream().map(DistributionRulePO::new).collect(Collectors.toList());
				return ok(result);
			}
			else {
				return badRequest("Could not find organizational unit for user");
			}
		}
	}

	@GET
	@Path("/buildRules")
	public Response buildRulesForMunicipality(@QueryParam("municipalityId") long municipalityId){
		if(municipalityId < 0) {
			log.info("buildRules - bad request[{}]", municipalityId);
			return badRequest("municipalityId needed");
		}
		distributionService.buildRulesForMunicipality(municipalityId);
		return ok();
	}

	//TODO: code below this point should probably be refactored to service methods.
	private Response doUpdateResponsibleOrganization(DistributionRule existing, DistributionRulePO updated) {
		if(!allowedToUpdate(existing)) {
			log.warn("User {} doesn't have permissions to update {}", "<WeDontHaveUsersYet>", existing);
			return Response.status(Response.Status.FORBIDDEN).build();
		}
		try {
			updateDistributionRule(existing, updated);
		}
		catch(IllegalArgumentException ex) {
			log.warn("doUpdateResponsibleOrganization - invalid arguments in [{}]", updated);
			// persistenceService.rollbackTransaction();	// if we move logic to DistributionService, perform rollback.
			return Response.serverError().build();
		}

		return Response.ok().build();
	}

	private boolean allowedToUpdate(DistributionRule existing) {
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
		//TODO: when we have logged-in users, actually do checks.
		return true;
	}

	private void updateDistributionRule(DistributionRule existing, DistributionRulePO updated) {
		//TODO: these updates should probably call service methods instead of setters. At some point, we might want to
		//calculate stuff and stuff with stuff on.
		updateIfChanged(existing.getResponsibleOrg().map(OrgUnit::getId).orElse(0L), updated.getResponsible(), newOwnerId -> {
			OrgUnit newOwner = (newOwnerId == 0) ? null :
				orgUnitService.getOrgUnit(newOwnerId).orElseThrow(IllegalArgumentException::new);
			existing.setResponsibleOrg(newOwner);
		});

		updateIfChanged(existing.getAssignedOrg().map(OrgUnit::getId).orElse(0L), updated.getOrg(), newOrgId -> {
			OrgUnit newOrg = (newOrgId == 0) ? null :
				orgUnitService.getOrgUnit(newOrgId).orElseThrow(IllegalArgumentException::new);
			existing.setAssignedOrg(newOrg);
		});

		updateIfChanged(existing.getAssignedEmp(), updated.getEmployee(), existing::setAssignedEmp);

		distributionService.createDistributionRule(existing); // if we move logic to DistributionService, 'existing' is managed and this shouldn't be necessary.
	}

	private<T extends Comparable<T>> void updateIfChanged(T oldVal, T newVal, Consumer<T> updater) {
		if(!newVal.equals(oldVal)) {
			updater.accept(newVal);
		}
	}
}
