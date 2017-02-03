package dk.os2opgavefordeler.rest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import dk.os2opgavefordeler.model.rest.KLEAssignmentPO;
import dk.os2opgavefordeler.model.rest.KleExtended;
import dk.os2opgavefordeler.model.rest.KleRestResultPO;
import dk.os2opgavefordeler.model.rest.OURestResultPO;
import net.minidev.json.JSONObject;

//ApplicationScoped for test only. Should be RequestScoped with ApplicationScoped repository.
@Path("/ou")
@ApplicationScoped
public class OUEndpoint extends Endpoint {

	private List<OURestResultPO> ous;
	private List<KleRestResultPO> kles;

	public OUEndpoint() {

		ous = new ArrayList<>();
		ous.add(new OURestResultPO(1, 0, "OU1"));
		ous.add(new OURestResultPO(2, 1, "OU2"));
		ous.add(new OURestResultPO(3, 1, "OU3"));
		ous.add(new OURestResultPO(4, 1, "OU4"));
		ous.add(new OURestResultPO(5, 1, "OU5"));

		kles = new ArrayList<>();

		// --- 00
		List<KleRestResultPO> children = new ArrayList<>();
		children.add(new KleRestResultPO("00.05", "Besøg, repræsentation mv.",
				Arrays.asList(new KleRestResultPO("00.05.01", "Venskabsbysamarbejde"),
						new KleRestResultPO("00.05.10", "Gaver til og fra kommunen/institutionen"))));
		children.add(new KleRestResultPO("00.17", "Kommunalt/tværsektorielt samarbejde  Servicetekst  Stikord",
				Arrays.asList(new KleRestResultPO("00.17.15", "Kommunale samarbejder"), new KleRestResultPO("00.17.20",
						"Aftaler om samarbejde mellem kommuner (kommunale fællesskaber)"))));

		kles.add(new KleRestResultPO("00", "Kommunens styrelse", children));
		// --- 03
		children = new ArrayList<>();
		children.add(new KleRestResultPO("03.01", "Benyttelse af boliger",
				Arrays.asList(new KleRestResultPO("03.01.00", "Benyttelse af boliger i almindelighed"),
						new KleRestResultPO("03.01.03", "Nedlæggelse af boliger"))));
		children.add(new KleRestResultPO("03.22", "Friplejeboliger",
				Arrays.asList(new KleRestResultPO("03.22.00", "Friplejeboliger i almindelighed"),
						new KleRestResultPO("03.22.05", "Etablering af friplejeboliger"),
						new KleRestResultPO("03.22.10", "Drift og afhændelse af friplejeboliger"))));

		kles.add(new KleRestResultPO("03", "Boliger", children));

		KleRestResultPO kle = findKLE(kles, "03.22");
		ous.get(2).addKle(kle.getNumber(), "Type1");
	}

	/**
	 * This method returns a KleRestResultPO object with number matching code
	 * or null if object not found.
	 * @param kles
	 * @param code
	 * @return KleRestResultPO, null
	 */
	private KleRestResultPO findKLE(List<KleRestResultPO> kles, String code) {
		KleRestResultPO result;
		for (KleRestResultPO kle : kles) {
			result = findKLE(kle, code);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Recursively looks for an KLE where number = code
	 * @param kle
	 * @param code
	 * @return KleRestResultPO, null
	 */
	private KleRestResultPO findKLE(KleRestResultPO kle, String code) {
		if (kle.getNumber().equals(code)) {
			return kle;
		}
		if (kle.getChildren() != null && !kle.getChildren().isEmpty()) {
			KleRestResultPO result;
			for (KleRestResultPO kleChild : kle.getChildren()) {
				result = findKLE(kleChild, code);
				if (result != null) {
					return result;
				}
			}
		}
		return null;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response list() {
		if (!ous.isEmpty()) {
			return Response.ok().entity(ous).build();
		} else {
			return Response.status(404).build();
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{Id}")
	public Response get(@PathParam("Id") long id) {
		OURestResultPO ou = ous.stream().filter(x -> x.getId() == id).findAny().orElse(null);
		if (ou != null) {
			return Response.ok(ou).build();
		} else {
			return Response.status(404).build();
		}
	}

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response post(OURestResultPO input) {
		ous.add(input);
		return Response.ok().build();
	}
	
	@POST
	@Path("/{Id}/addKLE")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response addKLE(@PathParam("Id") long id, KLEAssignmentPO input) {
		//Gather data
		Optional<OURestResultPO> ou = ous.stream().filter(x -> x.getId()==id).findFirst();
		KleRestResultPO kle = findKLE(kles, input.getNumber());
		//add kle
		if(ou.isPresent() && kle!=null){
			ou.get().addKle(kle.getNumber(),input.getAssignmentType());
		}else{
			return Response.status(404).build();
		}
		return Response.ok().entity(input).build();
	}
	
	@POST
	@Path("/{Id}/removeKLE")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response removeKLE(@PathParam("Id") long id, JSONObject input) {
		//gather 
		Optional<OURestResultPO> ou = ous.stream().filter(x -> x.getId()==id).findFirst();
		//remove kle
		if(ou.isPresent()){
			if(input.containsKey("number")){
				String number = input.get("number").toString();				
				ou.get().removeKle(number);
			}			
		}else{
			return Response.status(404).build();
		}
		return Response.ok().entity(input).build();
	}

}
