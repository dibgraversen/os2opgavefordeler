package dk.os2opgavefordeler.rest;

import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import dk.os2opgavefordeler.model.kle.KleGroup;
import dk.os2opgavefordeler.model.kle.KleMainGroup;
import dk.os2opgavefordeler.model.kle.KleParent;
import dk.os2opgavefordeler.model.kle.KleTopic;
import dk.os2opgavefordeler.model.presentation.DistributionRulePO;
import dk.os2opgavefordeler.service.PersistenceService;
import dk.osto.model.KLE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Path("/distribution-rules")
@RequestScoped
public class DistributionRuleEndpoint {
	private final Logger log = LoggerFactory.getLogger(getClass());

	@Inject
	PersistenceService persistence;

	/**
	 *
	 * @param employment The employment for whom to look up TopicRoutes
	 * @param scope The scope for which to get the TopicRoutes. Can be ALL, MINE or ALL_MINE.
	 * @return a list of TopicRoutePO's matching the employment and scope.
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response routesForEmployment(@QueryParam("employment") Integer employment, @QueryParam("scope") String scope) {
		//Mock code!
		//Assumptions:
		//	three test users
		//	main groups split evenly between users
		//	Explicit mappings for ALL subgroups and topics

		log.info("routesForEmployment[{},{}]", employment, scope);

		if(employment == null || scope == null) {
			return Response.status(Response.Status.BAD_REQUEST).build();
		}

		if(!Range.closed(1, 3).contains(employment)) {
			log.info("Employment {} out of bounds, returning NO_CONTENT", employment);
			return Response.noContent().build();
		}

		final List<KleMainGroup> groups = persistence.fetchAllKleMainGroups();
		if(groups.isEmpty()) {
			log.info("persistence.fetchAllKleMainGroups: empty, returning NO_CONTENT");
			return Response.noContent().build();
		}


		final List<DistributionRulePO> result = new ArrayList<>();
		int currentPoId = 1;
		for(KleMainGroup main : Lists.partition(groups, 3).get(employment-1)) {
			addPo(result, main, employment, currentPoId);
			++currentPoId;

			for (KleGroup group : main.getGroups()) {
				addPo(result, group, employment, currentPoId);
				++currentPoId;

				for (KleTopic topic : group.getTopics()) {
					addPo(result, topic, employment, currentPoId);
					++currentPoId;
				}
			}
		}

		return Response.ok(result).build();
	}

	private void addPo(List<DistributionRulePO> result, KleParent kle, Integer employment, int id) {
		final DistributionRulePO po = new DistributionRulePO();

		po.setId(id);
		po.setParent(id + 1);
		po.setEmployee(employment);
		po.setResponsible(employment);
		po.setOrg(42);
		po.setKle(kleFrom(kle));

		result.add(po);
	}

	private static KLE kleFrom(KleParent in) {
		KLE kle = new KLE();

		kle.setNumber(in.getNumber());
		kle.setName(in.getTitle());
		kle.setServiceText(in.getDescription());

		if(in instanceof KleMainGroup) {
			kle.setType("main");
		} else if(in instanceof KleGroup) {
			kle.setType("group");
		} else if(in instanceof KleTopic) {
			kle.setType("topic");
		} else {
			kle.setType("<unknown>");
		}

		return kle;
	}
}
