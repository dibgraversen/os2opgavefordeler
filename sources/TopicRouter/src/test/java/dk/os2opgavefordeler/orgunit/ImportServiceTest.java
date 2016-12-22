package dk.os2opgavefordeler.orgunit;

import com.google.common.collect.Lists;
import dk.os2opgavefordeler.employment.MunicipalityRepository;
import dk.os2opgavefordeler.employment.OrgUnitRepository;
import dk.os2opgavefordeler.model.Municipality;
import dk.os2opgavefordeler.model.OrgUnit;
import dk.os2opgavefordeler.test.UnitTest;
import org.apache.deltaspike.core.api.projectstage.ProjectStage;
import org.apache.deltaspike.testcontrol.api.TestControl;
import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import javax.inject.Inject;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertTrue;

@Category(UnitTest.class)
@RunWith(CdiTestRunner.class)
@TestControl(projectStage = ProjectStage.Development.class)
public class ImportServiceTest {
	Municipality municipality = new Municipality("testMunicipality");

	@Inject
	private MunicipalityRepository municipalityRepository;

	@Inject
	private OrgUnitRepository orgUnitRepository;

	@Inject
	private ImportService importService;

	@Before
	public void setUp() throws Exception {
		municipalityRepository.save(municipality);
	}

	@After
	public void tearDown() throws Exception {

	}

	@Test
	public void minimal() throws Exception {
		String businessKey = "testBusinessKey";

		OrgUnitDTO orgUnit = new OrgUnitDTO(businessKey);
		importService.importOrganization(municipality.getId(), orgUnit);

		assertEquals(businessKey, orgUnitRepository.findByBusinessKeyAndMunicipalityId(orgUnit.businessKey, municipality.getId()).getBusinessKey());
	}

	@Test
	public void invalidMunicipalityThrowsException() {
		int thrown = 0;
		try {
			importService.importOrganization(-1, null);
		} catch (ImportService.InvalidMunicipalityException e) {
			thrown++;
		}

		assertEquals(1, thrown);
	}

	private EmployeeDTO createEmployeeDto(String name) {
		EmployeeDTO e = new EmployeeDTO();
		e.name = name;
		return e;
	}

	@Test
	public void employeesAreImported() throws Exception {
		OrgUnitDTO orgUnitDTO = new OrgUnitDTO("foo");
		orgUnitDTO.employees = Lists.newArrayList(createEmployeeDto("foo"), createEmployeeDto("bar"));
		importService.importOrganization(municipality.getId(), orgUnitDTO);

		OrgUnit o = orgUnitRepository.findByBusinessKeyAndMunicipalityId("foo", municipality.getId());

		assertEquals(orgUnitDTO.employees.size(), o.getEmployees().size());
	}

	@Test
	public void managersAreImported() throws Exception {
		OrgUnitDTO orgUnitDTO = new OrgUnitDTO("foo");
		orgUnitDTO.name = "Test Department";
		orgUnitDTO.manager = createEmployeeDto("flaf");
		importService.importOrganization(municipality.getId(), orgUnitDTO);

		OrgUnit o = orgUnitRepository.findByBusinessKeyAndMunicipalityId("foo", municipality.getId());

		assertNotNull(o);
		assertEquals(1, o.getEmployees().size());
		assertEquals("flaf", o.getManager().get().getName());

	}

//	@Test
	public void subOrganizationsAreImported() throws Exception {

	}

	@Test
	public void testNoDuplicateEmployees() throws Exception {
		String businessKey = "orgUnitBusinessKey";
		OrgUnitDTO orgUnitDTO = new OrgUnitDTO(businessKey);
		orgUnitDTO.name = "Some Department";
		EmployeeDTO manager = createEmployeeDto("Bill");
		manager.businessKey = "managerbk";
		orgUnitDTO.manager = manager;
		importService.importOrganization(municipality.getId(), orgUnitDTO);

		OrgUnit o = orgUnitRepository.findByBusinessKeyAndMunicipalityId(businessKey, municipality.getId());
		assertNotNull(o);
		assertTrue(o.getManager().isPresent());
	}
}
