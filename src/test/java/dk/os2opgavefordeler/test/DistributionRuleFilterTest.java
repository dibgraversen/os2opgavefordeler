package dk.os2opgavefordeler.test;

import dk.os2opgavefordeler.model.DistributionRule;
import dk.os2opgavefordeler.model.Kle;
import dk.os2opgavefordeler.model.Municipality;
import dk.os2opgavefordeler.model.OrgUnit;
import dk.os2opgavefordeler.service.DistributionService;
import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.joda.time.DateTime;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import javax.inject.Inject;

import static org.junit.Assert.assertTrue;

@Category(UnitTest.class)@RunWith(CdiTestRunner.class)
public class DistributionRuleFilterTest {

    @Inject
    private DistributionService distributionService;

    @Test
    @Ignore
    public void testIfNoRulesUseDefault() throws Exception {

        Kle kle = new Kle("1.1.1", "test kle", "blank", DateTime.now().toDate());
        Municipality municipality = new Municipality("test");
        OrgUnit orgUnit = new OrgUnit.Builder().build();


        DistributionRule distributionRule = new DistributionRule.Builder()
                .kle(kle)
                .municipality(municipality)
                .responsibleOrg(orgUnit)
                .build();

        distributionService.createDistributionRule(distributionRule);
distributionService.findAssigned(kle, municipality);
        assertTrue(false);
    }

}
