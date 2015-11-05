package dk.os2opgavefordeler.distribution;

import dk.os2opgavefordeler.model.DistributionRule;
import dk.os2opgavefordeler.model.DistributionRuleFilter;
import dk.os2opgavefordeler.model.presentation.DistributionRulePO;
import dk.os2opgavefordeler.rest.Endpoint;
import dk.os2opgavefordeler.service.BootstrappingDataProviderSingleton;
import dk.os2opgavefordeler.service.OrgUnitService;
import org.apache.deltaspike.jpa.api.transaction.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@Path("/distributionrulefilter")
@Transactional
public class DistributionRuleFilterEndpoint extends Endpoint {

    @Inject
    private BootstrappingDataProviderSingleton bootstrap;

    @Inject
    private DistributionRuleRepository distributionRuleRepository;

    @Inject
    private OrgUnitService orgUnitService;

    @Inject
    private EntityManager entityManager;

    @Inject
    private DistributionRuleController controller;

    @GET
    @Path("/bootstrap")
    public Response bootstrap() {
        bootstrap.bootstrap();
        return ok();
    }

    @GET
    @Path("/")
    public Response list() {
        return ok();
    }

    @POST
    @Path("/")
    public Response createFilter(CprDistributionRuleFilterDTO dto) {
        try {
            controller.createFilter(dto);
            return ok();
        } catch (Exception e) {
            return badRequest(e.getMessage());
        }
    }

    @DELETE
    @Path("/{distributionRuleId}/{name}")
    public Response deleteFilter(
            @PathParam("distributionRuleId") long distributionRuleId,
            @PathParam("name") String name) {

        DistributionRule rule;
        try {
            rule = distributionRuleRepository.findBy(distributionRuleId);
        } catch (NoResultException e) {
            return badRequest(
                    String.format("Distribution rule with id %s could not be found!",
                            distributionRuleId));
        }


        rule.removeFiltersWithName(name);
        distributionRuleRepository.save(rule);

        return ok();

    }

    public Response createRule(DistributionRulePO dto) {
        return ok();
    }

    public Response deleteRule(long id) {
        return ok();
    }

    public Response addFilter(long id, DistributionRuleFilter filter) {
        return ok();
    }

    public Response removeFilter(long id, long filterId) {
        return ok();
    }
}
