package stepDefinitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.restassured.response.Response;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class APISteps {

    Response response;
    Properties properties = new Properties();

    // Constructor - loads config once
    public APISteps() {
        try {
            InputStream input = getClass().getClassLoader()
                    .getResourceAsStream("config.properties");

            if (input == null) {
                throw new RuntimeException("config.properties not found!");
            }

            properties.load(input);
            System.out.println("Config loaded successfully");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Given("user calls OpenLibrary author API")
    public void callAuthorAPI() {

        String apiUrl = properties.getProperty("authorApiUrl");

        response = given()
                .when()
                .get(apiUrl)
                .then()
                .statusCode(200)
                .extract()
                .response();

        System.out.println("API called: " + apiUrl);
    }

    @Then("personal_name should be validated")
    public void validatePersonalName() {

        String actualName = response.jsonPath().getString("personal_name");
        String expectedName = properties.getProperty("expectedPersonalName");
        System.out.println("Actual personal_name: " + actualName+ " and expected personal_name "+expectedName);
        assertEquals(expectedName, actualName);
    }

    @Then("alternate_names should contain expected")
    public void validateAlternateNames() {

        java.util.List<String> alternateNames = response.jsonPath().getList("alternate_names");
        String expectedAltName = properties.getProperty("expectedAlternateName");
        System.out.println("Alternate Names: " + alternateNames+ " which contains expected alternate name "+expectedAltName);
        assertTrue(alternateNames.contains(expectedAltName));
    }
}
