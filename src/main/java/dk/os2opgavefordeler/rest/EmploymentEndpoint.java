package dk.os2opgavefordeler.rest;

import dk.os2opgavefordeler.model.presentation.EmploymentPO;
import dk.os2opgavefordeler.service.EmploymentService;
import org.slf4j.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;

@Path("/employments")
@RequestScoped
public class EmploymentEndpoint {
	@Inject
	Logger log;

	@Inject
	EmploymentService employmentService;

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response listAll(@QueryParam("municipalityId") Long municipalityId) {
		final List<EmploymentPO> employees =  employmentService.getAllPO(municipalityId);

		if(!employees.isEmpty()) {
			return Response.ok().entity(employees).build();
		} else {
			return Response.status(404).build();
		}
	}

	@GET
	@Path("/{empId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response get(@PathParam("empId") Long empId) {
		final Optional<EmploymentPO> result = employmentService.getEmploymentPO(empId);

		return result.map(
			epo -> Response.ok().entity(epo)
		).orElseGet(
			() -> Response.status(404)
		).build();
	}
}
