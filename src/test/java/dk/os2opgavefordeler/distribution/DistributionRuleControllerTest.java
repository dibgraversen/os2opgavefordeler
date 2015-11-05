package dk.os2opgavefordeler.distribution;

import com.google.common.collect.Iterables;
import dk.os2opgavefordeler.model.DistributionRule;
import dk.os2opgavefordeler.model.Municipality;
import dk.os2opgavefordeler.model.OrgUnit;
import dk.os2opgavefordeler.test.UnitTest;
import org.apache.deltaspike.core.api.projectstage.ProjectStage;
import org.apache.deltaspike.testcontrol.api.TestControl;
import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Category(UnitTest.class)
@RunWith(CdiTestRunner.class)
@TestControl(projectStage = ProjectStage.Development.class)
public class DistributionRuleControllerTest {

    @Inject
    private DistributionRuleController controller;

    @Inject
    private DistributionRuleRepository repository;

    @Test
    public void testCanCreateFilter() throws Exception {

        assertTrue(repository.findAll().size() == 0);

        Municipality m = new Municipality("test");
        OrgUnit o = OrgUnit.builder()
                .businessKey("123")
                .municipality(m)
                .build();

        DistributionRule rule = new DistributionRule();
        rule.setMunicipality(m);
        rule.setAssignedOrg(o);
        rule.setResponsibleOrg(o);

        repository.save(rule);

        CprDistributionRuleFilterDTO dto = new CprDistributionRuleFilterDTO();
        dto.name = "TestFilter";
        dto.assignedEmployeeId = -1;
        dto.assignedOrgId = o.getId();
        dto.days = "1-15";
        dto.months = "1-3";
        dto.distributionRuleId = rule.getId();

        controller.createFilter(dto);

        assertTrue(repository.findAll().size() == 1);
        assertEquals(Iterables.size(repository.findBy(rule.getId()).getFilters()), 1);
    }

    @Test
    public void testInvalidOrgThrowsException() {

        Municipality m = new Municipality("test");
        OrgUnit o = OrgUnit.builder()
                .businessKey("123")
                .municipality(m)
                .build();

        DistributionRule rule = new DistributionRule();
        rule.setMunicipality(m);
        rule.setAssignedOrg(o);
        rule.setResponsibleOrg(o);

        repository.save(rule);

        CprDistributionRuleFilterDTO dto = new CprDistributionRuleFilterDTO();
        dto.name = "TestFilter";
        dto.assignedEmployeeId = -1;
        dto.assignedOrgId = 1000;
        dto.days = "1-15";
        dto.months = "1-3";
        dto.distributionRuleId = rule.getId();
        boolean called = false;
        try {
            controller.createFilter(dto);
        } catch (Exception e) {
            called = true;
        }
        assertTrue(called);
    }

    @Test
    public void testFilterNameIsUniqueForRule() throws Exception {

        Municipality m = new Municipality("test");
        OrgUnit o = OrgUnit.builder()
                .businessKey("123")
                .municipality(m)
                .build();

        DistributionRule rule = new DistributionRule();
        rule.setMunicipality(m);
        rule.setAssignedOrg(o);
        rule.setResponsibleOrg(o);

        repository.save(rule);

        CprDistributionRuleFilterDTO dto = new CprDistributionRuleFilterDTO();
        dto.name = "TestFilter";
        dto.assignedEmployeeId = -1;
        dto.assignedOrgId = o.getId();
        dto.days = "1-15";
        dto.months = "1-3";
        dto.distributionRuleId = rule.getId();

        CprDistributionRuleFilterDTO dto2 = new CprDistributionRuleFilterDTO();
        dto2.name = "TestFilter";
        dto2.assignedEmployeeId = -1;
        dto2.assignedOrgId = o.getId();
        dto2.days = "12-15";
        dto2.months = "2-3";
        dto2.distributionRuleId = rule.getId();

        controller.createFilter(dto);

        boolean called = false;

        try {
            controller.createFilter(dto2);
        } catch (DistributionRule.AlreadyHaveFilterWithNameException e) {
            called = true;
        }

        assertTrue(called);
    }

    @Test
    public void testCanDeleteFilter() throws Exception {
        Municipality m = new Municipality("test");
        OrgUnit o = OrgUnit.builder()
                .businessKey("123")
                .municipality(m)
                .build();

        DistributionRule rule = new DistributionRule();
        rule.setMunicipality(m);
        rule.setAssignedOrg(o);
        rule.setResponsibleOrg(o);

        repository.save(rule);

        CprDistributionRuleFilterDTO dto = new CprDistributionRuleFilterDTO();
        dto.name = "TestFilter";
        dto.assignedEmployeeId = -1;
        dto.assignedOrgId = o.getId();
        dto.days = "1-15";
        dto.months = "1-3";
        dto.distributionRuleId = rule.getId();

        controller.createFilter(dto);

        assertTrue(repository.findAll().size() == 1);
        assertEquals(Iterables.size(repository.findBy(rule.getId()).getFilters()), 1);

        controller.deleteFilter(rule.getId(), "TestFilter");
        assertEquals(Iterables.size(repository.findBy(rule.getId()).getFilters()), 0);

    }
}