package dk.os2opgavefordeler.rest;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.List;

import dk.os2opgavefordeler.model.kle.KleGroup;
import dk.os2opgavefordeler.model.kle.KleTopic;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;

import dk.os2opgavefordeler.model.kle.KleMainGroup;
import dk.os2opgavefordeler.service.KleImportService;
import dk.os2opgavefordeler.service.PersistenceService;
import org.slf4j.Logger;

@Path("/kle")
@RequestScoped
public class KleRestEndpoint {
	@Inject
	private Logger log;

	@Inject
	private KleImportService importer;

	@Inject
	private PersistenceService persistence;

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public Response list() {
		List<KleMainGroup> groups = persistence.fetchAllKleMainGroups();

		StringBuilder out = new StringBuilder();

		out.append(String.format("List of %d groups: ", groups.size()));
		int totalGroups = 0, totalTopics = 0;
		for (KleMainGroup group : groups) {
			out.append(String.format("Group %s/%s {\n", group.getNumber(), group.getTitle()));
			totalGroups += group.getGroups().size();
			for (KleGroup sub : group.getGroups()) {
				out.append(String.format("\tSubgroup %s/%s {\n", sub.getNumber(), sub.getTitle()));

				totalTopics += sub.getTopics().size();
				for (KleTopic topic : sub.getTopics()) {
					out.append(String.format("\t\tTopic %s/%s\n", topic.getNumber(), topic.getTitle()));
				}
				out.append("}\n");
			}
			out.append("}\n");
		}
		out.append(String.format("Total groups: %d, subgroups: %s, topics %d", groups.size(), totalGroups, totalTopics));

		return Response.status(Response.Status.OK).entity(out.toString()).build();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/groups/{number}")
	public Response getGroup(@PathParam("number") String number)
	{
		KleMainGroup group = persistence.fetchMainGroup(number);
		if(group != null) {
			log.info("returning group");
			return Response.status(Response.Status.OK).entity(group).build();
		} else {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
	}



	@POST @NoCache
	@Path("/import")
	@Produces("text/plain")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response importXml(@MultipartForm KleXmlUploadData request) {
		// XML is mandatory, XSD is optional.
		final InputStream xml = request.getXml();
		final InputStream xsd = request.getXsd();

		if(xml == null) {
			return Response.status(Response.Status.BAD_REQUEST).entity("Missing MXL").build();
		}

		final List<KleMainGroup> groups;
		try {
			groups = (xsd == null) ?
					importer.importFromXml(xml) :
					importer.importFromXml(xml, xsd);

			persistence.storeAllKleMainGroups(groups);
		}
		catch (Exception ex) {
			log.error("Error importing KLE XML", ex);

			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}

		final String response = String.format("KLE XML imported - group 0 is [%s/%s]",
				groups.get(0).getNumber(),
				groups.get(0).getTitle());
		return Response.status(Response.Status.OK).entity(response).build();
	}
}
