package dk.os2opgavefordeler.rest;

import dk.os2opgavefordeler.auth.AuthService;
import dk.os2opgavefordeler.auth.UserLoggedIn;
import dk.os2opgavefordeler.model.presentation.EmploymentPO;
import dk.os2opgavefordeler.model.presentation.SimpleMessage;
import dk.os2opgavefordeler.service.BadRequestArgumentException;
import dk.os2opgavefordeler.service.EmploymentService;
import dk.os2opgavefordeler.util.Validate;
import org.slf4j.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;

@UserLoggedIn
@Path("/employments")
@RequestScoped
public class EmploymentEndpoint extends Endpoint {
	@Inject
	Logger log;

	@Inject
	EmploymentService employmentService;

	@Inject
	private AuthService authService;

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response listAll(@QueryParam("municipalityId") Long municipalityId,
													@QueryParam("employmentId") Long employmentId,
													@QueryParam("managedOnly") boolean managedOnly) {
		try {
			Validate.nonZero(municipalityId, "Invalid municipalityId");
			Validate.nonZero(employmentId, "Invalid employmentId");
			List<EmploymentPO> employees;
			if(managedOnly){
				employees = employmentService.getManagedAsPO(municipalityId, employmentId);
			} else {
				employees = employmentService.getAllPO(municipalityId, employmentId);
			}
			if(!employees.isEmpty()) {
				return ok(employees);
			} else {
				return notFound();
			}
		}
		catch(BadRequestArgumentException e) {
			return badRequest(e.getMessage());
		}
	}

	@GET
	@Path("/{empId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response get(@PathParam("empId") Long empId) {
		try {
			Validate.nonZero(empId, "Invalid employmentId");
			final Optional<EmploymentPO> result = employmentService.getEmploymentPO(empId);

			return result.map(
				epo -> Response.ok().entity(epo)
			).orElseGet(
				() -> Response.status(404)
			).build();
		} catch (BadRequestArgumentException e) {
			return Response.status(Response.Status.BAD_REQUEST).entity(new SimpleMessage(e.getMessage())).build();
		}
	}

	@GET
	@Path("/{empId}/subordinates")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSubordinates(@PathParam("empId") Long employmentId){
		if(authService.hasEmployment(employmentId))
		try {
			Validate.nonZero(employmentId, "Invalid employmentId");
		} catch (BadRequestArgumentException e) {
			return  Response.status(Response.Status.BAD_REQUEST).entity(new SimpleMessage(e.getMessage())).build();
		}
		final List<EmploymentPO> result = employmentService.getSubordinates(employmentId);
		return ok(result);
	}
}
