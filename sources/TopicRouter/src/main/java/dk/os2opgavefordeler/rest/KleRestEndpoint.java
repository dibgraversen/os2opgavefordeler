package dk.os2opgavefordeler.rest;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import dk.os2opgavefordeler.model.Kle;
import dk.os2opgavefordeler.service.KleService;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;

import dk.os2opgavefordeler.service.KleImportService;
import org.slf4j.Logger;

@Path("/kle")
@RequestScoped
public class KleRestEndpoint extends Endpoint {

	@Inject
	private Logger log;

	@Inject
	private KleImportService importer;

	@Inject
	private KleService kleService;

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public Response list() {
		List<Kle> groups = kleService.fetchAllKleMainGroups();

		StringBuilder out = new StringBuilder();

		out.append(String.format("List of %d groups: ", groups.size()));
		int totalGroups = 0, totalTopics = 0;
		for (Kle group : groups) {
			out.append(String.format("Group %s/%s {\n", group.getNumber(), group.getTitle()));
			totalGroups += group.getChildren().size();
			for (Kle sub : group.getChildren()) {
				out.append(String.format("\tSubgroup %s/%s {\n", sub.getNumber(), sub.getTitle()));

				totalTopics += sub.getChildren().size();
				for (Kle topic : sub.getChildren()) {
					out.append(String.format("\t\tTopic %s/%s\n", topic.getNumber(), topic.getTitle()));
				}
				out.append("}\n");
			}
			out.append("}\n");
		}
		out.append(String.format("Total groups: %d, subgroups: %s, topics %d", groups.size(), totalGroups, totalTopics));

		return ok(out.toString());
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/groups/{number}")
	public Response getGroup(@PathParam("number") String number, @QueryParam("municipalityId") Long municipalityId) {
		Optional<Kle> group = kleService.fetchMainGroup(number, municipalityId);
		return group.isPresent() ?
			ok(group.get()) :
			Response.status(Response.Status.NOT_FOUND).build();
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
			return badRequest("Missing MXL");
		}

		final List<Kle> groups;
		try {
			groups = (xsd == null) ?
					importer.importFromXml(xml) :
					importer.importFromXml(xml, xsd);

			kleService.storeAllKleMainGroups(groups);
		}
		catch (Exception ex) {
			log.error("Error importing KLE XML", ex);

			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
		reportsStats(groups);

		final String response = String.format("KLE XML imported - group 0 is [%s/%s]",
				groups.get(0).getNumber(),
				groups.get(0).getTitle());
		return ok(response);
	}

	static private class Stats {
		public int max = 0, num = 0, numNonZero = 0;
		long tlen = 0;
	}

	private void visit(List<Kle> kle, Consumer<Kle> visitor) {
		for(Kle k : kle) {
			visitor.accept(k);
			visit(k.getChildren(), visitor);
		}
	}

	private void reportsStats(List<Kle> groups) {
		final Stats stats = new Stats();

		log.info("reportStats: counting");
		visit(groups, kle -> {
			stats.num++;

			final String desc = kle.getDescription();
			if(!desc.isEmpty()) {
				int len = desc.length();
				stats.max = Math.max(stats.max, len);
				stats.numNonZero++;
				stats.tlen += len;
			}
		});

		log.info(String.format("%d topics, %d nonzero, %d maxlen, %.3f avglen",
				stats.num, stats.numNonZero, stats.max, (double) stats.tlen / stats.numNonZero));
	}
}
