package dk.os2opgavefordeler.rest;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import dk.os2opgavefordeler.model.OrgUnit;
import dk.os2opgavefordeler.model.presentation.KleAssignmentType;
import dk.os2opgavefordeler.model.presentation.OrgUnitTreePO;
import dk.os2opgavefordeler.model.presentation.OrgUnitWithKLEPO;
import dk.os2opgavefordeler.service.OrgUnitService;
import dk.os2opgavefordeler.service.OrgUnitWithKLEService;

@Path("/ou")
@RequestScoped
public class OUEndpoint extends Endpoint {

	@Inject
	private OrgUnitWithKLEService orgUnitService;
	
	@Inject
	private OrgUnitService ouService;
	

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/list")
	public Response list() {
		List<OrgUnitWithKLEPO> result = orgUnitService.getAll(1L);
		return Response.ok().entity(result).build();
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response tree() {
		Optional<OrgUnit> result = ouService.getToplevelOrgUnit(1L);		
		if(result.isPresent()){
			OrgUnitTreePO value = new OrgUnitTreePO(result.get());
			return Response.ok().entity(Arrays.asList(value)).build();
		}
		return Response.status(404).entity("No data found.").build();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{Id}")
	// @KleAssignerRequired
	public Response get(@PathParam("Id") long id) {
		OrgUnitWithKLEPO result = orgUnitService.get(id);
		if (result != null) {
			return Response.ok().entity(result).build();
		} else {
			return Response.status(404).entity("Entity not found for ID: " + id).build();
		}
	}

	@POST
	@Path("/{ouId}/{assignmentType}/{kleNumber}")
	public Response assignKLE(@PathParam("ouId") long ouId,
			@PathParam("assignmentType") KleAssignmentType assignmentType, @PathParam("kleNumber") String kleNumber) {
		System.out.println("We hit the endpoint: /" + ouId+"/"+assignmentType+"/"+kleNumber );
		orgUnitService.addKLE(ouId, kleNumber, assignmentType);
		System.out.println("After kle is added");
		return Response.ok().build();
	}

	@DELETE
	@Path("/{ouId}/{assignmentType}/{kleNumber}")
	public Response deleteKLE(@PathParam("ouId") long ouId,
			@PathParam("assignmentType") KleAssignmentType assignmentType,@PathParam("kleNumber") String kleNumber) {
		orgUnitService.removeKLE(ouId, kleNumber, assignmentType);
		return Response.ok().build();
	}
}
