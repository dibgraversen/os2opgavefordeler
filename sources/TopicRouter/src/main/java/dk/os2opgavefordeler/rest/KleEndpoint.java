package dk.os2opgavefordeler.rest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import dk.os2opgavefordeler.model.rest.KleRestResultPO;

@Path("/kle2")
@RequestScoped
public class KleEndpoint extends Endpoint {
	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response list() {
		List<KleRestResultPO> kles = new ArrayList<>();

		//--- 00
		List<KleRestResultPO> children = new ArrayList<>();
		children.add(new KleRestResultPO("00.05", "Besøg, repræsentation mv.",
				Arrays.asList(new KleRestResultPO("00.05.01", "Venskabsbysamarbejde"),
						new KleRestResultPO("00.05.10", "Gaver til og fra kommunen/institutionen"))));
		children.add(new KleRestResultPO("00.17", "Kommunalt/tværsektorielt samarbejde  Servicetekst  Stikord",
				Arrays.asList(new KleRestResultPO("00.17.15", "Kommunale samarbejder"),
						new KleRestResultPO("00.17.20", "Aftaler om samarbejde mellem kommuner (kommunale fællesskaber)"))));

		kles.add(new KleRestResultPO("00", "Kommunens styrelse", children));
		//--- 03
		children = new ArrayList<>();
		children.add(new KleRestResultPO("03.01", "Benyttelse af boliger",
				Arrays.asList(new KleRestResultPO("03.01.00", "Benyttelse af boliger i almindelighed"),
						new KleRestResultPO("03.01.03", "Nedlæggelse af boliger"))));
		children.add(new KleRestResultPO("03.22", "Friplejeboliger",
				Arrays.asList(new KleRestResultPO("03.22.00", "Friplejeboliger i almindelighed"),
						new KleRestResultPO("03.22.05", "Etablering af friplejeboliger"),
						new KleRestResultPO("03.22.10", "Drift og afhændelse af friplejeboliger"))));
		
		kles.add(new KleRestResultPO("03", "Boliger", children));

		if (!kles.isEmpty()) {
			return Response.ok().entity(kles).build();
		} else {
			return Response.status(404).build();
		}
	}
}
