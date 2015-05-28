package dk.os2opgavefordeler.rest;

import dk.os2opgavefordeler.model.OrgUnit;
import dk.os2opgavefordeler.service.OrgUnitService;
import org.slf4j.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Optional;

@Path("/org-unit")
@RequestScoped
public class OrgUnitEndpoint {
	@Inject
	Logger log;

	@Inject
	OrgUnitService orgUnitService;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response listAll() {
		final Optional<OrgUnit> ou = orgUnitService.getToplevelOrgUnitPO();

		if(ou.isPresent()) {
			return Response.ok().entity(ou.get()).build();
		} else {
			return Response.status(404).build();
		}
	}
}