package dk.os2opgavefordeler.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import dk.os2opgavefordeler.model.Employment;
import dk.os2opgavefordeler.model.OrgUnit;
import dk.os2opgavefordeler.model.presentation.OrgUnitPO;
import dk.os2opgavefordeler.service.OrgUnitService;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.slf4j.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Path("/org-units")
@RequestScoped
public class OrgUnitEndpoint {
	public static final String FILE = "file";
	@Inject
	Logger log;

	@Inject
	OrgUnitService orgUnitService;

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response listAll( @QueryParam("municipalityId") Long municipalityId ) {
		final List<OrgUnitPO> ou = orgUnitService.getToplevelOrgUnitPO(municipalityId);

		if(!ou.isEmpty()) {
			return Response.ok().entity(ou).build();
		} else {
			return Response.status(404).build();
		}
	}

	@GET
	@Path("/display")
	@Produces(MediaType.TEXT_PLAIN)
	public Response listAllDisplay(@QueryParam("municipalityId") Long municipalityId) {
		final Optional<OrgUnit> result = orgUnitService.getToplevelOrgUnit(municipalityId);

		return result.map(
			ou -> Response.ok().entity( printOrg(new StringBuilder(), 0, ou) )
		).orElseGet(
			() -> Response.status(404)
		).build();
	}

	@GET
	@Path("/{orgId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response get(@PathParam("orgId") Long orgId) {
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
		fixupOrgUnit(input);

		orgUnitService.importOrganization(input);

		return Response.ok().build();
	}

	@POST
	@Path("/fileImport")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response fileImport(MultipartFormDataInput multipartInput) {
		Map<String, List<InputPart>> uploadForm = multipartInput.getFormDataMap();
		List<InputPart> inputParts = uploadForm.get(FILE);
		StringBuilder completeString = new StringBuilder();
		for (InputPart inputPart : inputParts) {
			try {
				completeString.append(inputPart.getBodyAsString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			ObjectMapper mapper = new ObjectMapper();
			OrgUnit input = mapper.readValue(completeString.toString(), OrgUnit.class);
			fixupOrgUnit(input);
			orgUnitService.importOrganization(input);
		} catch (IOException e) {
			return Response.status(Response.Status.BAD_REQUEST).build();
		}
		return Response.ok().build();
	}

	private void fixupOrgUnit(OrgUnit input) {
		deduplicateManager(input);

		input.getEmployees().stream().forEach(this::fixupEmployee);

		for (OrgUnit orgUnit : input.getChildren()) {
			fixupOrgUnit(orgUnit);
		}
	}

	private void deduplicateManager(OrgUnit input) {
		input.getManager().ifPresent(manager -> {
			if (input.getEmployees().contains(manager)) {
				// replace copy with reference
				input.removeEmployee(manager);
				input.addEmployee(manager);
			}
		});
	}

	private void fixupEmployee(Employment employee) {
		if(Strings.isNullOrEmpty(employee.getInitials())) {
			employee.setInitials("jdoe");
		}
		if(Strings.isNullOrEmpty(employee.getEmail())) {
			employee.setEmail(employee.getInitials() + "@syddjurs.dk");
		}
		if(Strings.isNullOrEmpty(employee.getPhone())) {
			employee.setPhone("12345678");
		}
	}

	private StringBuilder printOrg(StringBuilder sb, int indent, OrgUnit org) {
		final String tabs = Strings.repeat("\t", indent);

		sb.append(tabs).append(String.format("%s - manager: %s\n", org, org.getManager()));

		sb.append(tabs).append("\tEmployees:\n");
		org.getEmployees().forEach( e -> sb.append(tabs).append("\t\t").append(e).append("\n") );

		sb.append(tabs).append("\tChildren:\n");
		org.getChildren().forEach(t -> printOrg(sb, indent + 2, t));

		return sb;
	}
}
