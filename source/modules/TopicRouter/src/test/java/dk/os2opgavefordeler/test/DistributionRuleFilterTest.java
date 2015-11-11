package dk.os2opgavefordeler.test;

import dk.os2opgavefordeler.Kle.KleRepository;
import dk.os2opgavefordeler.assigneesearch.Assignee;
import dk.os2opgavefordeler.assigneesearch.FindAssignedForKleService;
import dk.os2opgavefordeler.distribution.DistributionRuleRepository;
import dk.os2opgavefordeler.model.*;
import dk.os2opgavefordeler.service.BootstrappingDataProviderSingleton;
import dk.os2opgavefordeler.service.DistributionService;
import dk.os2opgavefordeler.service.KleService;
import dk.os2opgavefordeler.service.MunicipalityService;
import org.apache.deltaspike.core.api.projectstage.ProjectStage;
import org.apache.deltaspike.testcontrol.api.TestControl;
import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

@Category(UnitTest.class)
@RunWith(CdiTestRunner.class)
@TestControl(projectStage = ProjectStage.Development.class)
public class DistributionRuleFilterTest {

    @Inject
    private DistributionService distributionService;

    @Inject
    private MunicipalityService municipalityService;

    @Inject
    private KleService kleService;

    @Inject
    private EntityManager entityManager;

    @Inject
    private KleRepository kleRepository;

    @Inject
    private FindAssignedForKleService findAssignedForKleService;

    @Inject
    private BootstrappingDataProviderSingleton bootstrap;

    @Inject
    private DistributionRuleRepository repository;

    @Test
    public void testIfNoRulesUseDefault() throws Exception {
        Municipality municipality = new Municipality("test");
        Kle kle = new Kle("1.1.1", "test kle", "blank", DateTime.now().toDate());
        kle.setMunicipality(municipality);
        kleRepository.save(kle);


        OrgUnit orgUnit = OrgUnit.builder()
                .name("f")
                .email("e@f.dk")
                .esdhId("foo")
                .esdhLabel("flaf")
                .phone("l")
                .municipality(municipality)
                .businessKey("businessKey")
                .build();

        municipalityService.createMunicipality(municipality);


        DistributionRule distributionRule = new DistributionRule.Builder()
                .kle(kle)
                .municipality(municipality)
                .responsibleOrg(orgUnit)
                .build();
        distributionRule.setAssignedOrg(orgUnit);

        distributionService.createDistributionRule(distributionRule);
        Assignee assigned = findAssignedForKleService.findAssignedForKle(kle, municipality);

        assertEquals(orgUnit, assigned.getOrgUnit());
    }

    @Test
    public void testBootstrap() throws Exception{
        bootstrap.bootstrap();
    }

    @Test
    public void testIfThereIsAFilterAndItMatchesReturnThat() throws Exception {
        Kle kle = new Kle("1.1.1", "test kle", "blank", DateTime.now().toDate());
        kleRepository.save(kle);

        Municipality municipality = new Municipality("test");

        OrgUnit orgUnit = OrgUnit.builder()
                .name("f")
                .email("e@f.dk")
                .esdhId("foo")
                .esdhLabel("flaf")
                .phone("l")
                .municipality(municipality)
                .businessKey("businessKey")
                .build();

        OrgUnit correctOrgUnit = OrgUnit.builder()
                .name("f")
                .email("e@f.dk")
                .esdhId("foo")
                .esdhLabel("flaf")
                .phone("l")
                .municipality(municipality)
                .businessKey("businessKey")
                .build();

        municipalityService.createMunicipality(municipality);

        entityManager.persist(orgUnit);
        entityManager.persist(correctOrgUnit);



        DistributionRule distributionRule = new DistributionRule.Builder()
                .kle(kle)
                .municipality(municipality)
                .responsibleOrg(orgUnit)
                .build();
        distributionRule.setAssignedOrg(orgUnit);

        CprDistributionRuleFilter dateFilter = new CprDistributionRuleFilter();
        dateFilter.setAssignedOrg(correctOrgUnit);
        dateFilter.setDistributionRule(distributionRule);
        dateFilter.setName("cpr");
        dateFilter.setDays("1-31");
        dateFilter.setMonths("1-12");



        //repository.save(distributionRule);

        //entityManager.persist(dateFilter);
        distributionRule.addFilter(dateFilter);

        repository.save(distributionRule);

        Map<String,String> filterParameters = new HashMap<>();
        filterParameters.put("cpr", "141186-1145");

        Assignee assignedForKle = findAssignedForKleService.findAssignedForKle(kle, municipality, filterParameters);
        assertEquals(correctOrgUnit, assignedForKle.getOrgUnit());


    }
}
