package dk.os2opgavefordeler.rest;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.List;

import dk.os2opgavefordeler.model.Kle;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;

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
		List<Kle> groups = persistence.fetchAllKleMainGroups();

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

		return Response.status(Response.Status.OK).entity(out.toString()).build();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/groups/{number}")
	public Response getGroup(@PathParam("number") String number)
	{
		Kle group = persistence.fetchMainGroup(number);
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

		final List<Kle> groups;
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
		reportsStats(groups);

		final String response = String.format("KLE XML imported - group 0 is [%s/%s]",
				groups.get(0).getNumber(),
				groups.get(0).getTitle());
		return Response.status(Response.Status.OK).entity(response).build();
	}


	private void reportsStats(List<Kle> groups) {
		int max = 0, num = 0, numNonZero = 0;
		long tlen = 0;
		String desc;

		log.info("reportStats: counting");
		for (Kle main : groups) {
			num++;

			desc = main.getDescription();
			if(!desc.isEmpty()) {
				int len = desc.length();
				max = Math.max(max, len);
				numNonZero++;
				tlen += len;
			}

			for (Kle group : main.getChildren()) {
				num++;
				desc = group.getDescription();
				if(!desc.isEmpty()) {
					int len = desc.length();
					max = Math.max(max, len);
					numNonZero++;
					tlen += len;
				}

				for (Kle topic : group.getChildren()) {
					num++;
					desc = topic.getDescription();
					if(!desc.isEmpty()) {
						int len = desc.length();
						max = Math.max(max, len);
						numNonZero++;
						tlen += len;
					}
				}
			}
		}

		/*
		// this is cute, but only reports stats for the leaf nodes... doh.

		List<KleTopic> topics = FluentIterable
				.from(groups)
				.transformAndConcat(new Function<KleMainGroup, FluentIterable<KleGroup>>() {
					@Override
					public FluentIterable<KleGroup> apply(KleMainGroup kleMainGroup) {
						return FluentIterable.from(kleMainGroup.getGroups());
					}
				})
				.transformAndConcat(new Function<KleGroup, FluentIterable<KleTopic>>() {
					@Override
					public FluentIterable<KleTopic> apply(KleGroup kleGroup) {
						return FluentIterable.from(kleGroup.getTopics());
					}
				})
				.toList();

		for (KleTopic topic : topics) {
			if(!topic.getDescription().isEmpty()) {
				int len = topic.getDescription().length();
				max = Math.max(max, len);
				numNonZero++;
				tlen += len;
			}
		}
		*/

		log.info(String.format("%d topics, %d nonzero, %d maxlen, %.3f avglen",
				num, numNonZero, max, (double) tlen / numNonZero));
	}

}
