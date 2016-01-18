package dk.os2opgavefordeler.orgunit;

import dk.os2opgavefordeler.auth.CurrentUser;
import dk.os2opgavefordeler.model.OrgUnit;
import dk.os2opgavefordeler.model.User;
import dk.os2opgavefordeler.model.presentation.OrgUnitPO;
import dk.os2opgavefordeler.rest.Endpoint;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Path("/org-unit-import")
public class ImportEndpoint extends Endpoint {

    @Inject
    private Logger logger;

    @Inject
    private ImportService importService;

    @Inject
    @CurrentUser
    private User currentUser;

    @POST
    @Consumes("application/json")
    @Produces("application/json")
    @Path("/")
    public Response import_(OrgUnitDTO orgUnitDTO) {
        logger.info("Importing organizational unit");
        logger.info(orgUnitDTO.toString());

        if (currentUser == null) {
            return badRequest("No user");
        }

        try {
            OrgUnit o = importService.importOrganization(currentUser.getMunicipality().getId(), orgUnitDTO);
            return Response
                    .ok()
                    .entity(o.getId())
                    .build();
        } catch (ImportService.InvalidMunicipalityException e) {
            return badRequest("ERROR");
        }
    }

}
