package dk.os2opgavefordeler.orgunit;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Header;
import dk.os2opgavefordeler.model.Municipality;
import dk.os2opgavefordeler.model.Role;
import dk.os2opgavefordeler.model.User;
import org.apache.commons.codec.binary.Base64;
import org.junit.Test;

public class ImportEndpointIT {

    private String municipality_endpoint = "http://localhost:1080/rest/municipalities";
    private String orgunit_endpoint = "http://localhost:1080/rest/org-unit-import";
    private String user_endpoint = "http://localhost:1080/rest/users";

    @Test
    public void testImport_() throws Exception {

        Municipality municipality = new Municipality();
        municipality.setActive(true);
        municipality.setName("test");
        municipality.setToken("test");

        municipality = RestAssured.given()
                .body(municipality)
                .contentType(ContentType.JSON)
                .post(municipality_endpoint)
                .andReturn()
                .as(Municipality.class);

        Role role = new Role();
        role.setName("test role");
        role.setAdmin(true);
        role.setManager(true);
        role.setMunicipalityAdmin(true);
        role.setSubstitute(false);


        User user = new User();
        user.setName("kkj");
        user.setEmail("test@foo.dk");
        user.setMunicipality(municipality);
        //user.addRole(role);


        user = RestAssured.given()
                .body(user)
                .contentType(ContentType.JSON)
                .post(user_endpoint)
                .andReturn().as(User.class);


        OrgUnitDTO dto = new OrgUnitDTO("test");

        RestAssured.given()
                .body(dto)
                .contentType(ContentType.JSON)
                .header(
                        new Header(
                                "Authorization",
                                "Basic " + Base64.encodeBase64String( (user.getEmail()+":"+municipality.getToken() ).getBytes()
                                )
                        )
                )
                .post(orgunit_endpoint)
                .then()
                .assertThat()
                .statusCode(200);

        RestAssured.given()
                .delete(user_endpoint + "/" + user.getId())
                .then()
                .assertThat()
                .statusCode(200);

        RestAssured.given()
                .delete(municipality_endpoint + "/" + municipality.getId())
                .then()
                .assertThat()
                .statusCode(200);


    }
}