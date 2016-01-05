package dk.os2opgavefordeler.distribution;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import dk.os2opgavefordeler.distribution.dto.CprDistributionRuleFilterDTO;
import dk.os2opgavefordeler.test.IntegrationTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(IntegrationTest.class)
public class DistributionRuleFilterEndpointIT {

    private String endpoint = "http://localhost:8080/TopicRouter/rest/distributionrulefilter";

    @Test
    public void testCreateFilter() throws Exception {

        CprDistributionRuleFilterDTO dto = new CprDistributionRuleFilterDTO();
        dto.name = "TestFilter";
        dto.assignedEmployeeId = 6;
        dto.assignedOrgId = 1;
        dto.days = "1-15";
        dto.months = "1-3";
        dto.distributionRuleId = 1;

        RestAssured.given()
                .body(dto)
                .contentType(ContentType.JSON)
                .post(endpoint)
                .then()
                .assertThat()
                .statusCode(200);


        RestAssured.delete(
                String.format("%s/%s/%s", endpoint, dto.distributionRuleId, "TestFilter")
        ).then()
                .statusCode(200);

    }

    @Test
    public void testGetKleWithFilter() throws Exception{

        CprDistributionRuleFilterDTO dto = new CprDistributionRuleFilterDTO();
        dto.name = "TestFilter";
        dto.assignedEmployeeId = 6;
        dto.assignedOrgId = 1;
        dto.days = "1-15";
        dto.months = "1-3";
        dto.distributionRuleId = 1;

        CprDistributionRuleFilterDTO dto2 = new CprDistributionRuleFilterDTO();
        dto.name = "TestFilter";
        dto.assignedEmployeeId = 6;
        dto.assignedOrgId = 1;
        dto.days = "15-20";
        dto.months = "1-3";
        dto.distributionRuleId = 1;

        RestAssured.given()
                .body(dto)
                .contentType(ContentType.JSON)
                .post(endpoint)
                .then()
                .assertThat()
                .statusCode(200);

        RestAssured.given()
                .body(dto2)
                .contentType(ContentType.JSON)
                .post(endpoint)
                .then()
                .assertThat()
                .statusCode(200);

       // RestAssured.get()



    }
}
