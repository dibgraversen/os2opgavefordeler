package dk.os2opgavefordeler.rest;

import dk.os2opgavefordeler.model.presentation.OrgUnitPO;
import dk.os2opgavefordeler.service.OrgUnitService;
import org.slf4j.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;

@Path("/org-unit")
@RequestScoped
public class OrgUnitEndpoint {
	@Inject
	Logger log;

	@Inject
	OrgUnitService orgUnitService;

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response listAll() {
		final List<OrgUnitPO> ou = orgUnitService.getToplevelOrgUnitPO();

		if(!ou.isEmpty()) {
			return Response.ok().entity(ou).build();
		} else {
			return Response.status(404).build();
		}
	}

	@GET
	@Path("/{orgId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response get(@PathParam("orgId") Integer orgId) {
		final Optional<OrgUnitPO> result = orgUnitService.getOrgUnitPO(orgId);

		return result.map(
			ou -> Response.ok().entity(ou)
		).orElseGet(
			() -> Response.status(404)
		).build();
	}
}
