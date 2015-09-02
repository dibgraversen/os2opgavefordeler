package dk.os2opgavefordeler.rest;

import dk.os2opgavefordeler.model.Municipality;
import dk.os2opgavefordeler.service.MunicipalityService;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
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
public class MunicipalityEndpoint {
	@Inject
	MunicipalityService municipalityService;

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getMunicipalities(){
		List<Municipality> result = municipalityService.getMunicipalities();
		return Response.ok().entity(result).build();
	}

	@POST
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createMunicipality(Municipality municipality){
		Municipality result =  municipalityService.createMunicipality(municipality);
		return Response.ok().entity(result).build();
	}

	@POST
	@Path("/{municipalityId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createMunicipality(@PathParam("municipalityId") long municipalityId, Municipality municipality){
		Municipality result =  municipalityService.createOrUpdateMunicipality(municipality);
		return Response.ok().entity(result).build();
	}
}
