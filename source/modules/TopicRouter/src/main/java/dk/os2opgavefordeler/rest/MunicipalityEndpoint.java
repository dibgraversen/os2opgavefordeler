package dk.os2opgavefordeler.rest;

import dk.os2opgavefordeler.model.Municipality;
import dk.os2opgavefordeler.model.ValidationException;
import dk.os2opgavefordeler.model.presentation.KlePO;
import dk.os2opgavefordeler.service.MunicipalityService;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * @author hlo@miracle.dk
 */
@Path("/municipalities")
public class MunicipalityEndpoint extends Endpoint {
	public static final String TEXT_PLAIN = "text/plain";

	@Inject
	Logger log;

	@Inject
	MunicipalityService municipalityService;

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getMunicipalities(){
		List<Municipality> result = municipalityService.getMunicipalities();
		return ok(result);
	}

	@POST
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createMunicipality(Municipality municipality){
		Municipality result =  municipalityService.createMunicipality(municipality);
		return ok(result);
	}

	@POST
	@Path("/{municipalityId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createMunicipality(@PathParam("municipalityId") long municipalityId, Municipality municipality){
		Municipality result =  municipalityService.createOrUpdateMunicipality(municipality);
		return ok(result);
	}

	@GET
	@Path("/{municipalityId}/kle")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getMunicipalityKle(@PathParam("municipalityId") Long municipalityId){
		if(municipalityId == null){
			return badRequest("You need to specify municipalityId");
		}
		List<KlePO> result = municipalityService.getMunicipalityKle(municipalityId);
		return ok(result);
	}

	@POST
	@Path("/{municipalityId}/kle")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response saveMunicipalityKle(KlePO kle){
		if(kle == null){
			return badRequest("You need to provide a valid KlePO");
		}
		try {
			KlePO result = municipalityService.saveMunicipalityKle(kle);
			return ok(result);
		} catch	(ValidationException ve){
			return badRequest(ve.getMessage());
		}
	}

	@DELETE
	@Path("/{municipalityId}/kle/{id}")
	public Response deleteMunicipalityId(@PathParam("municipalityId") Long municipalityId, @PathParam("id") Long kleId){
		if(municipalityId == null || kleId == null){
			return badRequest("You need to provide valid municipalityId and kleId");
		}
		try {
			municipalityService.deleteMunicipalityKle(municipalityId, kleId);
			return ok(kleId);
		} catch (ValidationException e) {
			return badRequest(e.getMessage());
		}
	}
}
