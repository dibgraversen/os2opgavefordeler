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

import org.slf4j.Logger;

import dk.os2opgavefordeler.auth.AuthService;
import dk.os2opgavefordeler.auth.KleAssignerRequired;
import dk.os2opgavefordeler.auth.UserLoggedIn;
import dk.os2opgavefordeler.model.Municipality;
import dk.os2opgavefordeler.model.OrgUnit;
import dk.os2opgavefordeler.model.presentation.KleAssignmentType;
import dk.os2opgavefordeler.model.presentation.OrgUnitListPO;
import dk.os2opgavefordeler.model.presentation.OrgUnitTreePO;
import dk.os2opgavefordeler.model.presentation.OrgUnitWithKLEPO;
import dk.os2opgavefordeler.repository.UserRepository;
import dk.os2opgavefordeler.service.KleService;
import dk.os2opgavefordeler.service.OrgUnitService;
import dk.os2opgavefordeler.service.OrgUnitWithKLEService;


@Path("/ou")
@RequestScoped
@UserLoggedIn
public class OUEndpoint extends Endpoint {
	
	@Inject
	private Logger log;

	@Inject
	private OrgUnitWithKLEService orgUnitWithKLEService;

	@Inject
	private OrgUnitService orgUnitService;

	@Inject
	private KleService kleService;

	@Inject
	private AuthService authService;

	@Inject
	private UserRepository userRepository;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/list")
	public Response list() {
		List<OrgUnitListPO> result = orgUnitWithKLEService.getList(getMunicipality().getId());
		
		return Response.ok().entity(result).build();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response tree() {
		Optional<OrgUnit> result = orgUnitService.getToplevelOrgUnit(getMunicipality().getId());

		if (result.isPresent()) {
			OrgUnitTreePO value = new OrgUnitTreePO(result.get());

			return Response.ok().entity(Arrays.asList(value)).build();
		}

		return Response.status(404).entity("No data found.").build();
	}

	private Municipality getMunicipality() {
		return authService.currentUser().getMunicipality();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{Id}")
	public Response get(@PathParam("Id") long id) {
		OrgUnitWithKLEPO result = orgUnitWithKLEService.get(id,getMunicipality());

		if (result != null) {
			return Response.ok().entity(result).build();
		} else {
			return Response.status(404).entity("Entity not found for ID: " + id).build();
		}
	}

	@POST
	@Path("/{ouId}/{assignmentType}/{kleNumber}")
	@Produces(MediaType.TEXT_PLAIN)
	@KleAssignerRequired
	public Response assignKLE(@PathParam("ouId") long ouId, @PathParam("assignmentType") String assignmentTypeString, @PathParam("kleNumber") String kleNumber) {

		// Check if ou exists
		Optional<OrgUnit> ou = orgUnitService.getOrgUnit(ouId,getMunicipality());
		if (!ou.isPresent()) {
			return Response.status(400).entity("OrgUnit not found for ID: " + ouId).build();
		}

		// Check if assignment type is correct
		KleAssignmentType assignmentType;
		try {
			assignmentType = KleAssignmentType.fromString(assignmentTypeString);
		} catch (Exception e) {
			return Response.status(400).entity("No assignment type with a name: \"" + assignmentTypeString + "\" found")
					.build();
		}

		// Check if kle exists
		try {
			kleService.getKle(kleNumber);
		} catch (Exception e) {
			log.error("Failed to lookup a KLE from database.",e);
			return Response.status(400).entity("No KLE for number: \"" + kleNumber + "\" found").build();
		}

		boolean result = orgUnitWithKLEService.addKLE(ouId, kleNumber, assignmentType);
		if (result == false) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}

		return Response.ok().build();
	}

	@DELETE
	@Path("/{ouId}/{assignmentType}/{kleNumber}")
	@Produces(MediaType.TEXT_PLAIN)
	@KleAssignerRequired
	public Response unassignKLE(@PathParam("ouId") long ouId, @PathParam("assignmentType") String assignmentTypeString, @PathParam("kleNumber") String kleNumber) {

		// Check if ou exists
		Optional<OrgUnit> ou = orgUnitService.getOrgUnit(ouId,getMunicipality());
		if (!ou.isPresent()) {
			return Response.status(404).entity("OrgUnit not found for ID: " + ouId).build();
		}

		// Check if assignment type is correct
		KleAssignmentType assignmentType;
		try {
			assignmentType = KleAssignmentType.fromString(assignmentTypeString);
		} catch (Exception e) {
			return Response.status(400).entity("No assignment type with a name: \"" + assignmentTypeString + "\" found")
					.build();
		}

		//Check if OU contains that kleNumber
		if(!orgUnitWithKLEService.containsKLE(ou.get(),assignmentType,kleNumber)){
			log.info("Not removing KLE " + kleNumber + " from " + ou.get().getName() + " because it was not assigned previously");
	                return Response.ok().build();
		}

		boolean result = orgUnitWithKLEService.removeKLE(ouId, kleNumber, assignmentType);
		if (result == false) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}

		return Response.ok().build();
	}
}
