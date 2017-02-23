package dk.os2opgavefordeler.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import dk.os2opgavefordeler.model.OrgUnit;
import dk.os2opgavefordeler.orgunit.OrgUnitDTO;
import dk.os2opgavefordeler.repository.OrgUnitRepository;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.slf4j.Logger;

import javax.enterprise.context.RequestScoped;

import javax.inject.Inject;

import javax.servlet.http.HttpServletRequest;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import dk.os2opgavefordeler.assigneesearch.Assignee;
import dk.os2opgavefordeler.assigneesearch.FindAssignedForKleService;
import dk.os2opgavefordeler.auth.AuthService;
import dk.os2opgavefordeler.model.Kle;
import dk.os2opgavefordeler.model.Municipality;
import dk.os2opgavefordeler.model.api.DistributionRuleApiResultPO;
import dk.os2opgavefordeler.model.api.EmploymentApiResultPO;
import dk.os2opgavefordeler.service.*;

/**
 * This class supports the endpoints that are part of the external/programmatic API.
 *
 * @author hlo@miracle.dk
 */
@Path("/api")
@RequestScoped
public class ApiEndpoint extends Endpoint {

	private static final String DID_NOT_FIND_A_MUNICIPALITY_BASED_ON_GIVEN_AUTHORIZATION = "Did not find a municipality based on given authorization.";
	private static final String YOUR_SUBSCRIPTION_IS_NOT_ACTIVE_AND_THEREFOR_THE_API_CANNOT_BE_USED = "Your subscription is not active and therefor the api cannot be used.";
	private static final String NO_ORG_UNIT_FOUND_FOR_PNUMBER = "No org unit found for pnumber";
	private static final String NO_ONE_SEEMS_TO_BE_HANDLING_THE_GIVEN_KLE_FOR_MUNICIPALITY = "No one seems to be handling the given kle for municipality.";
	private static final String DID_NOT_FIND_A_KLE_BASED_ON_GIVEN_NUMBER = "Did not find a Kle based on given number.";
	
	@Inject
	Logger log;

	@Inject
	KleService kleService;

	@Inject
	MunicipalityService municipalityService;

	@Inject
	DistributionService distributionService;

	@Inject
	OrgUnitService orgUnitService;

	@Inject
	OrgUnitRepository orgUnitRepo;

	@Inject
	EmploymentService employmentService;

	@Inject
	private FindAssignedForKleService findAssignedForKleService;

	@Inject
	private AuthService authService;

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response lookup(@QueryParam("kle") String kleNumber, @Context UriInfo uriInfo, @Context HttpServletRequest request) {
		AuthorizeResult authorizeResult = authorize();
		if(authorizeResult.success){
			Municipality municipality = authorizeResult.municipality;
			Optional<Kle> kleMaybe = kleService.fetchMainGroup(kleNumber, municipality.getId());
			if (!kleMaybe.isPresent()) {
				return badRequest(DID_NOT_FIND_A_KLE_BASED_ON_GIVEN_NUMBER);
			}
			Kle kle = kleMaybe.get();

			Map<String, String> parameters = new HashMap<>();
			for (Map.Entry<String, List<String>> m : uriInfo.getQueryParameters().entrySet()) {
				parameters.put(m.getKey(), m.getValue().get(0));
			}

			Assignee assignee = findAssignedForKleService.findAssignedForKle(kle, municipality, parameters);
			if (assignee == null) {
				return notFound(NO_ONE_SEEMS_TO_BE_HANDLING_THE_GIVEN_KLE_FOR_MUNICIPALITY);
			}

			EmploymentApiResultPO manager = new EmploymentApiResultPO(orgUnitService.findResponsibleManager(assignee.getOrgUnit()).orElse(null));
			EmploymentApiResultPO employee = assignee.getEmployment().map(EmploymentApiResultPO::new).orElse(null);
			Optional<OrgUnit> distributionOrgUnit = assignee.getRule().getAssignedOrg();
			OrgUnit assignedOrg;
			if (distributionOrgUnit.isPresent()) {
				if (distributionOrgUnit.get().equals(assignee.getOrgUnit())) {
					assignedOrg = distributionOrgUnit.get();
				} else {
					assignedOrg = assignee.getOrgUnit();
				}
			} else {
				assignedOrg = assignee.getOrgUnit();
			}
			DistributionRuleApiResultPO resultPO = new DistributionRuleApiResultPO(assignee.getRule().getKle(), assignedOrg, manager, employee);
			log.info("API endpoint called by {} for KLE: {} with result: {}", authService.getAuthentication().getEmail(), resultPO.getKle().getNumber(), resultPO.getOrg().getName());
			return ok(resultPO);
		}	else {
			return Response.status(authorizeResult.status).type(TEXT_PLAIN).entity(authorizeResult.message).build();
		}
	}


	@GET
	@Path("/orgunit/pNumber/{pNumber}")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response getByPNumber(@PathParam("pNumber") String pNumber){
		AuthorizeResult authorizeResult = authorize();
		if( authorizeResult.success ){
			Municipality municipality = authorizeResult.municipality;
			OrgUnit orgUnit = orgUnitRepo.findByPNumberAndMunicipalityId(pNumber, municipality.getId());
			if(orgUnit != null){
				return ok(new OrgUnitDTO(orgUnit));
			} else {
				return notFound(NO_ORG_UNIT_FOUND_FOR_PNUMBER);
			}
		} else {
			return Response.status(authorizeResult.status).type(TEXT_PLAIN).entity(authorizeResult.message).build();
		}
	}

	@GET
	@Path("/healthcheck")
	@Produces(MediaType.TEXT_PLAIN + "; charset=UTF-8")
	@NoCache
	public Response healthCheck() {
		//TODO: perform (light-weight) sanity checks.
		boolean everythingIsOk = true;

		if (everythingIsOk) {
			return ok("We get signal.");
		} else {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Somebody set up us the bomb.").build();
		}
	}

	/**
	 * Authorizes the incoming call to the endpoint
	 * @return AuthorizeResult containing the municipality if success, or HTTP error status and message if not success.
	 */
	private AuthorizeResult authorize(){
		String token = authService.getAuthentication().getToken();

		if (token == null || token.isEmpty()) {
			return new AuthorizeResult(false, null, Response.Status.UNAUTHORIZED, "");
		}

		Optional<Municipality> municipalityMaybe = municipalityService.getMunicipalityFromToken(token);

		if (!municipalityMaybe.isPresent()) {
			return new AuthorizeResult(false, null, Response.Status.UNAUTHORIZED, DID_NOT_FIND_A_MUNICIPALITY_BASED_ON_GIVEN_AUTHORIZATION);
		}

		Municipality municipality = municipalityMaybe.get();

		if (!municipality.isActive()) {
			return new AuthorizeResult(false, null, Response.Status.PAYMENT_REQUIRED, YOUR_SUBSCRIPTION_IS_NOT_ACTIVE_AND_THEREFOR_THE_API_CANNOT_BE_USED);
		}

		return new AuthorizeResult(true, municipality, null, "");
	}

	private class AuthorizeResult{
		private boolean success;
		private Municipality municipality;
		private Response.Status status;
		private String message;

		private AuthorizeResult(boolean success, Municipality municipality, Response.Status status, String message) {
			this.success = success;
			this.municipality = municipality;
			this.status = status;
			this.message = message;
		}
	}
}
