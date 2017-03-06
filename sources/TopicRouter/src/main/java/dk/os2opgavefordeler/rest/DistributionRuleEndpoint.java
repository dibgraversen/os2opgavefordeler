package dk.os2opgavefordeler.rest;

import dk.os2opgavefordeler.auth.AdminRequired;
import dk.os2opgavefordeler.auth.AuthService;
import dk.os2opgavefordeler.auth.MunicipalityAdminRequired;
import dk.os2opgavefordeler.auth.UserLoggedIn;
import dk.os2opgavefordeler.logging.AuditLogged;
import dk.os2opgavefordeler.logging.AuditLogger;
import dk.os2opgavefordeler.model.*;
import dk.os2opgavefordeler.model.presentation.DistributionRulePO;
import dk.os2opgavefordeler.model.presentation.FilterNamePO;
import dk.os2opgavefordeler.service.*;
import org.slf4j.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@UserLoggedIn
@AuditLogged
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
	EmploymentService employmentService;

	@Inject
	KleService kleService;

	@Inject
	UserService userService;

	@Inject
	private AuthService authService;

	@Inject
	private AuditLogger auditLogger;

	private static final String USER_NOT_FOUND = "Bruger ikke fundet.";
	private static final String INVALID_MUNICIPALITY_ID = "Denne bruger er ikke tilknyttet aktiv kommune.";
	private static final String NO_ORGUNIT_FOUND_FOR_USER = "Bruger er ikke knyttet til afdeling, derfor kan der ikke findes fordelingsregler.";
	private static final String NO_EMPLOYMENT_FOUND_FOR_USER = "Kan ikke finde ansættelse for bruger, derfor kan der ikke findes fordelingsregler.";
	private static final String NO_ROLE_FOUND_FOR_USER = "Kan ikke finde rolle for bruger.";

	private static final String RESPONSIBILITY_UPDATE_TYPE = "responsibility";
	private static final String DISTRIBUTION_UPDATE_TYPE = "distribution";

	private final List<String> validUpdateTypes = Arrays.asList("responsibility", "distribution");

	/**
	 * Returns a list of distribution rules for the specified role and scope.
	 *
	 * @param roleId The role for which to get the distribution rules
	 * @param scope The scope for looking up distribution rules
	 * @return list of DistributionRulePO's matching the role and scope
	 */
	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response routesForEmployment(@QueryParam("role") Long roleId, @QueryParam("scope") DistributionRuleScope scope) {
		log.info("routesForEmployment[{},{}]", roleId, scope);

		if (roleId == null || scope == null) {
			return badRequest("Invalid role and/or scope");
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
					return badRequest(NO_ORGUNIT_FOUND_FOR_USER);
				}
			}
			else {
				return badRequest(NO_EMPLOYMENT_FOUND_FOR_USER);
			}
		}
		else {
			return badRequest(NO_ROLE_FOUND_FOR_USER);
		}
	}

	@POST
	@Path("/{distId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response updateResponsibleOrganization(@PathParam("distId") Long distId, DistributionRulePO distribution, @QueryParam("type") String type) {
		// comp: validate
		if (distId == null || distribution == null) {
			log.warn("updateResponsibleOrganization - bad request[{},{}]", distId, distribution);
			return badRequest("need distId and distribution object");
		}
		if(!validUpdateTypes.contains(type)){
			log.warn("invalid update type given. Type: "+type);
			return badRequest("Invalid type given as parameter.");
		}
		Optional<User> user = userService.findByEmail(authService.getAuthentication().getEmail());
		if(!user.isPresent()){
			log.warn("returning from user not found.");
			return Response.status(Response.Status.NOT_FOUND).entity(USER_NOT_FOUND).build();
		}
		long userId = user.get().getId();
		if (hasUpdatePermission(userId)) { // only managers and admins can update responsibility
			final String userStr = user.get().getEmail();
			final Kle kle = kleService.getKle(distribution.getKle().getId());
			final String kleStr = kle != null && !kle.getNumber().isEmpty() ? kle.getNumber() : "";
			final Municipality municipality = user.get().getMunicipality();
			final Optional<DistributionRule> existingDistributionRule = distributionService.getDistribution(distId);

			String operationType = "";
			String eventType = "";
			String orgUnitStr = "";
			String employmentStr = "";
			String dataStr = "";

			if (updatingResponsible(type)) {
				eventType = LogEntry.RESPONSIBILITY_TYPE;

				if (distribution.getResponsible() == 0) { // deleting responsible org unit
					operationType = LogEntry.DELETE_TYPE;
				}
				else {
					if (!existingDistributionRule.isPresent()) { // distribution rule didn't already exist
						operationType = LogEntry.CREATE_TYPE;
					}
					else { // distribution rule exists
						if (existingDistributionRule.get().getResponsibleOrg().isPresent()) { // responsible org is set
							operationType = LogEntry.UPDATE_TYPE;
						}
						else {
							operationType = LogEntry.CREATE_TYPE;
						}
					}

					// fetch organisational unit
					Optional<OrgUnit> orgUnit = orgUnitService.getOrgUnit(distribution.getResponsible());
					orgUnitStr = orgUnit.isPresent() ? orgUnit.get().getName() + " (" + orgUnit.get().getBusinessKey() + ")"  : "";
				}
			}	else if (DISTRIBUTION_UPDATE_TYPE.equals(type)) {
				eventType = LogEntry.DISTRIBUTION_TYPE;

				if (distribution.getOrg() == 0 && distribution.getEmployee() == 0) { // deleting distribution rule
					operationType = LogEntry.DELETE_TYPE;
				}
				else {
					if (!existingDistributionRule.isPresent()) { // distribution rule didn't already exist
						operationType = LogEntry.CREATE_TYPE;
					}
					else { // distribution rule exists
						if (existingDistributionRule.get().getAssignedOrg().isPresent()) { // assigned org is set
							operationType = LogEntry.UPDATE_TYPE;
						}
						else {
							operationType = LogEntry.CREATE_TYPE;
						}
					}
				}

				// fetch organisational unit
				Optional<OrgUnit> orgUnit = orgUnitService.getOrgUnit(distribution.getOrg());
				orgUnitStr = orgUnit.isPresent() ? orgUnit.get().getName() + " (" + orgUnit.get().getBusinessKey() + ")"  : "";

				// fetch employment
				Optional<Employment> employment = employmentService.getEmployment(distribution.getEmployee());
				employmentStr = employment.isPresent() ? employment.get().getName() + " (" + employment.get().getInitials() + ")": "";
			} else {
				log.warn("why do we end up here? type: "+type);
			}

			// log event
			auditLogger.event(kleStr, userStr, operationType, eventType, dataStr, orgUnitStr, employmentStr, municipality);

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
		} else {
			log.warn("returned since user does not have permission to update. User: "+user);
			return badRequest("Does not have permission to update");
		}

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
				return badRequest(NO_ORGUNIT_FOUND_FOR_USER);
			}
		}
	}

	@GET
	@Path("/buildRules")
	@AdminRequired
	public Response buildRulesForMunicipality(@QueryParam("municipalityId") long municipalityId) {
		if (municipalityId < 0) {
			log.info("buildRules - bad request[{}]", municipalityId);
			return badRequest(INVALID_MUNICIPALITY_ID);
		}

		distributionService.buildRulesForMunicipality(municipalityId);

		return ok();
	}

	@GET
	@Path("/text/names")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response getFilterNamesText(@QueryParam("municipalityId") long municipalityId) {
		if (municipalityId < 0) {
			log.info("#getFilterNames with no municipalityId");
			return badRequest(INVALID_MUNICIPALITY_ID);
		}
		else {
			return ok(distributionService.getFilterNamesText(municipalityId));
		}
	}

	@POST
	@Path("/text/names")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	@MunicipalityAdminRequired
	public Response updateTextFilterName(@QueryParam("municipalityId") long municipalityId, FilterNamePO filterNamePO) {
		if (municipalityId < 0) {
			log.info("#getFilterNames with no municipalityId");
			return badRequest(INVALID_MUNICIPALITY_ID);
		}
		else {
			return ok(distributionService.updateFilterName(municipalityId, filterNamePO));
		}
	}

	@DELETE
	@Path("/text/names/{filterNameId}")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	@MunicipalityAdminRequired
	public Response deleteTextFilterName(@PathParam("filterNameId") Long filterNameId, @QueryParam("municipalityId") long municipalityId) {
		if (municipalityId < 0) {
			log.info("#deleteFilterName with no municipalityId");
			return badRequest(INVALID_MUNICIPALITY_ID);
		}
		else {
			distributionService.deleteFilterName(municipalityId, filterNameId);

			return ok();
		}
	}

	@DELETE
	@Path("/date/names/{filterNameId}")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	@MunicipalityAdminRequired
	public Response deleteDateFilterName(@PathParam("filterNameId") Long filterNameId, @QueryParam("municipalityId") long municipalityId) {
		if (municipalityId < 0) {
			log.info("#deleteFilterName with no municipalityId");
			return badRequest(INVALID_MUNICIPALITY_ID);
		}
		else {
			distributionService.deleteFilterName(municipalityId, filterNameId);

			return ok();
		}
	}

	@GET
	@Path("/text/names/default")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response getDefaultFilterNameText(@QueryParam("municipalityId") long municipalityId) {
		if (municipalityId < 0) {
			log.info("#getFilterNames with no municipalityId");
			return badRequest(INVALID_MUNICIPALITY_ID);
		}
		else {
			return ok(distributionService.getDefaultTextFilterName(municipalityId));
		}
	}

	@POST
	@Path("/text/names/default/{filterNameId}")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	@MunicipalityAdminRequired
	public Response setDefaultFilterNameText(@PathParam("filterNameId") Long filterNameId, @QueryParam("municipalityId") long municipalityId) {
		if (municipalityId < 0) {
			log.info("#setDefaultFilterNameText with no municipalityId");
			return badRequest(INVALID_MUNICIPALITY_ID);
		}
		else {
			distributionService.setDefaultTextFilterName(municipalityId, filterNameId);

			return ok();
		}
	}

	@GET
	@Path("/date/names")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response getFilterNamesDate(@QueryParam("municipalityId") long municipalityId) {
		if (municipalityId < 0) {
			log.info("#getFilterNames with no municipalityId");
			return badRequest(INVALID_MUNICIPALITY_ID);
		}
		else {
			return ok(distributionService.getFilterNamesDate(municipalityId));
		}
	}

	@POST
	@Path("/date/names")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	@MunicipalityAdminRequired
	public Response updateDateFilterName(@QueryParam("municipalityId") long municipalityId, FilterNamePO filterNamePO) {
		if (municipalityId < 0) {
			log.info("#getFilterNames with no municipalityId");
			return badRequest(INVALID_MUNICIPALITY_ID);
		}
		else {
			return ok(distributionService.updateFilterName(municipalityId, filterNamePO));
		}
	}

	@GET
	@Path("/date/names/default")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response getDefaultFilterNameDate(@QueryParam("municipalityId") long municipalityId) {
		if (municipalityId < 0) {
			log.info("#getFilterNames with no municipalityId");
			return badRequest(INVALID_MUNICIPALITY_ID);
		}
		else {
			return ok(distributionService.getDefaultDateFilterName(municipalityId));
		}
	}

	@POST
	@Path("/date/names/default/{filterNameId}")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	@MunicipalityAdminRequired
	public Response setDefaultFilterNameDate(@PathParam("filterNameId") Long filterNameId, @QueryParam("municipalityId") long municipalityId) {
		if (municipalityId < 0) {
			log.info("#setDefaultFilterNameText with no municipalityId");
			return badRequest(INVALID_MUNICIPALITY_ID);
		}
		else {
			distributionService.setDefaultDateFilterName(municipalityId, filterNameId);

			return ok();
		}
	}

	//TODO: code below this point should probably be refactored to service methods.
	private Response doUpdateResponsibleOrganization(DistributionRule existing, DistributionRulePO updated) {
		if(!allowedToUpdate(existing)) {
			log.warn("User {} doesn't have permissions to update {}", "<WeDontHaveUsersYet>", existing);

			return forbidden();
		}
		try {
			updateDistributionRule(existing, updated);
		}
		catch(IllegalArgumentException ex) {
			log.warn("doUpdateResponsibleOrganization - invalid arguments in [{}]", updated);
			// persistenceService.rollbackTransaction();	// if we move logic to DistributionService, perform rollback.
			return Response.serverError().build();
		}

		return ok();
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

		updateIfChanged(existing.getAssignedEmp().orElse(0L), updated.getEmployee(), existing::setAssignedEmp);

		distributionService.createDistributionRule(existing); // if we move logic to DistributionService, 'existing' is managed and this shouldn't be necessary.
	}

	private<T extends Comparable<T>> void updateIfChanged(T oldVal, T newVal, Consumer<T> updater) {
		if(!newVal.equals(oldVal)) {
			updater.accept(newVal);
		}
	}

	private boolean updatingResponsible(String type){
		return RESPONSIBILITY_UPDATE_TYPE.equals(type);
	}

	private boolean hasUpdatePermission(long userId){
		return userService.isAdmin(userId) || userService.isMunicipalityAdmin(userId) || userService.isManager(userId);
	}
}
