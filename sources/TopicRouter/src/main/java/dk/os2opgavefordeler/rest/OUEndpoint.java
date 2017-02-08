package dk.os2opgavefordeler.rest;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import dk.os2opgavefordeler.auth.KleAssignerRequired;
import dk.os2opgavefordeler.model.presentation.OrgUnitWithKLEPO;
import dk.os2opgavefordeler.service.OrgUnitWithKLEService;

@Path("/ou")
@RequestScoped
public class OUEndpoint extends Endpoint {

	@Inject
	private OrgUnitWithKLEService orgUnitService;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response list() {
		List<OrgUnitWithKLEPO> result = orgUnitService.getAll(1L);		
		return Response.ok().entity(result).build();		
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{Id}")
//	@KleAssignerRequired
	public Response get(@PathParam("Id") long id) {
		OrgUnitWithKLEPO result = orgUnitService.get(id);
		if (result != null) {
			return Response.ok().entity(result).build();
		} else {
			return Response.status(404).entity("Entity not found for ID: " + id).build();
		}
	}
	
}
