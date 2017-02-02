package dk.os2opgavefordeler.rest;

import dk.os2opgavefordeler.auth.AdminRequired;
import dk.os2opgavefordeler.auth.AuthService;
import dk.os2opgavefordeler.auth.MunicipalityAdminRequired;
import dk.os2opgavefordeler.auth.UserLoggedIn;
import dk.os2opgavefordeler.repository.MunicipalityRepository;
import dk.os2opgavefordeler.repository.OrgUnitRepository;
import dk.os2opgavefordeler.repository.UserRepository;
import dk.os2opgavefordeler.model.Municipality;
import dk.os2opgavefordeler.model.User;
import dk.os2opgavefordeler.model.ValidationException;
import dk.os2opgavefordeler.model.presentation.ApiKeyPO;
import dk.os2opgavefordeler.model.presentation.KlePO;
import dk.os2opgavefordeler.service.MunicipalityService;
import dk.os2opgavefordeler.service.UserService;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * @author hlo@miracle.dk
 */
@UserLoggedIn
@Path("/municipalities")
public class MunicipalityEndpoint extends Endpoint {

	@Inject
	Logger log;

	@Inject
	MunicipalityService municipalityService;

	@Inject
	private OrgUnitRepository orgUnitRepository;

	@Inject
	private MunicipalityRepository municipalityRepository;

	@Inject
	private EntityManager entityManager;

	@Inject
	private AuthService authService;

	@Inject
	private UserRepository userRepository;

	@Inject
	private UserService userService;

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getMunicipalities() {
		List<Municipality> result = municipalityService.getMunicipalities();
		return ok(result);
	}

	@DELETE
	@Path("/{municipalityId}")
	@Produces("application/json")
	@AdminRequired
	public Response deleteMunicipality(@PathParam("municipalityId") long municipalityId) {
		log.info("Deleting structure for municipality with ID: {}", municipalityId);

		// start transaction
		EntityTransaction transaction = entityManager.getTransaction();
		transaction.begin();

		// TODO delegate this to service - if even appropriate.

		// prepare query statements
		StringBuilder sb = new StringBuilder();

		sb.append("DELETE FROM distributionrule_distributionrulefilter WHERE distributionrule_id IN (SELECT id FROM distributionrule WHERE municipality_id = ");
		sb.append(municipalityId);
		sb.append(");");

		sb.append("DELETE FROM distributionrulefilter drf WHERE drf.distributionrule_id IN (SELECT id FROM distributionrule WHERE municipality_id = ");
		sb.append(municipalityId);
		sb.append(");");

		sb.append("DELETE FROM distributionrule WHERE municipality_id = ");
		sb.append(municipalityId);
		sb.append(";");

		sb.append("DELETE FROM role r WHERE r.employment_id IN (SELECT id FROM employment WHERE municipality_id = ");
		sb.append(municipalityId);
		sb.append(");");

		sb.append("UPDATE employment SET employedin_id = NULL WHERE municipality_id = ");
		sb.append(municipalityId);
		sb.append(";");

		sb.append("UPDATE orgunit SET manager_id = NULL WHERE municipality_id = ");
		sb.append(municipalityId);
		sb.append(";");

		sb.append("DELETE FROM employment WHERE municipality_id = ");
		sb.append(municipalityId);
		sb.append(";");

		sb.append("DELETE FROM orgunit WHERE municipality_id = ");
		sb.append(municipalityId);
		sb.append(";");

		sb.append("DELETE FROM tr_user WHERE municipality_id = ");
		sb.append(municipalityId);
		sb.append(";");

		sb.append("DELETE FROM kle WHERE municipality_id = ");
		sb.append(municipalityId);
		sb.append(";");

		sb.append("DELETE FROM municipality WHERE id = ");
		sb.append(municipalityId);
		sb.append(";");

		// create query
		Query query = entityManager.createNativeQuery(sb.toString());

		// execute the delete statements in the query and commit the transaction
		query.executeUpdate();
		transaction.commit();

		return ok();
	}

	@POST
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@AdminRequired
	public Response createMunicipality(Municipality municipality) {
		log.info("Creating municipality: {}", municipality);

		Municipality result = municipalityService.createMunicipality(municipality);

		log.info("Municipality created: {}", municipality);
		return ok(result);
	}

	@POST
	@Path("/{municipalityId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@AdminRequired
	public Response createMunicipality(@PathParam("municipalityId") long municipalityId, Municipality municipality) {
		Municipality result = municipalityService.createOrUpdateMunicipality(municipality);
		return ok(result);
	}

	@GET
	@Path("/{municipalityId}/kle")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getMunicipalityKle(@PathParam("municipalityId") Long municipalityId) {
		if (municipalityId == null) {
			return badRequest("You need to specify municipalityId");
		}
		List<KlePO> result = municipalityService.getMunicipalityKle(municipalityId);
		return ok(result);
	}

	@POST
	@Path("/{municipalityId}/kle")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@MunicipalityAdminRequired
	public Response saveMunicipalityKle(KlePO kle) {
		if (kle == null) {
			return badRequest("You need to provide a valid KlePO");
		}
		try {
			KlePO result = municipalityService.saveMunicipalityKle(kle);
			return ok(result);
		} catch (ValidationException ve) {
			return badRequest(ve.getMessage());
		}
	}

	@DELETE
	@Path("/{municipalityId}/kle/{id}")
	@MunicipalityAdminRequired
	public Response deleteMunicipalityKle(@PathParam("municipalityId") Long municipalityId, @PathParam("id") Long kleId) {
		if (municipalityId == null || kleId == null) {
			return badRequest("You need to provide valid municipalityId and kleId");
		}

		try {
			municipalityService.deleteMunicipalityKle(municipalityId, kleId);
			return ok(kleId);
		} catch (ValidationException e) {
			return badRequest(e.getMessage());
		}
	}

	@GET
	@Path("/{municipalityId}/apikey")
	@Produces(MediaType.APPLICATION_JSON)
	@NoCache
	@MunicipalityAdminRequired
	public Response getApiKey(@PathParam("municipalityId") long municipalityId) {
		if (permissionsOk(municipalityId)) {
			String apiKey = municipalityService.getApiKey(municipalityId);
			return ok(new ApiKeyPO(apiKey));
		} else {
			return badRequest("could not find municipality or validate permissions.");
		}
	}

	@POST
	@Path("/{municipalityId}/apikey/{apiKey}")
	@Produces(MediaType.APPLICATION_JSON)
	@MunicipalityAdminRequired
	public Response setApiKey(@PathParam("municipalityId") long municipalityId, @PathParam("apiKey") String apiKey) {
		if (permissionsOk(municipalityId)) {
			municipalityService.setApiKey(municipalityId, apiKey);
			return ok(new ApiKeyPO(apiKey));
		} else {
			return badRequest("could not find municipality or validate permissions.");
		}
	}

	private boolean permissionsOk(long municipalityId) {
		// determine municipality from user
		User user = userRepository.findByEmail(authService.getAuthentication().getEmail());

		// make sure the user is authorized to see/update the API key
		if (userService.isMunicipalityAdmin(user.getId())) {
			Municipality currentMunicipality = user.getMunicipality();

			if (currentMunicipality.getId() == municipalityId) {
				return true;
			}
		}

		return false;
	}
}