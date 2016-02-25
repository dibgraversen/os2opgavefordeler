package dk.os2opgavefordeler.model;

import dk.os2opgavefordeler.test.UnitTest;
import org.apache.deltaspike.core.api.projectstage.ProjectStage;
import org.apache.deltaspike.testcontrol.api.TestControl;
import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.HashMap;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

@Category(UnitTest.class)
@RunWith(CdiTestRunner.class)
@TestControl(projectStage = ProjectStage.Development.class)
public class TextDistributionRuleFilterTest {

    @Inject
    private EntityManager entityManager;

    @Test
    public void testMatches() throws Exception {
        //String name, DistributionRule distributionRule, OrgUnit orgUnit, Employment employment, String text


        Municipality m = new Municipality("test");

        OrgUnit o = new OrgUnit();
        o.setIsActive(true);
        o.setMunicipality(m);
        o.setName("foo");
        o.setBusinessKey("foobk");
        o.setEmail("test@flaf.dk");
        o.setEsdhLabel("esdhlabel");


        DistributionRule d = new DistributionRule();
        d.setMunicipality(m);
        d.setAssignedOrg(o);

        Employment e = new Employment();
        e.setMunicipality(m);
        e.setIsActive(true);
        e.setName("test");

        entityManager.getTransaction().begin();
        entityManager.persist(m);
        entityManager.persist(o);
        entityManager.persist(d);
        entityManager.persist(e);
        entityManager.getTransaction().commit();

        TextDistributionRuleFilter f = new TextDistributionRuleFilter("subject", d, o, e, "of");


        assertFalse(f.matches(new HashMap<String, String>() {{
            put("subject", "foo");
        }}));

        assertTrue(f.matches(new HashMap<String, String>() {{
            put("subject", "of");
        }}));


        TextDistributionRuleFilter beginsWith = new TextDistributionRuleFilter("subject", d, o, e, "*of");

        assertFalse(beginsWith.matches(new HashMap<String, String>() {{
            put("subject", "off");
        }}));

        assertTrue(beginsWith.matches(new HashMap<String, String>() {{
            put("subject", "fof");
        }}));

    }


}