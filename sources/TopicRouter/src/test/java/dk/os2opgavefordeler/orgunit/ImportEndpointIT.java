package dk.os2opgavefordeler.orgunit;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Header;
import dk.os2opgavefordeler.model.Municipality;
import dk.os2opgavefordeler.model.OrgUnit;
import dk.os2opgavefordeler.model.Role;
import dk.os2opgavefordeler.model.User;
import dk.os2opgavefordeler.model.presentation.OrgUnitPO;
import dk.os2opgavefordeler.test.UnitTest;
import org.apache.commons.codec.binary.Base64;
import org.apache.deltaspike.core.api.projectstage.ProjectStage;
import org.apache.deltaspike.testcontrol.api.TestControl;
import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;

@Category(UnitTest.class)
@RunWith(CdiTestRunner.class)
@TestControl(projectStage = ProjectStage.IntegrationTest.class)
public class ImportEndpointIT {

    public static final String ORG_UNIT_ENDPOINT = "http://localhost:1080/rest/org-unit-import";
    @Inject
    private Logger logger;
    private EntityManager entityManager;
    private Municipality municipality;
    private User user;

    @Before
    public void init() {
        entityManager = Persistence.createEntityManagerFactory("integration-test-db").createEntityManager();

        municipality = new Municipality();
        municipality.setActive(true);
        municipality.setName("test");
        municipality.setToken("test");

        Role role = new Role();
        role.setName("test role");
        role.setAdmin(true);
        role.setManager(true);
        role.setMunicipalityAdmin(true);
        role.setSubstitute(false);

        user = new User();
        user.setName("kkj");
        user.setEmail("test@foo.dk");
        user.setMunicipality(municipality);

        entityManager.getTransaction().begin();
        entityManager.persist(municipality);
        entityManager.persist(user);
        entityManager.getTransaction().commit();

        logger.info("Created municipality {}", municipality);
        logger.info("Created user {}", user);
    }

    @After
    public void cleanup() {
        entityManager.getTransaction().begin();
        entityManager.remove(user);
        entityManager.remove(municipality);
        entityManager.getTransaction().commit();
    }

    private Header authHeader() {
        return new Header(
                "Authorization",
                "Basic " + Base64.encodeBase64String((user.getEmail() + ":" + municipality.getToken()).getBytes()
                )
        );
    }


    @Test
    public void testImport_() throws Exception {
        OrgUnitDTO dto = new OrgUnitDTO("test");

        Long id = RestAssured.given()
                .body(dto)
                .contentType(ContentType.JSON)
                .header(authHeader())
                .post(ORG_UNIT_ENDPOINT)
                //.andReturn().as(OrgUnit.class)
                .then()
                .assertThat()
                .statusCode(200).extract().as(Long.class);

        OrgUnit orgUnit = entityManager.find(OrgUnit.class, id);
        entityManager.getTransaction().begin();
        entityManager.remove(orgUnit);
        entityManager.getTransaction().commit();
    }
}