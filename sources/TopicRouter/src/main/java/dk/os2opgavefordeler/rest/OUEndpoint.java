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
import dk.os2opgavefordeler.service.KleService;
import dk.os2opgavefordeler.service.OrgUnitService;
import dk.os2opgavefordeler.service.OrgUnitWithKLEService;

@Path("/ou")
@RequestScoped
public class OUEndpoint extends Endpoint {

	@Inject
	private OrgUnitWithKLEService orgUnitService;

	@Inject
	private OrgUnitService ouService;

	@Inject
	private KleService kleService;

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
		if (result.isPresent()) {
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
	@Produces(MediaType.TEXT_PLAIN)
	public Response assignKLE(@PathParam("ouId") long ouId, @PathParam("assignmentType") String assignmentTypeString,
			@PathParam("kleNumber") String kleNumber) {
		// Check if ou exists
		Optional<OrgUnit> ou = ouService.getOrgUnit(ouId);
		if (!ou.isPresent()) {
			return Response.status(404).entity("OrgUnit not found for ID: " + ouId).build();
		}
		// Check if assignment type is correct
		KleAssignmentType assignmentType;
		try {
			assignmentType = KleAssignmentType.fromString(assignmentTypeString);
		} catch (Exception e) {
			return Response.status(404).entity("No assignment type with a name: \"" + assignmentTypeString + "\" found")
					.build();
		}
		// Check if kle exists
		try {
			kleService.getKle(kleNumber);
		} catch (Exception e) {
			return Response.status(404).entity("No KLE for number: \"" + kleNumber + "\" found").build();
		}
		orgUnitService.addKLE(ouId, kleNumber, assignmentType);
		return Response.ok().build();
	}

	@DELETE
	@Path("/{ouId}/{assignmentType}/{kleNumber}")
	@Produces(MediaType.TEXT_PLAIN)
	public Response deleteKLE(@PathParam("ouId") long ouId,
			@PathParam("assignmentType") String assignmentTypeString, @PathParam("kleNumber") String kleNumber) {
		// Check if ou exists
		Optional<OrgUnit> ou = ouService.getOrgUnit(ouId);
		if (!ou.isPresent()) {
			return Response.status(404).entity("OrgUnit not found for ID: " + ouId).build();
		}
		// Check if assignment type is correct
		KleAssignmentType assignmentType;
		try {
			assignmentType = KleAssignmentType.fromString(assignmentTypeString);
		} catch (Exception e) {
			return Response.status(404).entity("No assignment type with a name: \"" + assignmentTypeString + "\" found")
					.build();
		}
		// Check if kle exists
		try {
			kleService.getKle(kleNumber);
		} catch (Exception e) {
			return Response.status(404).entity("No KLE for number: \"" + kleNumber + "\" found").build();
		}
		orgUnitService.removeKLE(ouId, kleNumber, assignmentType);
		return Response.ok().build();
	}
}
