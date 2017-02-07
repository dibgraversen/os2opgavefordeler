package dk.os2opgavefordeler.rest;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import dk.os2opgavefordeler.model.presentation.OrgUnitWithKLEPO;
import dk.os2opgavefordeler.service.OrgUnitWithKLEService;

//ApplicationScoped for test only. Should be RequestScoped with ApplicationScoped repository.
@Path("/ou")
@ApplicationScoped
public class OUEndpoint extends Endpoint {

	@Inject
	private OrgUnitWithKLEService orgUnitService;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response list() {
		List<OrgUnitWithKLEPO> result = orgUnitService.getAll(1L);		
		return Response.ok().entity(result).build();		
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{Id}")
	public Response get(@PathParam("Id") long id) {
		OrgUnitWithKLEPO result = orgUnitService.get(id);
		if (result != null) {
			return Response.ok().entity(result).build();
		} else {
			return Response.status(404).entity("Entity not found for ID: " + id).build();
		}
	}

	// @POST
	// @Path("/")
	// @Consumes(MediaType.APPLICATION_JSON)
	// @Produces(MediaType.APPLICATION_JSON)
	// public Response post(OURestResultPO input) {
	// ous.add(input);
	// return Response.ok().build();
	// }
	//
	// @POST
	// @Path("/{Id}/kle/{kleNumber}")
	// @Consumes(MediaType.APPLICATION_JSON)
	// @Produces(MediaType.APPLICATION_JSON)
	// public Response addKLE(@PathParam("Id") long id,@PathParam("kleNumber")
	// String kleNumber, JSONObject input) {
	// //Gather data
	// Optional<OURestResultPO> ou = ous.stream().filter(x ->
	// x.getId()==id).findFirst();
	// KleRestResultPO kle = findKLE(kles, kleNumber);
	// String assignmentType = input.get("assignmentType")+"";
	// //add kle
	// if(ou.isPresent() && kle!=null){
	// ou.get().addKle(kle.getNumber(),assignmentType);
	// }else{
	// return Response.status(404).build();
	// }
	// return Response.ok().entity(input).build();
	// }
	//
	// @POST
	// @Path("/{Id}/removeKLE")
	// @Consumes(MediaType.APPLICATION_JSON)
	// @Produces(MediaType.APPLICATION_JSON)
	// public Response removeKLE(@PathParam("Id") long id, JSONObject input) {
	// //gather
	// Optional<OURestResultPO> ou = ous.stream().filter(x ->
	// x.getId()==id).findFirst();
	// //remove kle
	// if(ou.isPresent()){
	// if(input.containsKey("number")){
	// String number = input.get("number").toString();
	// ou.get().removeKle(number);
	// }
	// }else{
	// return Response.status(404).build();
	// }
	// return Response.ok().entity(input).build();
	// }

}
