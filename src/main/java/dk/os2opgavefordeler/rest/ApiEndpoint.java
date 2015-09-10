package dk.os2opgavefordeler.rest;

import dk.os2opgavefordeler.model.DistributionRule;
import dk.os2opgavefordeler.model.Employment;
import dk.os2opgavefordeler.model.Kle;
import dk.os2opgavefordeler.model.Municipality;
import dk.os2opgavefordeler.model.api.DistributionRuleApiResultPO;
import dk.os2opgavefordeler.model.api.EmploymentApiResultPO;
import dk.os2opgavefordeler.service.DistributionService;
import dk.os2opgavefordeler.service.EmploymentService;
import dk.os2opgavefordeler.service.KleService;
import dk.os2opgavefordeler.service.MunicipalityService;
import dk.os2opgavefordeler.service.OrgUnitService;
import org.slf4j.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Optional;

/**
 * This class supports the endpoints that are part of the external/programmatic api.
 * @author hlo@miracle.dk
 */
@Path("/api")
@RequestScoped
public class ApiEndpoint {
	private static final int PAYMENT_REQUIRED = 402;
	public static final String TEXT_PLAIN = "text/plain";

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
	EmploymentService employmentService;

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response lookup(@QueryParam("kle") String kleNumber, @QueryParam("municipality") long municipalityId){
		// TODO add authentication
		Optional<Kle> kleMaybe = kleService.fetchMainGroup(kleNumber);
		if(!kleMaybe.isPresent()){
			return Response.status(Response.Status.BAD_REQUEST).type(TEXT_PLAIN)
					.entity("Did not find a Kle based on given number.").build();
		}
		Kle kle = kleMaybe.get();
		Municipality municipality = null;
		if(municipalityId > 0l){
			municipality = municipalityService.getMunicipality(municipalityId);
		}
		if(municipality == null){
			return Response.status(Response.Status.BAD_REQUEST).type(TEXT_PLAIN)
					.entity("Did not find a municipality based on given municipality id.").build();
		}
		if(!municipality.isActive()){
			return Response.status(PAYMENT_REQUIRED).type(TEXT_PLAIN)
					.entity("Your subscription is not active and therefor the api cannot be used.").build();
		}

		// find handling rule from distService.
		DistributionRule result = distributionService.findAssigned(kle, municipality);
		if(result != null){
			Optional<Employment> employeeMaybe = distributionService.findResponsibleEmployee(result);
			EmploymentApiResultPO manager = new EmploymentApiResultPO(orgUnitService.findResponsibleManager(result.getAssignedOrg().get()).orElse(null));
			EmploymentApiResultPO employee = employeeMaybe.map(EmploymentApiResultPO::new).orElse(null);
			DistributionRuleApiResultPO resultPO = new DistributionRuleApiResultPO(result, manager, employee);
			return Response.ok().entity(resultPO).build();
		} else {
			return Response.status(Response.Status.NOT_FOUND).type(TEXT_PLAIN).entity("Noone seems to be handling the given kle for municipality.").build();
		}
	}
}
