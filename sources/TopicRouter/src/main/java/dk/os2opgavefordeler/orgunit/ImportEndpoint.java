package dk.os2opgavefordeler.orgunit;

import dk.os2opgavefordeler.auth.AuthService;
import dk.os2opgavefordeler.employment.UserRepository;
import dk.os2opgavefordeler.model.OrgUnit;
import dk.os2opgavefordeler.model.User;
import dk.os2opgavefordeler.rest.Endpoint;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

@Path("/org-unit-import")
public class ImportEndpoint extends Endpoint {

    @Inject
    private Logger logger;

    @Inject
    private ImportService importService;

    @Inject
    private UserRepository userRepository;

    @Inject
    private AuthService authService;

    @Context
    private HttpServletRequest request;

    @POST
    @Consumes("application/json")
    @Produces("application/json")
    @Path("/")
    public Response import_(OrgUnitDTO orgUnitDTO) {
        logger.info("Importing organizational unit");
        logger.info(orgUnitDTO.toString());

        if (!authService.isAuthenticated()) {
            return badRequest("No user");
        }

        User u = userRepository.findByEmail(authService.getAuthentication().getEmail());

        try {
            OrgUnit o = importService.importOrganization(u.getMunicipality().getId(), orgUnitDTO);

            return Response
                    .ok()
                    .entity(o.getId())
                    .build();
        }
        catch (ImportService.InvalidMunicipalityException e) {
            logger.error("Invalid municipality for import: {}", e);

	        return badRequest("ERROR");
        }
    }

}
