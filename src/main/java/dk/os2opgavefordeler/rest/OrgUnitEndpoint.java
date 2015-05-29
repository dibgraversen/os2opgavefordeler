package dk.os2opgavefordeler.rest;

import com.google.common.base.Strings;
import dk.os2opgavefordeler.model.OrgUnit;
import dk.os2opgavefordeler.model.presentation.OrgUnitPO;
import dk.os2opgavefordeler.service.OrgUnitService;
import org.slf4j.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
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

	@POST
	@Path("/import")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response importOrg(OrgUnit input) {
		fixRelations(input);
		printOrg(input);

		orgUnitService.createOrgUnit(input);

		return Response.ok().build();
	}



	private void fixRelations(OrgUnit input) {
		input.getChildren().forEach(child -> {
			child.setParent(input);
			fixRelations(child);
		});
	}

	int indent = 0;
	private void printOrg(OrgUnit org) {
		System.out.println(String.format("%s%s - %s", Strings.repeat("\t", indent), org, org.getManager()) );
		++indent;
		org.getChildren().
			forEach(this::printOrg);
		--indent;
	}
}
