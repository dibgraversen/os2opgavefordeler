package dk.os2opgavefordeler.distribution;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import org.apache.deltaspike.jpa.api.transaction.Transactional;

import javax.inject.Inject;

import javax.persistence.EntityManager;

import dk.os2opgavefordeler.distribution.dto.CprDistributionRuleFilterDTO;
import dk.os2opgavefordeler.distribution.dto.DistributionRuleFilterDTO;
import dk.os2opgavefordeler.distribution.dto.TextDistributionRuleFilterDTO;

import dk.os2opgavefordeler.model.CprDistributionRuleFilter;
import dk.os2opgavefordeler.model.DistributionRule;
import dk.os2opgavefordeler.model.DistributionRuleFilter;
import dk.os2opgavefordeler.model.TextDistributionRuleFilter;
import dk.os2opgavefordeler.model.presentation.DistributionRulePO;

import dk.os2opgavefordeler.rest.Endpoint;

import dk.os2opgavefordeler.service.BootstrappingDataProviderSingleton;
import dk.os2opgavefordeler.service.OrgUnitService;

@Path("/distributionrulefilter")
@Transactional
public class DistributionRuleFilterEndpoint extends Endpoint {

    @Inject
    private BootstrappingDataProviderSingleton bootstrap;

    @Inject
    private DistributionRuleRepository ruleRepository;

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
    @Path("/{ruleId}/filters")
    @Produces("application/json")
    public Response list(@PathParam("ruleId") long ruleId) {
        DistributionRule rule = ruleRepository.findBy(ruleId);

        if (rule == null){
            return ok();
        }

        Iterable<DistributionRuleFilter> filters = rule.getFilters();

        List<DistributionRuleFilterDTO> result = new ArrayList<>();

        for (DistributionRuleFilter f: filters) {
            if (f instanceof CprDistributionRuleFilter) {
                result.add(new CprDistributionRuleFilterDTO((CprDistributionRuleFilter) f));
            }
            else if (f instanceof TextDistributionRuleFilter){
                result.add(new TextDistributionRuleFilterDTO((TextDistributionRuleFilter) f));
            }
        }

        return ok(result);
    }

    @POST
    @Path("/{ruleId}/filters/{filterId}")
    public Response updateFilter(
            @PathParam("ruleId") long ruleId,
            @PathParam("filterId") long filterId,
            DistributionRuleFilterDTO dto) {
        try {
            controller.updateFilter(ruleId, filterId, dto);
            return ok();
        } catch (Exception e) {
            return badRequest(e.getMessage());
        }
    }

    @POST
    @Path("/")
    public Response createFilter(DistributionRuleFilterDTO dto) {
        try {
            if(dto.filterId == 0) {
                controller.createFilter(dto);
            } else {
                controller.updateFilter(dto.distributionRuleId, dto.filterId, dto);
            }
            return ok();
        } catch (Exception e) {
            return badRequest(e.getMessage());
        }
    }

    @DELETE
    @Path("/{distributionRuleId}/{id}")
    public Response deleteFilter(
            @PathParam("distributionRuleId") long distributionRuleId,
            @PathParam("id") long filterId) {

        try {
            controller.deleteFilter(distributionRuleId, filterId);
        } catch (Exception e) {
            return badRequest(e.getMessage());
        }

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
